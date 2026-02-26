package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model

/**
 * Describes side effects that must be executed after a state transition.
 *
 * Effects are executed by the ViewModel layer.
 */
sealed interface MenuEffect {

    // -----------------------------
    // Download effects
    // -----------------------------

    data class StartDownload(
        val feedIndex: Int,
        val episodeIndex: Int
    ) : MenuEffect

    data class CancelDownload(
        val feedIndex: Int,
        val episodeIndex: Int
    ) : MenuEffect

    data class DeleteEpisode(
        val feedIndex: Int,
        val episodeIndex: Int
    ) : MenuEffect

    // -----------------------------
    // Playback effects
    // -----------------------------

    data class StartPlayback(
        val slot: SlotState
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