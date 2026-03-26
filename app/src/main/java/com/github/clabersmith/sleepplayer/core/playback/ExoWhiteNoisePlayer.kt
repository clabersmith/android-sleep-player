package com.github.clabersmith.sleepplayer.core.playback

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ExoWhiteNoisePlayer(
    context: Context,
) : WhiteNoisePlayer {

    private val exoPlayer = ExoPlayer.Builder(context).build()

    private val scope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var currentTrack: WhiteNoiseTrack? = null

    private val _snapshotFlow =
        MutableStateFlow(WhiteNoiseSnapshot())

    override val snapshotFlow: StateFlow<WhiteNoiseSnapshot>
        get() = _snapshotFlow

    init {
        exoPlayer.volume = 0.6f

        // Loop forever
        exoPlayer.repeatMode = Player.REPEAT_MODE_ONE

        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                emitSnapshot()
            }

            override fun onPlaybackStateChanged(state: Int) {
                emitSnapshot()
            }
        })
    }

    @OptIn(UnstableApi::class)
    override fun play(track: WhiteNoiseTrack) {
        // Avoid reloading same track if already playing
        if (currentTrack == track && exoPlayer.isPlaying) return

        currentTrack = track

        scope.launch(Dispatchers.Main) {
            val uri = RawResourceDataSource.buildRawResourceUri(track.resId)
            val mediaItem = MediaItem.fromUri(uri)

            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }
    }

    override fun stop() {
        exoPlayer.stop()
        currentTrack = null
        emitSnapshot()
    }

    override fun setVolume(volume: Int) {
        val clamped = volume.coerceIn(0, 100)
        exoPlayer.volume = clamped / 100f
        emitSnapshot()
    }

    override fun isPlaying(): Boolean {
        return exoPlayer.isPlaying
    }

    override fun release() {
        exoPlayer.release()
    }

    private fun emitSnapshot() {
        val snapshot = WhiteNoiseSnapshot(
            isPlaying = exoPlayer.isPlaying,
            volume = (exoPlayer.volume * 100).toInt(),
            currentTrack = currentTrack
        )

        _snapshotFlow.value = snapshot
    }
}