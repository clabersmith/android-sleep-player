package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.menu

import androidx.compose.runtime.Composable
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuItem
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuState

@Composable
fun SettingsMenu(
    state: MenuState.Settings
) {
    val settings = state.context.playbackSettings

    val items: List<MenuItem> = listOf(
        MenuItem(
            title = "Percent to Fade White Noise for Playback",
            value = "${settings.duckVolumePercent}%"
        ),
        MenuItem(
            title = "Minutes to Start Fade Out of Playback",
            value = settings.autoFadeMinutes?.let { "$it m" } ?: "None"
        ),
        MenuItem(
            title = "Minutes to Stop Playback",
            value = settings.autoStopMinutes?.let { "$it m" } ?: "None"
        )
    )

    MenuList(
        items = items,
        selectedIndex = state.selectedIndex
    )
}