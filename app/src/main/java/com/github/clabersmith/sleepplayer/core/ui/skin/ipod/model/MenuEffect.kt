package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model

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
    // Scan effects
    // -----------------------------
    object StartScanForward : MenuEffect
    object StartScanBack : MenuEffect
    object StopScan : MenuEffect

}