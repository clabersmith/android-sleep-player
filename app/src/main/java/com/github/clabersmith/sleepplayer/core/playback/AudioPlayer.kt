package com.github.clabersmith.sleepplayer.core.playback

import kotlinx.coroutines.flow.StateFlow

interface AudioPlayer {

    val snapshotFlow: StateFlow<PlayerSnapshot>

    suspend fun load(source: AudioSource)

    fun play()
    fun pause()
    fun seekTo(positionMs: Long)
    fun setStartedAt(startedAtMs: Long)

    fun currentPosition(): Long
    fun duration(): Long
    fun isPlaying(): Boolean

    fun release()

    fun stop()

    fun setVolume(volume: Int)
}

data class PlayerSnapshot(
    val positionMs: Long,
    val durationMs: Long,
    val startedAtMs: Long?,
    val isPlaying: Boolean,
    val volume: Int
)