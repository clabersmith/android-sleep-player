package com.github.clabersmith.sleepplayer.core.playback

import android.util.Log
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.PlaybackSettings
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.viewmodel.NowPlayingUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class AudioDuckingCoordinator(
    nowPlayingUiState: StateFlow<NowPlayingUiState>,
    playbackSettings: StateFlow<PlaybackSettings>,
    private val player: AudioPlayer,
    private val whiteNoisePlayer: WhiteNoisePlayer,
    private val scope: CoroutineScope,
    private val stopPlaybackCompletely: () -> Unit
) {

    private var duckPercent: Int = 20 // default fallback
    private var isDucked: Boolean = false

    private var fadeJob: Job? = null
    private var autoFadeTriggerJob: Job? = null
    private var podcastFadeJob: Job? = null
    private var autoStopJob: Job? = null

    init {
        observeSettings(playbackSettings)
        observePodcastPlayback(nowPlayingUiState)
        observeAutoFade(nowPlayingUiState, playbackSettings)
        observeAutoStop(nowPlayingUiState, playbackSettings)
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
                        Log.d("AudioDucking", "Duck volume percent changed. Updating ducked volume to $percent% ($target)")
                        whiteNoisePlayer.setVolume(target)
                    }
                }
        }
    }

    // ----- White Noise Fading Management -----

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


    private fun fadeTo(targetVolume: Float, durationMs: Long = 15000L, useEasing: Boolean = false) {
        fadeJob?.cancel()

        fadeJob = scope.launch {
            val start = whiteNoisePlayer.getVolume()
            val steps = 20
            val stepDelay = durationMs / steps

            var t: Float = 0f

            repeat(steps) { i ->
                if(useEasing) {
                    val rawT = (i + 1) / steps.toFloat()
                    t = easeLateHeavy(rawT)
                } else {
                    t = (i + 1) / steps.toFloat()
                }

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

    // ----- Podcast and White Noise Auto Fade on Timer Management -----

    private fun observeAutoFade(
        nowPlayingUiState: StateFlow<NowPlayingUiState>,
        playbackSettings: StateFlow<PlaybackSettings>
    ) {
        scope.launch {
            combine(
                nowPlayingUiState.map { it.isPlaying && it.slot != null },
                playbackSettings.map { it.autoFadeMinutes }
            ) { isPlaying, autoFadeMinutes ->
                Pair(isPlaying, autoFadeMinutes)
            }
                .distinctUntilChanged()
                .collect { (isPlaying, autoFadeMinutes) ->

                    autoFadeTriggerJob?.cancel()
                    podcastFadeJob?.cancel()

                    if (!isPlaying || autoFadeMinutes == null) return@collect

                    val triggerDelayMs = autoFadeMinutes * 60_000L

                    Log.d("AudioDucking", "Scheduling auto fade ONCE (delay: $triggerDelayMs ms)")

                    autoFadeTriggerJob = scope.launch {
                        delay(triggerDelayMs)

                        // Pull LATEST values at execution time
                        val nowPlaying = nowPlayingUiState.value
                        val settings = playbackSettings.value

                        startAutoFade(nowPlaying, settings)
                    }
                }
        }
    }

    private fun startAutoFade(
        nowPlaying: NowPlayingUiState,
        settings: PlaybackSettings
    ) {
        val remainingMs = nowPlaying.durationMs - nowPlaying.positionMs
        if (remainingMs <= 0) return

        val fadeDurationMs = when (val stopMin = settings.autoStopMinutes) {
            null -> remainingMs
            else -> {
                val stopMs = stopMin * 60_000L
                val elapsedMs = nowPlaying.positionMs
                val remainingToStop = stopMs - elapsedMs
                remainingToStop.coerceAtLeast(0)
            }
        }

        startWhiteNoiseFadeUp(fadeDurationMs)
        startPodcastFadeDown(fadeDurationMs)
    }

    private fun startWhiteNoiseFadeUp(durationMs: Long) {
        Log.d("AudioDucking", "Starting white noise fade UP (duration: $durationMs ms)")
        fadeTo(targetVolume = 1.0f, durationMs = durationMs, useEasing = true)
    }

    private fun startPodcastFadeDown(durationMs: Long) {
        podcastFadeJob?.cancel()

        podcastFadeJob = scope.launch {

            val steps = 40
            val stepDelay = (durationMs / steps).coerceAtLeast(50L)

            val startVolume = 1.0f

            repeat(steps) { i ->
                val rawT = (i + 1) / steps.toFloat()
                val easedT = easeLateHeavy(rawT)
                val volume = lerp(startVolume, 0f, easedT)

                Log.d("AudioDucking", "Fading podcast... step ${i + 1}/$steps, volume: $volume")
                setPodcastVolume(volume)

                delay(stepDelay)
            }

            setPodcastVolume(0f)
        }
    }

    private fun setPodcastVolume(volume: Float) {
        val scaled = (volume * 100).toInt().coerceIn(0, 100)
        player.setVolume(scaled)
    }

    private fun observeAutoStop(
        nowPlayingUiState: StateFlow<NowPlayingUiState>,
        playbackSettings: StateFlow<PlaybackSettings>
    ) {
        scope.launch {
            combine(
                nowPlayingUiState.map { it.isPlaying && it.slot != null },
                playbackSettings.map { it.autoStopMinutes }
            ) { playingKey, autoStopMinutes ->
                Pair(playingKey, autoStopMinutes)
            }
                .distinctUntilChanged()
                .collect { (isPlaying, autoStopMinutes) ->

                    autoStopJob?.cancel()

                    // Not playing or no track
                    if (!isPlaying) return@collect

                    val stopMinutes = autoStopMinutes ?: return@collect

                    // ALWAYS read latest state HERE
                    val nowPlaying = nowPlayingUiState.value

                    val stopTimeMs = stopMinutes * 60_000L
                    val currentPosition = nowPlaying.positionMs

                    val delayMs = stopTimeMs - currentPosition

                    if (delayMs <= 0) {
                        Log.d("AudioDucking", "Auto stop immediately (already past target)")
                        stopPodcast()
                        return@collect
                    }

                    Log.d("AudioDucking", "Scheduling auto stop (delay: $delayMs ms)")

                    autoStopJob = scope.launch {
                        delay(delayMs)

                        // Re-check before stopping (important edge case)
                        val latest = nowPlayingUiState.value

                        if (latest.isPlaying && latest.slot != null) {
                            stopPodcast()
                        }
                    }
                }
        }
    }

    private fun stopPodcast() {
        Log.d("AudioDucking", "Auto stop triggered. Stopping playback.")
        stopPlaybackCompletely()
    }

    private fun easeLateHeavy(t: Float): Float {
        // cubic ease-in (slow → fast)
        return t * t * t
    }

}