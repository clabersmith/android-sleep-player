package com.github.clabersmith.sleepplayer.core.playback

import android.util.Log
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.PlaybackSettings
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
    playbackSettings: StateFlow<PlaybackSettings>,
    private val whiteNoisePlayer: WhiteNoisePlayer,
    private val scope: CoroutineScope
) {

    private var duckPercent: Int = 20 // default fallback
    private var isDucked: Boolean = false

    private var fadeJob: Job? = null

    init {
        observePodcastPlayback(nowPlayingUiState)
        observeSettings(playbackSettings)
    }

    private fun observeSettings(
        playbackSettings: StateFlow<PlaybackSettings>
    ) {
        scope.launch {
            playbackSettings
                .map { it.duckVolumePercent }
                .distinctUntilChanged()
                .collect { percent ->
                    duckPercent = percent

                    //if already ducked, update immediately
                    if (isDucked) {
                        val target = percentToVolume(percent)
                        whiteNoisePlayer.setVolume(target)
                    }
                }
        }
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

    private fun duckWhiteNoise() {
        isDucked = true
        fadeTo(targetVolume = percentToVolume(duckPercent))
    }


    private fun unduckWhiteNoise() {
        isDucked = false
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
                Log.d("AudioDucking", "Fading... step ${i + 1}/$steps, volume: $volume")
                whiteNoisePlayer.setVolume(volume)
                delay(stepDelay)
            }

            whiteNoisePlayer.setVolume(targetVolume)
            Log.d("AudioDucking", "Fade complete. Target volume: $targetVolume")
        }
    }

    private fun percentToVolume(percent: Int): Float {
        return (percent / 100f).coerceIn(0f, 1f)
    }

    fun lerp(start: Float, end: Float, t: Float): Float {
        return start + (end - start) * t
    }
}