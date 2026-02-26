package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model

/**
 * Result of reducing a state + event.
 *
 * Contains:
 * - The new state
 * - Any side effects that must be executed
 */
data class MenuTransition(
    val newState: MenuState,
    val effects: List<MenuEffect> = emptyList()
)