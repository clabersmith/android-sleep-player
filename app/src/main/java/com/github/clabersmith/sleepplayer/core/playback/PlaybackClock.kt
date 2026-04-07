package com.github.clabersmith.sleepplayer.core.playback

import kotlinx.coroutines.flow.StateFlow

interface PlaybackClock {
    fun now(): Long
    val timeMs: StateFlow<Long>
}
