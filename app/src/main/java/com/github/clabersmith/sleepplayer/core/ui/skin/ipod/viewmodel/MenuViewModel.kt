package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.clabersmith.sleepplayer.core.playback.AudioDuckingCoordinator
import com.github.clabersmith.sleepplayer.core.playback.AudioPlayer
import com.github.clabersmith.sleepplayer.core.playback.AudioSource
import com.github.clabersmith.sleepplayer.core.playback.PlaybackClock
import com.github.clabersmith.sleepplayer.core.playback.SfxPlayer
import com.github.clabersmith.sleepplayer.core.playback.SfxSnapshot
import com.github.clabersmith.sleepplayer.core.playback.Volume
import com.github.clabersmith.sleepplayer.core.playback.WhiteNoisePlayer
import com.github.clabersmith.sleepplayer.core.playback.WhiteNoiseTrack
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.ActionRow
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.AudioSettings
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.DisplaySettings
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuContext
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuEvent
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuState
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.NavDirection
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.PlaybackSettings
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.SlotState
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.toPersisted
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.viewmodel.NowPlayingUiState.NowPlayingBarMode
import com.github.clabersmith.sleepplayer.features.podcasts.data.download.PodcastDownloader
import com.github.clabersmith.sleepplayer.features.podcasts.data.local.FileStorage
import com.github.clabersmith.sleepplayer.features.podcasts.data.local.PersistedSettings
import com.github.clabersmith.sleepplayer.features.podcasts.data.local.SettingsRepository
import com.github.clabersmith.sleepplayer.features.podcasts.data.local.SlotRepository
import com.github.clabersmith.sleepplayer.features.podcasts.domain.model.PodcastEpisode
import com.github.clabersmith.sleepplayer.features.podcasts.domain.model.PodcastFeed
import com.github.clabersmith.sleepplayer.features.podcasts.domain.repository.PodcastRepository
import com.github.clabersmith.sleepplayer.features.sfx.data.local.PersistedSfxSlot
import com.github.clabersmith.sleepplayer.features.sfx.domain.repository.SfxRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.exp

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
 * - Manages audio playback via [podcastPlayer]: loading local files, play/pause/seek and
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
 * @param podcastPlayer audio player used to play and seek local audio files
 * @param playbackDispatcher coroutine dispatcher for playback-related tasks
 */
class MenuViewModel(
    private val podcastRepository: PodcastRepository,
    private val slotRepository: SlotRepository,
    private val settingsRepository: SettingsRepository,
    private val sfxRepository: SfxRepository,
    private val downloader: PodcastDownloader,
    private val storage: FileStorage,
    private val podcastPlayer: AudioPlayer,
    private val whiteNoisePlayer: WhiteNoisePlayer,
    private val sfxPlayer: SfxPlayer,
    private val playbackClock: PlaybackClock
) : ViewModel() {

    private var context = MenuContext(
        slots = emptyList(),
        feeds = emptyList(),
        categories = emptyList(),
        playbackSettings = PlaybackSettings(),
        maxSlotsCount = 4 // This is a fixed limit for the number of download slots, it can be adjusted as needed.
    )

    private val _menuState = MutableStateFlow<MenuState>(MenuState.Home(context))
    val menuState: StateFlow<MenuState> = _menuState

    private val _navDirection = MutableStateFlow<NavDirection>(NavDirection.None)
    val navDirection: StateFlow<NavDirection> = _navDirection

    private var downloadJob: Job? = null
    private var scanJob: Job? = null
    private var playProgressJob: Job? = null

    private var volumeTimeoutJob: Job? = null
    private var overridePodcastVolumeLevel: Int? = null
    private var wasPlayingBeforeScan = false

    private val _activeSlot = MutableStateFlow<SlotState?>(null)
    val activeSlot: StateFlow<SlotState?> = _activeSlot

    private val barMode = MutableStateFlow(
        NowPlayingBarMode.TrackPosition)

    val nowPlayingUiState: StateFlow<NowPlayingUiState> =
        combine(
            activeSlot,
            podcastPlayer.snapshotFlow,
            barMode
        ) { slot, snapshot, mode ->

            NowPlayingUiState(
                slot = slot,
                positionMs = snapshot.positionMs,
                durationMs = snapshot.durationMs,
                startedAtMs = snapshot.startedAtMs,
                isPlaying = snapshot.isPlaying,
                volume = snapshot.volume,
                barMode = mode
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            NowPlayingUiState.EMPTY
        )

    val whiteNoiseUiState: StateFlow<WhiteNoiseUiState> =
        whiteNoisePlayer.snapshotFlow
            .map {
                WhiteNoiseUiState(
                    isPlaying = it.isPlaying,
                    volume = it.volume,
                    currentTrack = it.currentTrack
                )
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                WhiteNoiseUiState()
            )

    val sfxUiState: StateFlow<SfxSnapshot> =
        sfxPlayer.snapshotFlow
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                SfxSnapshot()
            )

    val homeItemsFlow: StateFlow<List<HomeItem>> =
        nowPlayingUiState
            .map { ui ->
                buildList {
                    add(HomeItem.Play)
                    add(HomeItem.WhiteNoise)
                    add(HomeItem.Settings)
                    add(HomeItem.Extras)
                    if (ui.slot != null) add(HomeItem.NowPlaying)
                }
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, listOf(HomeItem.Play, HomeItem.Settings))

    val playbackPlaybackSettingsFlow: StateFlow<PlaybackSettings> =
        menuState
            .map { it.context.playbackSettings }
            .distinctUntilChanged()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                context.playbackSettings
            )

    private val audioDuckingCoordinator = AudioDuckingCoordinator(
        nowPlayingUiState = nowPlayingUiState,
        player = podcastPlayer,
        whiteNoisePlayer = whiteNoisePlayer,
        playbackSettings = playbackPlaybackSettingsFlow,
        scope = viewModelScope,
        playbackClock = playbackClock,
        getPodcastBaseVolume = {
            overridePodcastVolumeLevel
                ?: context.audioSettings.defaultPodcastVolume
        },
        getWhiteNoiseBaseVolume = { context.audioSettings.defaultWhiteNoiseVolume },
        stopPlaybackCompletely = {
            stopPlaybackCompletely()
        }
    )

    init {
        load()
        observeWhiteNoise()
        observePlaybackCompletion()
        observeSfxDownloadStatus()
        observeSfxPlayStatus()
        observeWhiteNoiseStopsSfx()
    }

    // Initial load of feeds, categories and persisted download slots
    private fun load() {
        viewModelScope.launch {
            val feeds = podcastRepository.getFeeds()
            val categories = podcastRepository.getCategories().distinct().sorted()
            val restoredSlots = restoreSlots(feeds)
            val restoredSettings = settingsRepository.loadSettings()

            updateContext {
                it.copy(
                    feeds = feeds,
                    categories = categories,
                    slots = restoredSlots,
                    playbackSettings = restoredSettings?.playbackSettings ?: PlaybackSettings(),
                    displaySettings = restoredSettings?.displaySettings ?: DisplaySettings(),
                    audioSettings = restoredSettings?.audioSettings ?: AudioSettings()
                )
            }
            // Start at Home screen after loading data
            _menuState.value = MenuState.Home(context)
        }

    }

    private fun observeWhiteNoise() {
        viewModelScope.launch {
            whiteNoiseUiState.collect { wnState ->

                updateContext { current ->
                    if (current.currentWhiteNoiseTrack == wnState.currentTrack) {
                        current // no-op to avoid unnecessary recomposition
                    } else {
                        current.copy(
                            currentWhiteNoiseTrack = wnState.currentTrack
                        )
                    }
                }
            }
        }
    }

    private fun observePlaybackCompletion() {
        viewModelScope.launch {
            podcastPlayer.snapshotFlow
                .map { it.isEnded }
                .distinctUntilChanged()
                .filter { it } // only when it becomes true
                .collect {
                    if (_activeSlot.value != null) {
                        stopPlaybackCompletely()
                    }
                }
        }
    }

    private fun observeSfxDownloadStatus() {
        viewModelScope.launch {
            sfxRepository.status.collect { status ->
                updateContext { current ->
                    current.copy(sfxDownloadStatus = status)
                }
            }
        }
    }

    private fun observeSfxSlots() {
        viewModelScope.launch {

            val slots = sfxRepository.getSlots()

            val lastUpdated = slots
                .mapNotNull { it.lastDownloadedAt }
                .filter { it > 0 }
                .maxOrNull()

            updateContext { current ->
                current.copy(
                    sfxSlots = slots,
                    sfxLastUpdatedAt = lastUpdated
                )
            }
        }
    }

    private fun observeSfxPlayStatus() {
        viewModelScope.launch {
            sfxUiState.collect { sfx ->
                updateContext {
                    it.copy(activeSfxIndex = sfx.currentIndex)
                }
            }
        }
    }

    private fun observeWhiteNoiseStopsSfx() {
        viewModelScope.launch {
            whiteNoiseUiState
                .map { it.isPlaying }
                .distinctUntilChanged()
                .collect { isPlaying ->

                    if (!isPlaying && sfxPlayer.isPlaying()) {
                        sfxPlayer.stop()
                    }
                }
        }
    }

    private fun updateContext(transform: (MenuContext) -> MenuContext) {
        context = transform(context)

        val current = _menuState.value
        _menuState.value = current.withContext(context)
    }

    // Effect handler that executes side effects emitted by menu state transitions
    private val effectHandler = MenuEffectHandler(
        scope = viewModelScope,
        podcastPlayer = podcastPlayer,
        whiteNoisePlayer = whiteNoisePlayer,
        sfxPlayer = sfxPlayer,
        storage = storage,
        sfxRepository = sfxRepository,
        startDownload = { state -> startDownload(state) },
        cancelDownload = { state -> cancelDownload(state) },
        deleteEpisode = { state -> deleteEpisode(state) },
        goToNowPlaying = { slot, origin -> goToNowPlaying(slot, origin) },
        checkStartPlayback = { slot -> checkStartPlayback(slot) },
        startScanForward = { startScanForward() },
        startScanBack = { startScanBack() },
        stopScan = { stopScan() },
        updatePlaybackSettings = { transform ->
            updatePlaybackSettings(transform)
        },
        updateDisplayTheme = { theme -> updateDisplayTheme(theme) },
        updateAudioSettings = { transform -> updateAudioSettings(transform) },
        getWhiteNoiseBaseVolume = { context.audioSettings.defaultWhiteNoiseVolume },
        getSfxBaseVolume = { context.audioSettings.defaultSfxVolume },
        stopPodcastPlayback = ::stopPlaybackCompletely,
        updatePodcastVolume = { volume -> podcastPlayer.setVolume(volume) },
        updateWhiteNoiseVolume = { volume -> whiteNoisePlayer.setVolume(volume) },
        updateSfxVolume = { volume -> sfxPlayer.setVolume(volume) },
        refreshSfxSlots = ::observeSfxSlots
    )

    // Dispatches a [MenuEvent] to the current state, processes the resulting state transition,
    // updates the state and executes any emitted effects.
    fun dispatch(event: MenuEvent) {
        val current = _menuState.value
        val transition = current.reduce(event)

        _menuState.value = transition.newState
        _navDirection.value = transition.direction

        transition.effects.forEach {
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
        podcastPlayer.release()
    }

    private fun startVolumeTimeout() {
        volumeTimeoutJob?.cancel()
        volumeTimeoutJob = viewModelScope.launch {
            delay(3000)
            barMode.value = NowPlayingBarMode.TrackPosition
        }
    }

    // -----------------------------
    // Settings Helpers
    // -----------------------------
    private fun updateAudioSettings(
        transform: (AudioSettings) -> AudioSettings
    ) {
        updateContext {
            it.copy(
                audioSettings = transform(it.audioSettings)
            )
        }

        persistSettings()
    }

    private fun updatePlaybackSettings(
        transform: (PlaybackSettings) -> PlaybackSettings
    ) {
        updateContext {
            it.copy(
                playbackSettings = transform(it.playbackSettings)
            )
        }

        persistSettings()
    }

    private fun updateDisplayTheme(
        theme: MenuState.DisplaySettings.Theme
    ) {
        updateContext { context ->
            context.copy(
                displaySettings = context.displaySettings.copy(
                    theme = theme
                )
            )
        }

        persistSettings()
    }

    private fun persistSettings() {
        viewModelScope.launch {
            settingsRepository.saveSettings(
                PersistedSettings(
                    playbackSettings = context.playbackSettings,
                    displaySettings = context.displaySettings,
                    audioSettings = context.audioSettings
                )
            )
        }
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


    private fun goToNowPlaying(slot: SlotState, origin: MenuState.NowPlaying.Origin) {
        barMode.value = NowPlayingBarMode.TrackPosition
        _menuState.value = MenuState.NowPlaying(context, slot, origin)
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
        // If we're in Volume mode, adjust volume instead of seeking
        if (barMode.value == NowPlayingBarMode.VolumeLevel) {
            adjustVolume(+5)
            startVolumeTimeout()
            return
        }

        if (scanJob != null) return

        wasPlayingBeforeScan = podcastPlayer.isPlaying()
        podcastPlayer.pause()

        scanJob = viewModelScope.launch {
            val startTime = System.currentTimeMillis()

            while (isActive) {
                val elapsed = System.currentTimeMillis() - startTime
                val delta = computeScanDelta(elapsed)

                val current = podcastPlayer.currentPosition()
                val duration = podcastPlayer.duration()

                val newPosition = (current + delta)
                    .coerceAtMost(duration)

                podcastPlayer.seekTo(newPosition)

                delay(150)
            }
        }
    }

    fun startScanBack() {
        // If we're in Volume mode, adjust volume instead of seeking
        if (barMode.value == NowPlayingBarMode.VolumeLevel) {
            adjustVolume(-5)
            startVolumeTimeout()
            return
        }

        if (scanJob != null) return

        wasPlayingBeforeScan = podcastPlayer.isPlaying()
        podcastPlayer.pause()

        scanJob = viewModelScope.launch {
            val startTime = System.currentTimeMillis()

            while (isActive) {
                val elapsedMs = System.currentTimeMillis() - startTime
                val delta = computeScanDelta(elapsedMs)

                val current = podcastPlayer.currentPosition()

                val newPosition = (current - delta)
                    .coerceAtLeast(0)

                podcastPlayer.seekTo(newPosition)

                delay(150)
            }
        }
    }

    fun stopScan() {
        scanJob?.cancel()
        scanJob = null

        if (wasPlayingBeforeScan) {
            podcastPlayer.play()
        }
    }

    private fun computeScanDelta(elapsed: Long): Long {
        val minDelta = 1_000.0
        val maxDelta = 45_000.0
        val seconds = (elapsed / 1000.0).coerceAtLeast(0.0)
        val growthRate = 0.6 // increase to accelerate growth, decrease to slow it
        val raw = minDelta * exp(seconds * growthRate)
        return raw.coerceIn(minDelta, maxDelta).toLong()
    }

    private fun adjustVolume(delta: Int) {
        val current = overridePodcastVolumeLevel
            ?: context.audioSettings.defaultPodcastVolume

        val newVolumePercent = (current + delta)
            .coerceIn(0, 100)

        // store override
        overridePodcastVolumeLevel = newVolumePercent

        // apply immediately
        podcastPlayer.setVolume(Volume.percentToFloat(newVolumePercent))
    }

    //------------------------------
    // Click Wheel Play/Pause
    //------------------------------
    fun onPlayPauseShortPressed() {
        if (_activeSlot.value != null) {
            if (podcastPlayer.isPlaying()) podcastPlayer.pause()
            else podcastPlayer.play()
        } else if (_menuState.value is MenuState.NowPlaying){
            //If we stopped the track in NowPlaying, we should be able to start again
            val current = _menuState.value as MenuState.NowPlaying
            val slotToPlay = current.slot
            checkStartPlayback(slotToPlay)
        }
    }

    fun stopPlaybackCompletely() {
        if (_activeSlot.value != null) {
            podcastPlayer.stop()
            _activeSlot.value = null
        }

        overridePodcastVolumeLevel = null

        //Log.d("MenuViewModel", "_menuState.value: ${_menuState.value}")

        // Only dispatch if we are on NowPlaying
        if (_menuState.value is MenuState.NowPlaying) {
            dispatch(MenuEvent.PlaybackStopped)
        }
    }

    fun stopWhiteNoise() {
        whiteNoisePlayer.stop()
    }

    // -----------------------------
    // Click Wheel Center Button
    // -----------------------------
    fun confirmSelection() {
        val current = _menuState.value

        if (current is MenuState.Home) {
            val items = homeItemsFlow.value
            val selectedItem = items.getOrNull(current.selectedIndex) ?: return

            when (selectedItem) {
                HomeItem.Play, HomeItem.WhiteNoise, HomeItem.Settings, HomeItem.Extras -> {
                    dispatch(MenuEvent.Confirm)
                }

                HomeItem.NowPlaying -> {
                    val slot = _activeSlot.value ?: return
                    goToNowPlaying(slot, MenuState.NowPlaying.Origin.HOME)
                }
            }

            return
        } else if (current is MenuState.NowPlaying) {
            //if NowPlaying is selected, we want to toggle between track position and volume controls
            onNowPlayingConfirmPressed()
        }

        dispatch(MenuEvent.Confirm)
    }

    fun onNowPlayingConfirmPressed() {
        barMode.update {
            when (it) {
                NowPlayingBarMode.TrackPosition -> {
                    startVolumeTimeout()
                    NowPlayingBarMode.VolumeLevel
                }

                NowPlayingBarMode.VolumeLevel -> {
                    NowPlayingBarMode.TrackPosition
                }
            }
        }
    }

    //------------------------------
    // Episode Download Management
    //------------------------------

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

    /**
     * Start playback for the given download slot as a true start of the podcast.
     *
     * This method does not toggle play/pause. Instead, when the provided slot is not
     * currently active it:
     *  - loads the audio from storage,
     *  - records the absolute start time via `player.setStartedAt`,
     *  - and begins playback.
     */
    fun checkStartPlayback(slot: SlotState) {
        if (_activeSlot.value != slot) {

            viewModelScope.launch {
                sfxPlayer.stop()  //make sure we don't run sfx on top of the podcast audio

                val path = storage.getFilePath(slot.fileName)

                podcastPlayer.load(AudioSource(path))

                val now = playbackClock.now()
                podcastPlayer.setStartedAt(now)

                podcastPlayer.play()


                overridePodcastVolumeLevel = context.audioSettings.defaultPodcastVolume
                val volume = Volume.percentToFloat(context.audioSettings.defaultPodcastVolume)

                podcastPlayer.setVolume(volume)

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
    val startedAtMs: Long? = null,
    val isPlaying: Boolean,
    val volume: Float,
    val barMode: NowPlayingBarMode = NowPlayingBarMode.TrackPosition
) {
    companion object {
        val EMPTY = NowPlayingUiState(
            slot = null,
            positionMs = 0L,
            durationMs = 0L,
            isPlaying = false,
            volume = .5f
        )
    }

    enum class NowPlayingBarMode {
        TrackPosition,
        VolumeLevel
    }
}

data class WhiteNoiseUiState(
    val isPlaying: Boolean = false,
    val volume: Float = .6f,
    val currentTrack: WhiteNoiseTrack? = null
)

sealed class HomeItem {
    object Play : HomeItem()
    object WhiteNoise : HomeItem()
    object NowPlaying : HomeItem()
    object Settings : HomeItem()
    object Extras : HomeItem()
}



