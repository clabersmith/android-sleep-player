package com.github.clabersmith.sleepplayer.testutil.playback

import com.github.clabersmith.sleepplayer.core.playback.PlaybackClock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakePlaybackClock : PlaybackClock {
    private val _timeMs = MutableStateFlow(0L)
    override val timeMs : StateFlow<Long> = _timeMs

    override fun now(): Long = _timeMs.value

    fun advance(ms: Long) {
        _timeMs.value += ms
    }
}