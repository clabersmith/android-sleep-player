package com.github.clabersmith.sleepplayer.core.playback

import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.viewmodel.NowPlayingUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class AudioDuckingCoordinator(
    nowPlayingUiState: StateFlow<NowPlayingUiState>,
    private val whiteNoisePlayer: WhiteNoisePlayer,
    private val scope: CoroutineScope
) {

    init {
        observePodcastPlayback(nowPlayingUiState)
    }

    private fun observePodcastPlayback(
        nowPlayingUiState: StateFlow<NowPlayingUiState>
    ) {
        scope.launch {
            nowPlayingUiState
                .map { it.isPlaying && it.slot != null }
                .distinctUntilChanged()
                .collect { isPlaying ->
                    if (isPlaying) {
                        duckWhiteNoise()
                    } else {
                        unduckWhiteNoise()
                    }
                }
        }
    }

    private var fadeJob: Job? = null

    private fun duckWhiteNoise() {
        fadeTo(targetVolume = 0.2f)
    }

    private fun unduckWhiteNoise() {
        fadeTo(targetVolume = 1.0f)
    }

    private fun fadeTo(targetVolume: Float) {
        fadeJob?.cancel()

        fadeJob = scope.launch {
            val start = whiteNoisePlayer.getVolume()
            val steps = 20
            val durationMs = 15000L
            val stepDelay = durationMs / steps

            repeat(steps) { i ->
                val t = (i + 1) / steps.toFloat()
                val volume = lerp(start, targetVolume, t)
                whiteNoisePlayer.setVolume(volume)
                delay(stepDelay)
            }

            whiteNoisePlayer.setVolume(targetVolume)
        }
    }

    fun lerp(start: Float, end: Float, t: Float): Float {
        return start + (end - start) * t
    }
}