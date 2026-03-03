package com.github.clabersmith.sleepplayer.core.playback

import kotlinx.coroutines.flow.Flow

interface AudioPlayer {

    val snapshotFlow: Flow<PlayerSnapshot>

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

data class PlayerSnapshot(
    val positionMs: Long,
    val durationMs: Long,
    val isPlaying: Boolean
)