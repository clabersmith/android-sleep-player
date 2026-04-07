package com.github.clabersmith.sleepplayer.testutil.playback

import com.github.clabersmith.sleepplayer.core.playback.WhiteNoisePlayer
import com.github.clabersmith.sleepplayer.core.playback.WhiteNoiseSnapshot
import com.github.clabersmith.sleepplayer.core.playback.WhiteNoiseTrack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeWhiteNoisePlayer : WhiteNoisePlayer {

    var playCalled = false
    var stopCalled = false
    var setVolumeCalled = false

    var lastPlayedTrack: WhiteNoiseTrack? = null
    var volumeSet: Float = -1.0f //initially 0 for testing purposes

    private val _snapshotFlow = MutableStateFlow(
        WhiteNoiseSnapshot(
            isPlaying = false,
            volume = volumeSet,
            currentTrack = null
        )
    )

    override val snapshotFlow: StateFlow<WhiteNoiseSnapshot>
        get() = _snapshotFlow

    private var isPlaying: Boolean = false
    private var currentTrack: WhiteNoiseTrack? = null

    override fun play(track: WhiteNoiseTrack) {
        playCalled = true
        lastPlayedTrack = track

        isPlaying = true
        currentTrack = track

        emitSnapshot()
    }

    override fun stop() {
        stopCalled = true

        isPlaying = false
        currentTrack = null

        emitSnapshot()
    }

    override fun isPlaying(): Boolean = isPlaying

    override fun release() {
        // no-op
    }

    override fun setVolume(volume: Float) {
        setVolumeCalled = true
        volumeSet = volume
    }

    override fun getVolume(): Float {
        return volumeSet
    }

    private fun emitSnapshot() {
        _snapshotFlow.value = WhiteNoiseSnapshot(
            isPlaying = isPlaying,
            volume = volumeSet,
            currentTrack = currentTrack
        )
    }
}