package com.github.clabersmith.sleepplayer.core.playback

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AudioPlaybackClock(
    scope: CoroutineScope
) : PlaybackClock {

    private val _timeMs = MutableStateFlow(0L)
    override val timeMs : StateFlow<Long> = _timeMs

    override fun now(): Long = _timeMs.value

    init {
        scope.launch {
            while (true) {
                delay(1000)
                _timeMs.value += 1000
            }
        }
    }
}