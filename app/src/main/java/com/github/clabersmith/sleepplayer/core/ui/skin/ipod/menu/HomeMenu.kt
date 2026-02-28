package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.menu

import androidx.compose.runtime.Composable
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuState

@Composable
fun HomeMenu(
    state: MenuState.Home
) {
    val items = listOf(
        "Podcasts",
        "Play",
        "Settings"
    )

    MenuList(
        items = items,
        selectedIndex = state.selectedIndex
    )
}