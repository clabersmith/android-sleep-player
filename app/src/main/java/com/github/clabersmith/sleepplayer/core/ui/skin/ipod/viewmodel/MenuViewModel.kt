package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.clabersmith.sleepplayer.core.playback.AudioPlayer
import com.github.clabersmith.sleepplayer.core.playback.AudioSource
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.ActionRow
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuContext
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuEvent
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuState
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.SlotState
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.toPersisted
import com.github.clabersmith.sleepplayer.features.podcasts.data.download.Downloader
import com.github.clabersmith.sleepplayer.features.podcasts.data.local.FileStorage
import com.github.clabersmith.sleepplayer.features.podcasts.data.local.SlotRepository
import com.github.clabersmith.sleepplayer.features.podcasts.domain.model.PodcastEpisode
import com.github.clabersmith.sleepplayer.features.podcasts.domain.model.PodcastFeed
import com.github.clabersmith.sleepplayer.features.podcasts.domain.repository.PodcastRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

/**
 * ViewModel driving the iPod-style menu and playback UI.
 *
 * Responsibilities:
 * - Holds UI state in [_menuState] and exposes it via [menuState] (a [StateFlow]).
 * - Caches podcast feeds, categories and persisted download slots in [context].
 * - Builds concrete menu screens: home, downloads, categories, feeds, episodes,
 *   episode detail, playlist and now-playing using [MenuState].
 * - Handles click-wheel input: selection movement, scan forward/back (fast seek),
 *   play/pause, confirm and back/menu navigation.
 * - Manages episode downloads: start/cancel, progress reporting, persisting/removing
 *   slots, and deleting downloaded files via [downloader], [slotRepository] and [storage].
 * - Manages audio playback via [player]: loading local files, play/pause/seek and
 *   periodic progress updates (polled on [playbackDispatcher]).
 *
 * Concurrency and lifecycle:
 * - Uses [viewModelScope] and keeps references to active jobs (downloadJob, scanJob,
 *   playProgressJob) which are cancelled and cleaned up in [onCleared].
 *
 * Constructor parameters:
 * @param podcastRepository repository for feeds and categories
 * @param slotRepository repository for persisted download slot data
 * @param downloader component responsible for downloading episode audio
 * @param storage file helper for existence checks, deletion and path resolution
 * @param player audio player used to play and seek local audio files
 * @param playbackDispatcher coroutine dispatcher for playback-related tasks
 */
class MenuViewModel(
    private val podcastRepository: PodcastRepository,
    private val slotRepository: SlotRepository,
    private val downloader: Downloader,
    private val storage: FileStorage,
    private val player: AudioPlayer
) : ViewModel() {

    private var context = MenuContext(
        slots = emptyList(),
        feeds = emptyList(),
        categories = emptyList(),
        maxSlotsCount = 4 // This is a fixed limit for the number of download slots, it can be adjusted as needed.
    )

    private val _menuState = MutableStateFlow<MenuState>(MenuState.Home(context))
    val menuState: StateFlow<MenuState> = _menuState

    private var downloadJob: Job? = null
    private var scanJob: Job? = null

    private var playProgressJob: Job? = null
    private var wasPlayingBeforeScan = false

    private val _activeSlot = MutableStateFlow<SlotState?>(null)
    val activeSlot: StateFlow<SlotState?> = _activeSlot

    val nowPlayingUiState: StateFlow<NowPlayingUiState> =
        combine(
            activeSlot,
            player.snapshotFlow
        ) { slot, snapshot ->

            NowPlayingUiState(
                slot = slot,
                positionMs = snapshot.positionMs,
                durationMs = snapshot.durationMs,
                isPlaying = snapshot.isPlaying
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            NowPlayingUiState.EMPTY
        )

    val homeItemsFlow: StateFlow<List<HomeItem>> =
        nowPlayingUiState
            .map { ui ->
                buildList {
                    add(HomeItem.Play)
                    if (ui.slot != null) add(HomeItem.NowPlaying)
                    add(HomeItem.Settings)
                }
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, listOf(HomeItem.Play, HomeItem.Settings))

    init {
        load()
    }

    // Initial load of feeds, categories and persisted download slots
    private fun load() {
        viewModelScope.launch {
            val feeds = podcastRepository.getFeeds()
            val categories = podcastRepository.getCategories().distinct().sorted()
            val restoredSlots = restoreSlots(feeds)

            updateContext {
                it.copy(
                    feeds = feeds,
                    categories = categories,
                    slots = restoredSlots
                )
            }
            // Start at Home screen after loading data
            _menuState.value = MenuState.Home(context)

            println("Loaded slots: ${context.slots}")
            println("Home created with slots: ${context.slots.size}")
        }

    }

    private fun updateContext(transform: (MenuContext) -> MenuContext) {
        context = transform(context)

        val current = _menuState.value
        _menuState.value = current.withContext(context)
    }

    private fun goToNowPlaying(slot: SlotState, origin: MenuState.NowPlaying.Origin) {
        _menuState.value = MenuState.NowPlaying(context, slot, origin)
    }

    // Effect handler that executes side effects emitted by menu state transitions
    private val effectHandler = MenuEffectHandler(
        player = player,
        startDownload = { state -> startDownload(state) },
        cancelDownload = { state -> cancelDownload(state) },
        deleteEpisode = { state -> deleteEpisode(state) },
        goToNowPlaying = { slot, origin -> goToNowPlaying(slot, origin) },
        checkStartPlayback = { slot -> checkStartPlayback(slot) },
        startScanForward = { startScanForward() },
        startScanBack = { startScanBack() },
        stopScan = { stopScan() },
    )

    // Dispatches a [MenuEvent] to the current state, processes the resulting state transition,
    // updates the state and executes any emitted effects.
    fun dispatch(event: MenuEvent) {
        val current = _menuState.value
        val transition = current.reduce(event)

        _menuState.value = transition.newState

        println("Transitioned " +
                "from ${current::class.simpleName} to ${transition.newState::class.simpleName} " +
                "on event ${event::class.simpleName}")

        transition.effects.forEach {
            println("Handling effect ${it::class.simpleName}")
            effectHandler.handle(it)
        }
    }

    private fun setState(newState: MenuState) {
        _menuState.value = newState
    }

    // Clean up resources on ViewModel clearance
    override fun onCleared() {
        super.onCleared()
        playProgressJob?.cancel()
        scanJob?.cancel()
        player.release()
    }

    // -----------------------------
    // Click Wheel Movement
    // -----------------------------
    fun moveSelection(delta: Int) {
        val state = _menuState.value

        val count = when (state) {
            is MenuState.Home -> homeItemsFlow.value.size
            else -> state.itemCount
        }

        setState(
            state.copyWithIndex(
                nextIndex(state.selectedIndex, delta, count)
            )
        )
    }

    // -----------------------------
    // Click Wheel Scan Forward/Back (Fast Seek)
    // -----------------------------
    fun onScanForwardDown() {
        dispatch(MenuEvent.ScanForwardDown)
    }

    fun onScanForwardUp() {
        dispatch(MenuEvent.ScanForwardUp)
    }

    fun onScanBackDown() {
        dispatch(MenuEvent.ScanBackDown)
    }

    fun onScanBackUp() {
        dispatch(MenuEvent.ScanBackUp)
    }

    fun startScanForward() {
        if (scanJob != null) return

        wasPlayingBeforeScan = player.isPlaying()
        player.pause()

        scanJob = viewModelScope.launch {
            val startTime = System.currentTimeMillis()

            while (isActive) {
                val elapsed = System.currentTimeMillis() - startTime

                val delta = computeScanDelta(elapsed)

                val current = player.currentPosition()
                val duration = player.duration()

                val newPosition = (current + delta)
                    .coerceAtMost(duration)

                player.seekTo(newPosition)

                delay(150)
            }
        }
    }

    fun startScanBack() {
        if (scanJob != null) return

        wasPlayingBeforeScan = player.isPlaying()
        player.pause()

        scanJob = viewModelScope.launch {
            val startTime = System.currentTimeMillis()

            while (isActive) {
                val elapsedMs = System.currentTimeMillis() - startTime

                val delta = computeScanDelta(elapsedMs)

                val current = player.currentPosition()

                val newPosition = (current - delta)
                    .coerceAtLeast(0)

                player.seekTo(newPosition)

                delay(150)
            }
        }
    }

    fun stopScan() {
        scanJob?.cancel()
        scanJob = null

        if (wasPlayingBeforeScan) {
            player.play()
        }
    }

    private fun computeScanDelta(elapsed: Long): Long {
        val minDelta = 1_000.0
        val maxDelta = 45_000.0
        val seconds = (elapsed / 1000.0).coerceAtLeast(0.0)
        val growthRate = 0.6 // increase to accelerate growth, decrease to slow it
        val raw = minDelta * kotlin.math.exp(seconds * growthRate)
        return raw.coerceIn(minDelta, maxDelta).toLong()
    }

    //------------------------------
    // Click Wheel Play/Pause
    //------------------------------
    fun onPlayPauseShortPressed() {
        if (_activeSlot.value != null) {
            if (player.isPlaying()) player.pause()
            else player.play()
        } else if (_menuState.value is MenuState.NowPlaying){
            //If we stopped the track in NowPlaying, we should be able to start again
            val current = _menuState.value as MenuState.NowPlaying
            val slotToPlay = current.slot
            checkStartPlayback(slotToPlay)
        }
    }

    fun onPlayPauseLongPressed() {
        if (_activeSlot.value != null) {
            player.stop()
            _activeSlot.value = null
        }
    }

    // -----------------------------
    // Click Wheel Center Button
    // -----------------------------
    fun confirmSelection() {
        val current = _menuState.value

        if (current is MenuState.Home) {
            println("Confirming selection on Home screen")
            val items = homeItemsFlow.value
            val selectedItem = items.getOrNull(current.selectedIndex) ?: return

            when (selectedItem) {
                HomeItem.Play -> {
                    dispatch(MenuEvent.Confirm)
                }

                HomeItem.NowPlaying -> {
                    val slot = _activeSlot.value ?: return
                    goToNowPlaying(slot, MenuState.NowPlaying.Origin.HOME)
                }

                HomeItem.Settings -> {
                    dispatch(MenuEvent.Confirm)
                }
            }

            return
        }

        dispatch(MenuEvent.Confirm)
    }

    fun startDownload(state: MenuState.EpisodeDetail) {
        if (state.isDownloading) return

        downloadJob?.cancel()

        downloadJob = viewModelScope.launch {

            val result = downloadEpisode(state)

            when (result) {
                is DownloadResult.Success -> {
                    setState(MenuState.Download(context, selectedIndex = 0))
                }

                is DownloadResult.Cancelled -> {
                    // Return to non-downloading episode detail
                    setState(state.copy(
                        isDownloading = false,
                        actionRows = listOf(ActionRow.Download),
                        selectedIndex = 0)
                    )
                }

                is DownloadResult.Error -> {
                    setState(state.copy(
                        isDownloading = false,
                        actionRows = listOf(ActionRow.Download),
                        selectedIndex = 0)
                    )
                }
            }
        }
    }

    private suspend fun downloadEpisode(
        state: MenuState.EpisodeDetail,
    ): DownloadResult {

        val feed = context.feeds.getOrNull(state.feedIndex)
            ?: return DownloadResult.Error(IllegalStateException("Invalid feed index"))

        val episode = feed.episodes.getOrNull(state.episodeIndex)
            ?: return DownloadResult.Error(IllegalStateException("Invalid episode index"))

        return try {

            val file = downloader.download(
                url = episode.audioUrl,
                fileName = generateSafeFileName(episode),
                onProgress = { progress ->
                    updateDownloadProgress(progress)
                }
            )

            addSlot(
                feedIndex = state.feedIndex,
                feedName = feed.title,
                episodeIndex = state.episodeIndex,
                episode = episode,
                fileName = file.name
            )

            DownloadResult.Success(file.name)

        } catch (_: CancellationException) {
            DownloadResult.Cancelled

        } catch (e: Exception) {
            DownloadResult.Error(e)
        }
    }

    fun updateDownloadProgress(progress: Float) {
        _menuState.update { current ->
            if (current is MenuState.EpisodeDetail && current.isDownloading) {
                current.copy(
                    actionRows = listOf(
                        ActionRow.Downloading(progress),
                        ActionRow.Cancel
                    )
                )
            } else current
        }
    }

    fun cancelDownload(state: MenuState.EpisodeDetail) {
        downloadJob?.cancel()
        restoreEpisodeDetail(state)
    }

    fun deleteEpisode(state: MenuState.EpisodeDetail) {
        val slot = context.slots.find {
            it.feedIndex == state.feedIndex &&
                    it.episodeIndex == state.episodeIndex
        } ?: return

        storage.deleteFile(slot.fileName)
        removeSlot(slot.feedIndex, slot.episodeIndex)
        goDownloaded()
    }

    fun checkStartPlayback(slot: SlotState) {
        println("Checking playback for slot ${slot.feedName} - ${slot.loadedEpisode.title}")
        if (_activeSlot.value != slot) {

            viewModelScope.launch {

                val path = storage.getFilePath(slot.fileName)

                player.load(AudioSource(path))
                player.play()

                _activeSlot.value = slot
            }
        }
    }

    // -----------------------------
    // Menu Button (Click Wheel Back
    // -----------------------------
    fun onMenuShortPress() {
        dispatch(MenuEvent.MenuShortPress)
    }

    fun onMenuLongPress() {
        dispatch(MenuEvent.MenuLongPress)
    }

    // -----------------------------
    // Navigation Helpers
    // -----------------------------
    private fun nextIndex(current: Int, delta: Int, size: Int): Int {
        if (size <= 0) return current
        return (current + delta + size) % size
    }

    fun goDownloaded() {
        return setState(MenuState.Download(context, selectedIndex = 0))
    }

    // -----------------------------
    // Download Slot Management
    // -----------------------------
    private fun addSlot(feedIndex: Int, episodeIndex: Int, feedName: String, episode: PodcastEpisode, fileName: String) {
        val newSlot = SlotState(
            feedIndex = feedIndex,
            feedName = feedName,
            episodeIndex = episodeIndex,
            loadedEpisode = episode,
            fileName = fileName
        )

        updateContext {
            val updated = (it.slots + newSlot)
                .take(it.maxSlotsCount)
            it.copy(slots = updated)
        }

        persistSlots(context.slots)
    }

    private fun removeSlot(feedIndex: Int, episodeIndex: Int) {
        updateContext {
            it.copy(
                slots = it.slots.filterNot {
                        s -> s.feedIndex == feedIndex &&
                        s.episodeIndex == episodeIndex
                }
            )
        }

        persistSlots(context.slots)
    }

    private fun persistSlots(slots : List<SlotState>) {
        viewModelScope.launch {
            slotRepository.saveSlots(
                slots.map { it.toPersisted() }
            )
        }
    }

    private suspend fun restoreSlots( feeds: List<PodcastFeed>): List<SlotState> {
        val persisted = slotRepository.loadSlots()

        return persisted.mapNotNull { p ->

            val feed = feeds.getOrNull(p.feedIndex) ?: return@mapNotNull null
            val episode = feed.episodes
                .firstOrNull { it.id == p.episodeId }
                ?: return@mapNotNull null

            SlotState(
                feedIndex = p.feedIndex,
                feedName = p.feedName,
                episodeIndex = p.episodeIndex,
                loadedEpisode = episode,
                fileName = p.fileName
            )

        }.take(context.maxSlotsCount)
    }

    //----------------
    // Download Helpers
    //----------------
    private fun generateSafeFileName(episode: PodcastEpisode): String {
        val base = episode.title
            .replace("[^A-Za-z0-9_]".toRegex(), "_")
            .take(50)

        return "$base.mp3"
    }

    private fun restoreEpisodeDetail(
        previous: MenuState.EpisodeDetail,
    ) {
        val alreadyDownloaded = context.slots.any {
            it.feedIndex == previous.feedIndex &&
                    it.episodeIndex == previous.episodeIndex
        }

        setState(
            previous.copy(
                isDownloading = false,
                actionRows = when {
                    alreadyDownloaded -> listOf(ActionRow.AlreadyDownloaded)
                    else -> listOf(ActionRow.Download)
                },
                selectedIndex = 0
            )
        )
    }

}

sealed class DownloadResult {
    data class Success(val fileName: String) : DownloadResult()
    object Cancelled : DownloadResult()
    data class Error(val throwable: Throwable) : DownloadResult()
}

data class NowPlayingUiState(
    val slot: SlotState?,
    val positionMs: Long,
    val durationMs: Long,
    val isPlaying: Boolean
) {
    companion object {
        val EMPTY = NowPlayingUiState(
            slot = null,
            positionMs = 0L,
            durationMs = 0L,
            isPlaying = false
        )
    }
}

sealed class HomeItem {
    object Play : HomeItem()
    object NowPlaying : HomeItem()
    object Settings : HomeItem()
}



