package com.github.clabersmith.sleepplayer.core.playback

import androidx.annotation.RawRes
import kotlinx.coroutines.flow.StateFlow

interface WhiteNoisePlayer {

    val snapshotFlow: StateFlow<WhiteNoiseSnapshot>

    fun play(track: WhiteNoiseTrack)
    fun stop()

    fun setVolume(volume: Int)

    fun isPlaying(): Boolean

    fun release()
}

data class WhiteNoiseTrack(
    @RawRes val resId: Int
)

data class WhiteNoiseSnapshot(
    val isPlaying: Boolean = false,
    val volume: Int = 60,
    val currentTrack: WhiteNoiseTrack? = null
)