package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.viewmodel

import com.github.clabersmith.sleepplayer.core.playback.AudioPlayer
import com.github.clabersmith.sleepplayer.core.playback.SfxPlayer
import com.github.clabersmith.sleepplayer.core.playback.Volume
import com.github.clabersmith.sleepplayer.core.playback.WhiteNoisePlayer
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.AudioSettings
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuEffect
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuState
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.PlaybackSettings
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.SlotState
import com.github.clabersmith.sleepplayer.features.podcasts.data.local.FileStorage
import com.github.clabersmith.sleepplayer.features.sfx.domain.repository.SfxRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


/**
 * Handles UI menu effects for the iPod skin.
 *
 * Processes [MenuEffect] instances and performs side-effects such as playback control,
 * downloads, file operations and scanning. Long-running operations are launched on the
 * provided [scope].
 *
 * @param scope CoroutineScope used to launch asynchronous work.
 * @param storage FileStorage for local file operations.
 * @param player AudioPlayer used for playback control.
 * @param startDownload Callback to initiate episode download.
 * @param cancelDownload Callback to cancel an ongoing episode download.
 * @param deleteEpisode Callback to delete a downloaded episode.
 * @param buildDownloadState Callback to refresh the download state of episodes.
 * @param startScanForward Callback to start scanning forward in the current episode.
 * @param startScanBack Callback to start scanning backward in the current episode.
 * @param stopScan Callback to stop any ongoing scanning.
 * @param navigateToPlay Callback to navigate to the Now Playing screen.
 */
class MenuEffectHandler(
    private val scope: CoroutineScope,
    private val podcastPlayer: AudioPlayer,
    private val whiteNoisePlayer: WhiteNoisePlayer,
    private val sfxPlayer: SfxPlayer,
    private val sfxRepository: SfxRepository,
    private val storage: FileStorage,
    private val startDownload: (state: MenuState.EpisodeDetail) -> Unit,
    private val cancelDownload: (state: MenuState.EpisodeDetail) -> Unit,
    private val deleteEpisode: (state: MenuState.EpisodeDetail) -> Unit,
    private val goToNowPlaying: (slot: SlotState, origin: MenuState.NowPlaying.Origin) -> Unit,
    private val checkStartPlayback: (slot: SlotState) -> Unit,
    private val startScanForward: () -> Unit,
    private val startScanBack: () -> Unit,
    private val stopScan: () -> Unit,
    private val updatePlaybackSettings: ( (PlaybackSettings) -> PlaybackSettings ) -> Unit,
    private val updateDisplayTheme: (MenuState.DisplaySettings.Theme) -> Unit,
    private val updateAudioSettings: ( (AudioSettings) -> AudioSettings) -> Unit,
    private val getWhiteNoiseBaseVolume: () -> Int,
    private val getSfxBaseVolume: () -> Int,
    private val stopPodcastPlayback: () -> Unit,
    private val updatePodcastVolume: (Float) -> Unit,
    private val updateWhiteNoiseVolume: (Float) -> Unit,
    private val updateSfxVolume: (Float) -> Unit,
    private val refreshSfxSlots: () -> Unit
) {
    private var repeatJob: Job? = null

    fun handle(effect: MenuEffect) {
        when (effect) {

            // -----------------------------
            // Playback effects
            // -----------------------------

            is MenuEffect.CheckStartPlayback -> {
                checkStartPlayback(effect.slot)
            }

            is MenuEffect.TogglePlayPause -> {
                if (podcastPlayer.isPlaying()) {
                    podcastPlayer.pause()
                } else {
                    podcastPlayer.play()
                }
            }

            is MenuEffect.StopPlayback -> {
                podcastPlayer.stop()
            }

            // -----------------------------
            // White Noise effects
            // -----------------------------

            is MenuEffect.StartWhiteNoise -> {
                val baseVolPercent = getWhiteNoiseBaseVolume()
                val baseVol = (baseVolPercent / 100f).coerceIn(0f, 1f)
                whiteNoisePlayer.play(effect.track)
                whiteNoisePlayer.setVolume(baseVol)
            }

            is MenuEffect.StopWhiteNoise -> {
                whiteNoisePlayer.stop()
            }

            // -----------------------------
            // Scan effects
            // -----------------------------

            is MenuEffect.StartScanForward -> {
                startScanForward()
            }

            is MenuEffect.StartScanBack -> {
                startScanBack()
            }

            is MenuEffect.StopScan -> {
                stopScan()
            }

            is MenuEffect.StartDownload ->
                startDownload(effect.state)

            is MenuEffect.CancelDownload ->
                cancelDownload(effect.state)

            is MenuEffect.DeleteEpisode ->
                deleteEpisode(effect.state)

            is MenuEffect.GoToNowPlaying -> {
                goToNowPlaying(effect.slot, effect.origin)
            }

            is MenuEffect.UpdatePlaybackSettings -> {
                updatePlaybackSettings(effect.transform)
            }

            is MenuEffect.UpdateDisplayTheme -> {
                updateDisplayTheme(effect.theme)
            }

            is MenuEffect.UpdateAudioSettings -> {
                updateAudioSettings(effect.transform)
            }

            is MenuEffect.UpdatePodcastVolume -> {
                val volume = (effect.volumePercent / 100f)
                    .coerceIn(0f, 1f)

                updatePodcastVolume(volume)
            }

            is MenuEffect.UpdateWhiteNoiseVolume -> {
                val volume = (effect.volumePercent / 100f)
                    .coerceIn(0f, 1f)

                updateWhiteNoiseVolume(volume)
            }

            is MenuEffect.UpdateSfxVolume -> {
                val volume = (effect.volumePercent / 100f)
                    .coerceIn(0f, 1f)

                updateSfxVolume(volume)
            }

            is MenuEffect.StopRepeatingEffect -> {
                repeatJob?.cancel()
                repeatJob = null
            }

            is MenuEffect.StartSfxDownload -> {
                scope.launch {
                    sfxRepository.startDownload()
                    refreshSfxSlots()
                }
            }

            is MenuEffect.PlaySfx -> {
                val index = effect.index

                scope.launch {
                    // Stop podcast if playing
                    stopPodcastPlayback()

                    val current = sfxPlayer.snapshotFlow.value.currentIndex

                    if (current == index && sfxPlayer.isPlaying()) {
                        sfxPlayer.stop()
                        return@launch
                    }

                    val fileName = sfxRepository.getFileNameForIndex(index)
                    if (fileName.isNullOrBlank()) return@launch
                    val filePath = storage.getFilePath(fileName)

                    val baseVolPercent = getSfxBaseVolume()
                    val baseVol = (baseVolPercent / 100f).coerceIn(0f, 1f)
                    sfxPlayer.play(filePath, index)
                    sfxPlayer.setVolume(baseVol)
                }
            }

            is MenuEffect.StopSfx -> {
                scope.launch {
                    sfxPlayer.stop()
                }
            }
        }
    }
}