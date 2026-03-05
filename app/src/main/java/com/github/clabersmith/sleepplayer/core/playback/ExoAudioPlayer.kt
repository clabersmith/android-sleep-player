package com.github.clabersmith.sleepplayer.core.playback

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExoAudioPlayer(
    context: Context
) : AudioPlayer {

    private val exoPlayer = ExoPlayer.Builder(context).build()

    private var snapshotJob: Job? = null
    private val scope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    // -----------------
    // supports update of player snapshots for UI display
    // ------------------
    private val _snapshotFlow =
        MutableStateFlow(
            PlayerSnapshot(
                positionMs = 0L,
                durationMs = 0L,
                isPlaying = false,
                volume = 60
            )
        )

    override val snapshotFlow: StateFlow<PlayerSnapshot>
        get() = _snapshotFlow

    init {
        exoPlayer.volume = 0.6f // default to 60%
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                emitSnapshot()

                if (isPlaying) {
                    startPositionUpdates()
                } else {
                    stopPositionUpdates()
                }
            }

            override fun onPlaybackStateChanged(state: Int) {
                emitSnapshot()
            }
        })
    }

    private fun startPositionUpdates() {
        if (snapshotJob != null) return

        snapshotJob = scope.launch {
            while (isActive && exoPlayer.isPlaying) {
                emitSnapshot()
                delay(500)
            }
        }
    }

    private fun stopPositionUpdates() {
        snapshotJob?.cancel()
        snapshotJob = null
    }

    private fun emitSnapshot() {
        _snapshotFlow.value = PlayerSnapshot(
            positionMs = exoPlayer.currentPosition,
            durationMs = exoPlayer.duration,
            isPlaying = exoPlayer.isPlaying,
            volume = exoPlayer.volume.times(100).toInt()
        )
    }

    override suspend fun load(source: AudioSource) {
        //ExoPlayer's setMediaItem and prepare must be called on the main thread
        withContext(Dispatchers.Main) {
            val mediaItem = MediaItem.fromUri(Uri.parse(source.uri))
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
        }
    }

    override fun play() {
        exoPlayer.playWhenReady = true
    }

    override fun pause() {
        exoPlayer.playWhenReady = false
    }

    override fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
    }

    override fun stop() {
        exoPlayer.stop()
    }

    override fun setVolume(volume: Int) {
        exoPlayer.volume = volume / 100f
        _snapshotFlow.value = _snapshotFlow.value.copy(volume = volume)
    }

    override fun currentPosition(): Long {
        return exoPlayer.currentPosition
    }

    override fun duration(): Long {
        return if (exoPlayer.duration > 0) exoPlayer.duration else 0L
    }

    override fun isPlaying(): Boolean {
        return exoPlayer.isPlaying
    }

    override fun release() {
        exoPlayer.release()
    }
}