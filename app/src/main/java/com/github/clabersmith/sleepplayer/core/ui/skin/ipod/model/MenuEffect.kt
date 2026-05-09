package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model

import com.github.clabersmith.sleepplayer.core.playback.WhiteNoiseTrack
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuState.EpisodeDetail

/**
 * Describes side effects that must be executed after a state transition.
 *
 * Effects are executed by the ViewModel layer.
 */
sealed interface MenuEffect {

    // -----------------------------
    // Download effects
    // -----------------------------

    data class StartDownload(val state: EpisodeDetail) : MenuEffect
    data class CancelDownload(val state: EpisodeDetail) : MenuEffect
    data class DeleteEpisode(val state: EpisodeDetail) : MenuEffect

    // -----------------------------
    // Playback effects
    // -----------------------------

    data class CheckStartPlayback(
        val slot: SlotState
    ) : MenuEffect

    data class GoToNowPlaying(
        val slot: SlotState,
        val origin: MenuState.NowPlaying.Origin
    ) : MenuEffect

    data object TogglePlayPause : MenuEffect

    data object StopPlayback : MenuEffect

    // -----------------------------
    // White Noise effects
    // -----------------------------

    data class StartWhiteNoise(val track: WhiteNoiseTrack) : MenuEffect
    object StopWhiteNoise : MenuEffect
    data class UpdatePlaybackSettings(
        val transform: (PlaybackSettings) -> PlaybackSettings
    ) : MenuEffect

    // -----------------------------
    // Display Theme effects
    // -----------------------------
    data class UpdateDisplayTheme(
        val theme: MenuState.DisplaySettings.Theme
    ) : MenuEffect

    // -----------------------------
    // Audio Settings effects
    // -----------------------------
    data class UpdateAudioSettings(
        val transform: (AudioSettings) -> AudioSettings
    ) : MenuEffect

    data class UpdatePodcastVolume(
        val volumePercent: Int
    ) : MenuEffect

    data class UpdateWhiteNoiseVolume(
        val volumePercent: Int
    ) : MenuEffect

    data class UpdateSfxVolume(
        val volumePercent: Int
    ) : MenuEffect


    // -----------------------------
    // Scan effects
    // -----------------------------
    object StartScanForward : MenuEffect
    object StartScanBack : MenuEffect
    object StopScan : MenuEffect
    object StopRepeatingEffect : MenuEffect

    // -----------------------------
    // SFX effects
    // -----------------------------
    object StartSfxDownload : MenuEffect
    data class PlaySfx(val index: Int) : MenuEffect

    object StopSfx : MenuEffect
}