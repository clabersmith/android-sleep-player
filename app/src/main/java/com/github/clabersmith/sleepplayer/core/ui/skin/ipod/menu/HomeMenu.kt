package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.menu

import androidx.compose.runtime.Composable
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuItem
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuState

@Composable
fun HomeMenu(
    state: MenuState.Home
) {
    val items = listOf(
        MenuItem("Podcasts", showChevron = true),
        MenuItem("Play", showChevron = true),
        MenuItem("Settings", showChevron = true)
    )

    MenuList(
        items = items,
        selectedIndex = state.selectedIndex
    )
}