package com.github.clabersmith.sleepplayer.testutil.playback

import com.github.clabersmith.sleepplayer.core.playback.SfxPlayer
import com.github.clabersmith.sleepplayer.core.playback.SfxSnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeSfxPlayer : SfxPlayer {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    var playCalled = false
    var stopCalled = false
    var setVolumeCalled = false

    private var isPlayingInternal = false
    private var currentIndexInternal: Int? = null
    private var currentFilePath: String? = null
    var volumeSet: Float = 0f

    private val _snapshotFlow = MutableStateFlow(SfxSnapshot())
    override val snapshotFlow: StateFlow<SfxSnapshot> = _snapshotFlow

    override fun play(filePath: String, index: Int) {
        // Same behavior as real player: avoid restarting same item
        if (currentIndexInternal == index && isPlayingInternal) return

        playCalled = true

        currentFilePath = filePath
        currentIndexInternal = index
        isPlayingInternal = true

        emitSnapshot()
    }

    override fun stop() {
        isPlayingInternal = false
        currentIndexInternal = null
        currentFilePath = null
        stopCalled = true

        emitSnapshot()
    }

    override fun isPlaying(): Boolean = isPlayingInternal

    override fun release() {
        scope.cancel()
    }

    override fun setVolume(volume: Float) {
        setVolumeCalled = true
        volumeSet = volume
    }

    override fun getVolume(): Float {
        return volumeSet
    }


    // -----------------------------------
    // Test helpers (VERY useful)
    // -----------------------------------

    fun getCurrentIndex(): Int? = currentIndexInternal

    /**
     * Simulate playback finishing naturally
     */
    fun completePlayback() {
        isPlayingInternal = false
        currentIndexInternal = null
        currentFilePath = null

        emitSnapshot()
    }

    /**
     * Force-set state (useful for edge-case tests)
     */
    fun setState(
        isPlaying: Boolean,
        index: Int?
    ) {
        isPlayingInternal = isPlaying
        currentIndexInternal = index
        emitSnapshot()
    }

    private fun emitSnapshot() {
        _snapshotFlow.value = SfxSnapshot(
            isPlaying = isPlayingInternal,
            currentIndex = currentIndexInternal
        )
    }
}