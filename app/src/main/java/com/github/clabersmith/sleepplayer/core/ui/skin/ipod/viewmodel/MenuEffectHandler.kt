package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.viewmodel

import com.github.clabersmith.sleepplayer.core.playback.AudioPlayer
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuEffect
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuState
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.SlotState
import com.github.clabersmith.sleepplayer.features.podcasts.data.local.FileStorage
import kotlinx.coroutines.CoroutineScope


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
    private val player: AudioPlayer,
    private val startDownload: (state: MenuState.EpisodeDetail) -> Unit,
    private val cancelDownload: (state: MenuState.EpisodeDetail) -> Unit,
    private val deleteEpisode: (state: MenuState.EpisodeDetail) -> Unit,
    private val goToNowPlaying: (slot: SlotState, origin: MenuState.NowPlaying.Origin) -> Unit,
    private val checkStartPlayback: (slot: SlotState) -> Unit,
    private val startScanForward: () -> Unit,
    private val startScanBack: () -> Unit,
    private val stopScan: () -> Unit,
) {
    fun handle(effect: MenuEffect) {
        when (effect) {
            is MenuEffect.CheckStartPlayback -> {
                checkStartPlayback(effect.slot)
            }

            is MenuEffect.TogglePlayPause -> {
                if (player.isPlaying()) {
                    player.pause()
                } else {
                    player.play()
                }
            }

            is MenuEffect.StopPlayback -> {
                player.stop()
            }

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
        }
    }
}