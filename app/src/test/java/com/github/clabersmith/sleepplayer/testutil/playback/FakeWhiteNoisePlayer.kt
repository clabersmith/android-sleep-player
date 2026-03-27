package com.github.clabersmith.sleepplayer.testutil.playback

import com.github.clabersmith.sleepplayer.core.playback.WhiteNoisePlayer
import com.github.clabersmith.sleepplayer.core.playback.WhiteNoiseSnapshot
import com.github.clabersmith.sleepplayer.core.playback.WhiteNoiseTrack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeWhiteNoisePlayer : WhiteNoisePlayer {

    var playCalled = false
    var stopCalled = false

    var lastPlayedTrack: WhiteNoiseTrack? = null
    var volumeSet: Int = 60

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

    override fun setVolume(volume: Int) {
        volumeSet = volume
        emitSnapshot()
    }

    override fun isPlaying(): Boolean = isPlaying

    override fun release() {
        // no-op
    }

    private fun emitSnapshot() {
        _snapshotFlow.value = WhiteNoiseSnapshot(
            isPlaying = isPlaying,
            volume = volumeSet,
            currentTrack = currentTrack
        )
    }
}