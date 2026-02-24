package com.github.clabersmith.sleepplayer.core.playback

interface AudioPlayer {

    suspend fun load(source: AudioSource)

    fun play()
    fun pause()
    fun seekTo(positionMs: Long)

    fun currentPosition(): Long
    fun duration(): Long
    fun isPlaying(): Boolean

    fun release()

    fun stop()
}