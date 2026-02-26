package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model

/**
 * Reduces a [MenuState] and a [MenuEvent] into a [MenuTransition].
 *
 * Implementations define how menu events transform the current menu state
 * into the next state or transition.
 *
 * @see MenuState
 * @see MenuEvent
 * @see MenuTransition
 */
interface MenuReducer {
    fun reduce(
        state: MenuState,
        event: MenuEvent
    ): MenuTransition
}