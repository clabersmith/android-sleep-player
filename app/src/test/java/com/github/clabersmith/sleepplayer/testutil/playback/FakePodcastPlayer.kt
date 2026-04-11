package com.github.clabersmith.sleepplayer.testutil.playback

import com.github.clabersmith.sleepplayer.core.playback.AudioPlayer
import com.github.clabersmith.sleepplayer.core.playback.AudioSource
import com.github.clabersmith.sleepplayer.core.playback.PlayerSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakePodcastPlayer : AudioPlayer {

    var playCalled = false
    var pauseCalled = false
    var stopCalled = false

    val volumeHistory = mutableListOf<Float>()
    var volumeSet: Float = 0f

    var lastSeekPosition: Long = 0L



    private val _snapshotFlow = MutableStateFlow(
        PlayerSnapshot(
            positionMs = 0L,
            durationMs = 60_000L,
            isPlaying = false,
            startedAtMs = null,
            volume = 0f
        )
    )

    override val snapshotFlow: StateFlow<PlayerSnapshot>
        get() = _snapshotFlow

    var currentPosition: Long = 0L
    private var duration: Long = 60_000L
    private var isPlaying: Boolean = false

    private var startedAtMs: Long? = null

    override suspend fun load(source: AudioSource) {
        // no-op
    }

    override fun play() {
        playCalled = true
        pauseCalled = false
        isPlaying = true
        emitSnapshot()
    }

    override fun pause() {
        pauseCalled = true
        playCalled = false
        isPlaying = false
        emitSnapshot()
    }

    override fun stop() {
        stopCalled = true
        isPlaying = false
        emitSnapshot()
    }

    override fun setStartedAt(startedAtMs: Long) {
        this.startedAtMs = startedAtMs
        emitSnapshot()

    }

    override fun setVolume(volume: Float) {
        volumeHistory.add(volume)
        volumeSet = volume
    }

    override fun getVolume(): Float {
        return volumeSet
    }

    override fun seekTo(positionMs: Long) {
        lastSeekPosition = positionMs
        currentPosition = positionMs
        emitSnapshot()
    }

    fun advanceTime(ms: Long) {
        if (isPlaying) {
            currentPosition += ms
        }
    }

    override fun currentPosition(): Long = currentPosition

    override fun duration(): Long = duration

    override fun isPlaying(): Boolean = isPlaying

    override fun release() {}

    private fun emitSnapshot() {
        _snapshotFlow.value = PlayerSnapshot(
            positionMs = currentPosition,
            durationMs = duration,
            startedAtMs = startedAtMs,
            isPlaying = isPlaying,
            volume = volumeSet
        )
    }
}