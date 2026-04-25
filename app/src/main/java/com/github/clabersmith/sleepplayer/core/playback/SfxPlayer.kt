package com.github.clabersmith.sleepplayer.core.playback

import kotlinx.coroutines.flow.StateFlow

interface SfxPlayer {
    val snapshotFlow: StateFlow<SfxSnapshot>

    fun play(filePath: String, index: Int)
    fun stop()
    fun isPlaying(): Boolean

    fun setVolume(volume: Float)
    fun getVolume(): Float

    fun release()
}

data class SfxSnapshot(
    val isPlaying: Boolean = false,
    val volume: Float = 1f,
    val currentIndex: Int? = null
)