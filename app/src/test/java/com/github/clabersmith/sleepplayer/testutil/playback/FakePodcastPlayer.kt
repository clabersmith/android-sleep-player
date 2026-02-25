package com.github.clabersmith.sleepplayer.testutil.playback

import com.github.clabersmith.sleepplayer.core.playback.AudioPlayer
import com.github.clabersmith.sleepplayer.core.playback.AudioSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakePodcastPlayer : AudioPlayer {
    var playCalled = false
    var pauseCalled = false
    var stopCalled = false
    var lastSeekPosition: Long = 0L

    private val _isPlaying = MutableStateFlow(false)

    var currentPosition: Long = 0L
    var duration: Long = 60_000L
    override suspend fun load(source: AudioSource) {
        // No-op for fake player
    }

    override fun play() {
        playCalled = true
        _isPlaying.value = true
    }

    override fun pause() {
        pauseCalled = true
        _isPlaying.value = false
    }

    override fun stop() {
        stopCalled = true
        _isPlaying.value = false
    }

    override fun seekTo(positionMs: Long) {
        lastSeekPosition = positionMs
        currentPosition = positionMs
    }

    override fun currentPosition(): Long {
        return currentPosition
    }

    override fun duration(): Long {
        return duration
    }

    override fun isPlaying(): Boolean {
       return _isPlaying.value
    }

    override fun release() {
        // No-op for fake player
    }
}