package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.clabersmith.sleepplayer.core.playback.AudioPlayer
import com.github.clabersmith.sleepplayer.core.playback.AudioSource
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuState
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuState.EpisodeDetail.Origin
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuState.EpisodeDetail.Origin.DOWNLOAD
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuState.EpisodeDetail.Origin.EPISODES
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.ActionRow
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuState.*
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.SlotState
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.toPersisted
import com.github.clabersmith.sleepplayer.features.podcasts.data.download.Downloader
import com.github.clabersmith.sleepplayer.features.podcasts.data.local.FileStorage
import com.github.clabersmith.sleepplayer.features.podcasts.data.local.SlotRepository
import com.github.clabersmith.sleepplayer.features.podcasts.domain.model.PodcastFeed
import com.github.clabersmith.sleepplayer.features.podcasts.domain.repository.PodcastRepository
import com.github.clabersmith.sleepplayer.features.podcasts.domain.DownloadConstants.MAX_SLOT_SIZE
import com.github.clabersmith.sleepplayer.features.podcasts.domain.model.PodcastEpisode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException


/**
 * ViewModel that drives the iPod-style menu and playback UI.
 *
 * Responsibilities:
 * - Maintains UI state as [MenuState] in [_menuState] and exposes it via [menuState].
 * - Loads and caches podcast feeds, categories and persisted download slots.
 * - Builds concrete menu screens (downloads, categories, feeds, episodes, episode detail,
 *   play list and now-playing) requested by the menu model via [MenuActions].
 * - Handles click-wheel interactions: selection movement, scan left/right (fast seek),
 *   play/pause and confirm/menus (navigation).
 * - Manages episode downloads: starts/cancels downloads, reports progress, persists slots,
 *   and deletes downloaded files.
 * - Manages audio playback: loading local files, play/pause toggling, periodic progress
 *   updates and resource cleanup on [onCleared].
 *
 * Concurrency and lifecycle:
 * - Uses [viewModelScope] for background tasks and keeps references to active jobs
 *   (downloadJob, scanJob, playProgressJob) which are cancelled appropriately.
 *
 * Constructor parameters:
 * @param podcastRepository Repository used to load feeds and categories.
 * @param slotRepository Repository used to persist/load download slot information.
 * @param downloader Component responsible for downloading episode audio files.
 * @param storage File storage helper for file existence, deletion and path resolution.
 * @param player Audio player used to play and seek local audio files.
 */
class MenuViewModel(
    private val podcastRepository: PodcastRepository,
    private val slotRepository: SlotRepository,
    private val downloader: Downloader,
    private val storage: FileStorage,
    private val player: AudioPlayer
) : ViewModel(), MenuActions {

    private val _menuState = MutableStateFlow<MenuState>(Home())
    val menuState: StateFlow<MenuState> = _menuState

    private val _feeds = MutableStateFlow<List<PodcastFeed>>(emptyList())
    private val _categories = MutableStateFlow<List<String>>(emptyList())
    private val _slots = MutableStateFlow<List<SlotState>>(emptyList())

    private var downloadJob: Job? = null
    private var scanJob: Job? = null

    private var playProgressJob: Job? = null
    private var wasPlayingBeforeScan = false

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _feeds.value = podcastRepository.getFeeds()
            _categories.value = podcastRepository.getCategories()

            restoreSlots()   // after feeds exist
        }
    }


    // -----------------------------
    // Click Wheel Movement
    // -----------------------------
    fun moveSelection(delta: Int) {
        val state = _menuState.value

        _menuState.value = state.copyWithIndex(
            nextIndex(state.selectedIndex, delta, state.itemCount)
        )
    }

    // -----------------------------
    // Click Wheel Scan Left/Right
    // -----------------------------

    fun onScanForwardDown() {
        _menuState.update { it.onScanForwardDown(actions = this) }
    }

    fun onScanForwardUp() {
        _menuState.update { it.onScanForwardUp(actions = this) }
    }

    fun onScanBackDown() {
        _menuState.update { it.onScanBackDown(actions = this) }
    }

    fun onScanBackUp() {
        _menuState.update { it.onScanBackUp(actions = this) }
    }

    override fun startScanForward() {
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

    override fun startScanBack() {
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

    override fun stopScan() {
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
    fun onPlayPausePressed() {
        _menuState.update { it.onPlayPause(actions = this) }
    }

    // -----------------------------
    // Click Wheel Center Button
    // -----------------------------
    fun confirmSelection() {
        val current = _menuState.value
        _menuState.value = current.onConfirm(actions = this)
    }

    override fun buildDownloadState(): Download {
        println("Building Download State with slots: ${_slots.value}")
        return Download(
            slots = _slots.value,
            maxSlots = MAX_SLOT_SIZE,
            selectedIndex = 0
        )
    }

    override fun buildCategoriesState(): Categories {
        return Categories(
            categories = _categories.value.sorted(),
            selectedIndex = 0
        )
    }

    override fun buildFeedsState(category: String): Feeds {
        val feeds = _feeds.value
            .filter { it.category == category }

        return Feeds(
            feeds = feeds,
            categoryName = category,
            selectedIndex = 0
        )
    }

    override fun buildEpisodesState(
        feedIndex: Int,
        categoryName: String
    ): Episodes {

        val feed = _feeds.value[feedIndex]

        return Episodes(
            episodes = feed.episodes,
            feedIndex = feedIndex,
            categoryName = categoryName,
            selectedIndex = 0
        )
    }

    override fun buildEpisodeDetailState(
        feedIndex: Int,
        episodeIndex: Int,
        episode: PodcastEpisode,
        origin: Origin
    ): EpisodeDetail {

        val alreadyDownloaded = _slots.value.any {
            it.feedIndex == feedIndex &&
                    it.episodeIndex == episodeIndex
        }

        val primaryAction = when {
            origin == DOWNLOAD -> ActionRow.Delete

            origin == EPISODES &&
                    alreadyDownloaded -> ActionRow.AlreadyDownloaded

            else -> ActionRow.Download
        }

        return EpisodeDetail(
            feedIndex = feedIndex,
            episodeIndex = episodeIndex,
            episode = episode,
            actionRows = listOf(primaryAction),
            origin = origin
        )
    }

    override fun buildPlayState(): Play {
        return Play(
            slots = _slots.value,
            selectedIndex = 0
        )
    }

    override fun buildNowPlayingState(slot: SlotState): NowPlaying {
        return NowPlaying(
            slot = slot,
            durationMs = 0L,
            positionMs = 0L,
            isPlaying = false
        )
    }

    override fun feedIndexOf(feed: PodcastFeed): Int {
        return _feeds.value.indexOf(feed)
    }


    override fun startDownload(state: EpisodeDetail) {
        if (state.isDownloading) return

        downloadJob?.cancel()

        downloadJob = viewModelScope.launch {

            val result = downloadEpisode(state)

            println("Download result: $result")

            when (result) {
                is DownloadResult.Success -> {
                    _menuState.value = buildDownloadState()
                }

                is DownloadResult.Cancelled -> {
                    // Return to non-downloading episode detail
                    _menuState.value = state.copy(
                        isDownloading = false,
                        actionRows = listOf(ActionRow.Download),
                        selectedIndex = 0
                    )
                }

                is DownloadResult.Error -> {
                    _menuState.value = state.copy(
                        isDownloading = false,
                        actionRows = listOf(ActionRow.Download),
                        selectedIndex = 0
                    )
                }
            }
        }
    }

    private suspend fun downloadEpisode(
        state: EpisodeDetail
    ): DownloadResult {

        val feed = _feeds.value.getOrNull(state.feedIndex)
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

        } catch (e: CancellationException) {
            DownloadResult.Cancelled

        } catch (e: Exception) {
            DownloadResult.Error(e)
        }
    }

    fun updateDownloadProgress(progress: Float) {
        _menuState.update { current ->
            if (current is EpisodeDetail && current.isDownloading) {
                current.copy(
                    actionRows = listOf(
                        ActionRow.Downloading(progress),
                        ActionRow.Cancel
                    )
                )
            } else current
        }
    }

    override fun cancelDownload(state: EpisodeDetail) {
        downloadJob?.cancel()
        restoreEpisodeDetail(state)
    }

    override fun deleteEpisode(state: EpisodeDetail) {
        val slot = _slots.value.find {
            it.feedIndex == state.feedIndex &&
                    it.episodeIndex == state.episodeIndex
        } ?: return

        storage.deleteFile(slot.fileName)
        removeSlot(slot.feedIndex, slot.episodeIndex)
        goDownloaded()
    }


    // -----------------------------
    // Menu Button (Click Wheel Back
    // -----------------------------
    fun onMenuShortPress() {
        _menuState.value = goUp()
    }

    fun onMenuLongPress() {
        _menuState.value = Home()
    }

    override fun stopPlayback() {
        player.stop()
    }

    fun goUp(): MenuState {
        return when (val state = _menuState.value) {

            is Home -> state

            is Download -> Home()

            is Categories -> Home()

            is Feeds -> Categories(
                categories = sortedCategories()
            )

            is Episodes ->
                Feeds(
                    categoryName = state.categoryName ?: "",
                    feeds = filteredFeeds(state.categoryName)
                )

            is EpisodeDetail -> {

                when (state.origin) {

                    DOWNLOAD ->
                        Download(
                            slots = _slots.value,
                            maxSlots = MAX_SLOT_SIZE
                        )

                    EPISODES ->
                        Episodes(
                            feedIndex = state.feedIndex,
                            episodes = _feeds.value[state.feedIndex].episodes,
                            categoryName = _feeds.value[state.feedIndex].category
                        )
                }
            }

            is Play -> Home(selectedIndex = 1)

            is NowPlaying -> {
                stopPlayback()
                Play(
                    slots = _slots.value,
                    selectedIndex = 0
                )
            }
        }
    }

    // -----------------------------
    // Data Helpers
    // -----------------------------
    private fun filteredFeeds(categoryName: String?): List<PodcastFeed> {
        return _feeds.value.filter { feed ->
            categoryName == null ||
                    feed.category.equals(categoryName, ignoreCase = true)
        }
    }

    private fun sortedCategories(): List<String> {
        return _categories.value
            .distinct()
            .sorted()
    }

    // -----------------------------
    // Navigation Helpers
    // -----------------------------
    private fun nextIndex(current: Int, delta: Int, size: Int): Int {
        if (size <= 0) return current
        return (current + delta + size) % size
    }

    fun goDownloaded(): MenuState {
        return buildDownloadState()
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

        _slots.value = (_slots.value + newSlot).take(MAX_SLOT_SIZE)
        persistSlots()
    }

    private fun removeSlot(feedIndex: Int, episodeIndex: Int) {
        _slots.value = _slots.value.filterNot {
            it.feedIndex == feedIndex &&
                    it.episodeIndex == episodeIndex
        }
        persistSlots()
    }

    private fun persistSlots() {
        viewModelScope.launch {
            slotRepository.saveSlots(
                _slots.value.map { it.toPersisted() }
            )
        }
    }

    private fun restoreSlots() {
        viewModelScope.launch {
            val persisted = slotRepository.loadSlots()

            _slots.value = persisted.mapNotNull { p ->

                if (!storage.fileExists(p.fileName)) {
                    return@mapNotNull null
                }

                val feed = _feeds.value.getOrNull(p.feedIndex)
                    ?: return@mapNotNull null

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
            }.take(MAX_SLOT_SIZE)

            persistSlots()

        }
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
        previous: EpisodeDetail
    ) {
        val alreadyDownloaded = _slots.value.any {
            it.feedIndex == previous.feedIndex &&
                    it.episodeIndex == previous.episodeIndex
        }

        _menuState.value = previous.copy(
            isDownloading = false,
            actionRows = when {
                alreadyDownloaded -> listOf(ActionRow.AlreadyDownloaded)
                else -> listOf(ActionRow.Download)
            },
            selectedIndex = 0
        )
    }

    //----------------
    // Audio Helpers
    //----------------
    override fun startPlayback(slot: SlotState): MenuState {

        val filePath = storage.getFilePath(slot.fileName)

        viewModelScope.launch {
            player.load(AudioSource(filePath))
            player.play()
            startPlayProgressUpdates()
        }

        return NowPlaying(
            slot = slot,
            durationMs = 0L, // will update after load
            positionMs = 0L,
            isPlaying = true
        )
    }

    override fun togglePlayPause(state: NowPlaying): MenuState {

        if (player.isPlaying()) {
            player.pause()
            return state.copy(isPlaying = false)
        } else {
            player.play()
            return state.copy(isPlaying = true)
        }
    }

    private fun startPlayProgressUpdates() {
        playProgressJob?.cancel()

        playProgressJob = viewModelScope.launch {
            while (isActive) {
                delay(500)

                val currentState = _menuState.value
                if (currentState is NowPlaying) {

                    _menuState.value = currentState.copy(
                        positionMs = player.currentPosition(),
                        durationMs = player.duration(),
                        isPlaying = player.isPlaying()
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        playProgressJob?.cancel()
        scanJob?.cancel()
        player.release()
    }
}

interface MenuActions {
    fun buildDownloadState(): MenuState
    fun buildCategoriesState(): MenuState
    fun buildFeedsState(category: String): MenuState
    fun buildEpisodesState(feedIndex: Int, categoryName: String): MenuState
    fun buildEpisodeDetailState(
        feedIndex: Int,
        episodeIndex: Int,
        episode: PodcastEpisode,
        origin: Origin
    ): MenuState

    fun buildPlayState(): MenuState

    fun buildNowPlayingState(slot: SlotState): NowPlaying

    fun feedIndexOf(feed: PodcastFeed): Int

    // Side-effect actions
    fun startDownload(state: EpisodeDetail)
    fun cancelDownload(state: EpisodeDetail)
    fun deleteEpisode(state: EpisodeDetail)
    fun startPlayback(slot: SlotState): MenuState
    fun togglePlayPause(state: NowPlaying): MenuState
    fun stopPlayback()
    fun startScanForward()
    fun startScanBack()
    fun stopScan()

}

sealed class DownloadResult {
    data class Success(val fileName: String) : DownloadResult()
    object Cancelled : DownloadResult()
    data class Error(val throwable: Throwable) : DownloadResult()
}


