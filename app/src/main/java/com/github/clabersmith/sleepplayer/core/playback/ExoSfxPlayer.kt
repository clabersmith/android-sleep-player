package com.github.clabersmith.sleepplayer.core.playback

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ExoSfxPlayer(
    context: Context,
) : SfxPlayer {

    private val exoPlayer = ExoPlayer.Builder(context).build()

    private val scope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var currentIndex: Int? = null

    private val _snapshotFlow =
        MutableStateFlow(SfxSnapshot())

    override val snapshotFlow: StateFlow<SfxSnapshot>
        get() = _snapshotFlow

    init {
        // NO looping
        exoPlayer.repeatMode = Player.REPEAT_MODE_OFF

        exoPlayer.addListener(object : Player.Listener {

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                emitSnapshot()
            }

            override fun onPlaybackStateChanged(state: Int) {
                // When playback finishes → clear active SFX
                if (state == Player.STATE_ENDED) {
                    currentIndex = null
                }

                emitSnapshot()
            }
        })
    }

    override fun play(filePath: String, index: Int) {
        // Avoid restarting same file if already playing
        if (currentIndex == index && exoPlayer.isPlaying) return

        currentIndex = index

        scope.launch {
            val mediaItem = MediaItem.fromUri(filePath)

            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }
    }

    override fun stop() {
        exoPlayer.stop()
        currentIndex = null
        emitSnapshot()
    }

    override fun isPlaying(): Boolean {
        return exoPlayer.isPlaying
    }

    override fun release() {
        exoPlayer.release()
    }

    override fun setVolume(volume: Float) {
        exoPlayer.volume = volume.coerceIn(0f, 1f)
    }

    override fun getVolume(): Float {
        return exoPlayer.volume
    }

    private fun emitSnapshot() {
        _snapshotFlow.value = SfxSnapshot(
            isPlaying = exoPlayer.isPlaying,
            volume = exoPlayer.volume,
            currentIndex = currentIndex
        )
    }
}