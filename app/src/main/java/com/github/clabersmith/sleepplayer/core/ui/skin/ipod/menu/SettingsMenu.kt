package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.menu

import androidx.compose.runtime.Composable
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuItem
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuState

@Composable
fun SettingsMenu(
    state: MenuState.Settings
) {
    val items = listOf(
        MenuItem(
            title = "Playback",
            showChevron = true
        )
    )

    MenuList(
        items = items,
        selectedIndex = state.selectedIndex
    )
}