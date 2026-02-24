package com.github.clabersmith.sleepplayer.core.playback

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExoAudioPlayer(
    context: Context
) : AudioPlayer {
    private val exoPlayer = ExoPlayer.Builder(context).build()

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