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
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

class AudioDuckingCoordinator(
    nowPlayingUiState: StateFlow<NowPlayingUiState>,
    playbackSettings: StateFlow<PlaybackSettings>,
    private val player: AudioPlayer,
    private val whiteNoisePlayer: WhiteNoisePlayer,
    private val scope: CoroutineScope,
    private val playbackClock: PlaybackClock,
    private val getPodcastBaseVolume: () -> Int,
    private val getWhiteNoiseBaseVolume: () -> Int,
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
        observeAutoFade(nowPlayingUiState, playbackSettings, playbackClock)
        observeAutoStop(nowPlayingUiState, playbackSettings, playbackClock)
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
                        val target = Volume.percentToFloat(percent)
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
        fadeTo(targetVolume = Volume.percentToFloat(duckPercent))
    }

    private fun unduckWhiteNoise() {
        isDucked = false

        val base = Volume.percentToFloat(getWhiteNoiseBaseVolume())
        fadeTo(targetVolume = base)
    }

    private fun fadeTo(targetVolume: Float, durationMs: Long = 15000L, useEasing: Boolean = false) {
        fadeJob?.cancel()

        fadeJob = scope.launch {
            val start = whiteNoisePlayer.getVolume()
            val steps = 20
            val stepDelay = durationMs / steps

            var t: Float

            repeat(steps) { i ->
                if(useEasing) {
                    val rawT = (i + 1) / steps.toFloat()
                    t = easeLateHeavy(rawT)
                } else {
                    t = (i + 1) / steps.toFloat()
                }

                val volume = lerp(start, targetVolume, t)

                //Log.d("AudioDucking", "Fading white noise... step ${i + 1}/$steps, volume: $volume")
                whiteNoisePlayer.setVolume(volume)
                delay(stepDelay)
            }

            whiteNoisePlayer.setVolume(targetVolume)
            //Log.d("AudioDucking", "Fade complete. Target volume: $targetVolume")
        }
    }

    fun lerp(start: Float, end: Float, t: Float): Float {
        return start + (end - start) * t
    }

    // ----- Podcast and White Noise Auto Fade on Timer Management -----

    private fun observeAutoFade(
        nowPlayingUiState: StateFlow<NowPlayingUiState>,
        playbackSettings: StateFlow<PlaybackSettings>,
        playbackClock: PlaybackClock
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

                    // ALWAYS cancel existing jobs first
                    autoFadeTriggerJob?.cancel()
                    podcastFadeJob?.cancel()


                    if (!isPlaying || autoFadeMinutes == null) {
                        autoFadeTriggerJob = null
                        return@collect
                    }

                    val nowPlaying = nowPlayingUiState.value
                    val startedAtMs = nowPlaying.startedAtMs ?: playbackClock.now()
                    val triggerTimeMs = autoFadeMinutes * 60_000L

                    autoFadeTriggerJob = scope.launch {
                        playbackClock.timeMs
                            .map { it - startedAtMs } // elapsed
                            .filter { it >= triggerTimeMs }
                            .take(1)
                            .collect {
                                val latestNowPlaying = nowPlayingUiState.value
                                if (!latestNowPlaying.isPlaying || latestNowPlaying.slot == null) {
                                    return@collect
                                }

                                val latestSettings = playbackSettings.value
                                startAutoFade(latestNowPlaying, latestSettings)
                            }
                    }
                }
        }
    }

    private fun startAutoFade(
        nowPlaying: NowPlayingUiState,
        playbackSettings: PlaybackSettings
    ) {
        val remainingMs = nowPlaying.durationMs - nowPlaying.positionMs
        if (remainingMs <= 0) return

        val fadeDurationMs = when (val stopMin = playbackSettings.autoStopMinutes) {
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
        //Log.d("AudioDucking", "Starting white noise fade UP (duration: $durationMs ms)")
        val base = Volume.percentToFloat(getWhiteNoiseBaseVolume())
        fadeTo(targetVolume = 1.0f, durationMs = durationMs, useEasing = true)
    }


    private fun startPodcastFadeDown(durationMs: Long) {
        podcastFadeJob?.cancel()

        podcastFadeJob = scope.launch {

            val steps = 40
            val startVolume = player.getVolume()

            // scale duration based on remaining volume
            val baseVolume = Volume.percentToFloat(getPodcastBaseVolume())
            val remainingFraction =
                if (baseVolume > 0f) startVolume / baseVolume else 0f

            val adjustedDuration = (durationMs * remainingFraction).toLong()
            val stepDelay = (adjustedDuration / steps).coerceAtLeast(50L)

            repeat(steps) { i ->
                val rawT = (i + 1) / steps.toFloat()
                val easedT = easeLateHeavy(rawT)
                val volume = lerp(startVolume, 0f, easedT)

                //Log.d("AudioDucking", "Fading podcast... step ${i + 1}/$steps, volume: $volume")
                setPodcastVolume(volume)

                delay(stepDelay)
            }

            setPodcastVolume(0f)
        }
    }

    private fun setPodcastVolume(multiplier: Float) {
        val base = Volume.percentToFloat(getPodcastBaseVolume())

        val final = (base * multiplier)
            .coerceIn(0f, 1f)

        player.setVolume(final)
    }

    private fun observeAutoStop(
        nowPlayingUiState: StateFlow<NowPlayingUiState>,
        playbackSettings: StateFlow<PlaybackSettings>,
        playbackClock: PlaybackClock
    ) {
        scope.launch {
            combine(
                nowPlayingUiState.map { it.isPlaying to it.slot },
                playbackSettings.map { it.autoStopMinutes }
            ) { (isPlaying, slot), autoStopMinutes ->
                Triple(isPlaying, slot, autoStopMinutes)
            }
                .distinctUntilChanged()
                .collect { (isPlaying, slot, autoStopMinutes) ->

                    autoStopJob?.cancel()

                    // Not playing or no track
                    if (!isPlaying || slot == null) return@collect

                    val stopMinutes = autoStopMinutes ?: return@collect

                    // Capture start time
                    val nowPlaying = nowPlayingUiState.value
                    val startedAtMs = nowPlaying.startedAtMs ?: playbackClock.now()
                    val stopTimeMs = stopMinutes * 60_000L

                    // Create a flow that emits elapsed time
                    val autoStopFlow = playbackClock.timeMs
                        .map { elapsedMs -> elapsedMs - startedAtMs }
                        .filter { it >= stopTimeMs + 1000L} //add a small buffer to ensure we don't trigger early due to timing imprecision
                        .take(1) // Only need the first emission that reaches stop time

                    autoStopJob = scope.launch {
                        autoStopFlow.collect {
                            stopPodcast()
                        }
                    }
                }
        }
    }

    private fun stopPodcast() {
        //Log.d("AudioDucking", "Auto stop triggered. Stopping playback.")
        stopPlaybackCompletely()
    }

    private fun easeLateHeavy(t: Float): Float {
        // cubic ease-in (slow → fast)
        return t * t * t
    }
}