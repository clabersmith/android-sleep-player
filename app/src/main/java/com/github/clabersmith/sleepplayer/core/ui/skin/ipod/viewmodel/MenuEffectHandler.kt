package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.viewmodel

import com.github.clabersmith.sleepplayer.core.playback.AudioPlayer
import com.github.clabersmith.sleepplayer.core.playback.AudioSource
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuEffect
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuState.EpisodeDetail
import com.github.clabersmith.sleepplayer.features.podcasts.data.download.Downloader
import com.github.clabersmith.sleepplayer.features.podcasts.data.local.FileStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Handles UI menu effects for the iPod skin.
 *
 * Processes [MenuEffect] instances and performs side-effects such as playback control,
 * downloads, file operations and scanning. Long-running operations are launched on the
 * provided [scope].
 *
 * @param scope CoroutineScope used to launch asynchronous work.
 * @param downloader Downloader for episode downloads.
 * @param storage FileStorage for local file operations.
 * @param player AudioPlayer used for playback control.
 */
class MenuEffectHandler(
    private val scope: CoroutineScope,
    private val downloader: Downloader,
    private val storage: FileStorage,
    private val player: AudioPlayer,
    private val startDownload: (state: EpisodeDetail) -> Unit,
    private val cancelDownload: (state: EpisodeDetail) -> Unit,
    private val deleteEpisode: (state: EpisodeDetail) -> Unit,
    private val buildDownloadState: () -> Unit,
    private val startScanForward: () -> Unit,
    private val startScanBack: () -> Unit,
    private val stopScan: () -> Unit
) {
    fun handle(effect: MenuEffect) {
        when (effect) {

            is MenuEffect.StartPlayback -> {
                scope.launch {
                    val path = storage.getFilePath(effect.slot.fileName)
                    player.load(AudioSource(path))
                    player.play()
                }
            }

            MenuEffect.TogglePlayPause -> {
                if (player.isPlaying()) {
                    player.pause()
                } else {
                    player.play()
                }
            }

            MenuEffect.StopPlayback -> {
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

            MenuEffect.BuildDownloadState ->
                buildDownloadState()
        }
    }
}