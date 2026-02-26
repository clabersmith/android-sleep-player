package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model

/**
 * Represents all inputs to the menu state machine.
 *
 * This includes:
 * <ul>
 *   <li>User input — click wheel and buttons</li>
 *   <li>External input — playback progress updates</li>
 * </ul>
 */
sealed interface MenuEvent {

    // Click wheel rotation
    data class MoveSelection(val delta: Int) : MenuEvent

    // Center button
    data object Confirm : MenuEvent

    // Menu button
    data object MenuShortPress : MenuEvent
    data object MenuLongPress : MenuEvent

    // Play / Pause button
    data object PlayPause : MenuEvent

    // Scan controls
    data object ScanForwardDown : MenuEvent
    data object ScanForwardUp : MenuEvent
    data object ScanBackDown : MenuEvent
    data object ScanBackUp : MenuEvent

    // External playback updates
    data class PlaybackProgress(
        val positionMs: Long,
        val durationMs: Long,
        val isPlaying: Boolean
    ) : MenuEvent
}