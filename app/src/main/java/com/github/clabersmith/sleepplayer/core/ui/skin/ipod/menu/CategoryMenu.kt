package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.menu

import androidx.compose.runtime.Composable
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuState

@Composable
fun CategoryMenu(
    state: MenuState.Categories
) {
        val items = state.categories + "Back"

    MenuList(
        items = items,
        selectedIndex = state.selectedIndex
    )
}