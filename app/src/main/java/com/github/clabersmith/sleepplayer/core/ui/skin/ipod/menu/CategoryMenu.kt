package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.menu

import androidx.compose.runtime.Composable
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuItem
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuState

@Composable
fun CategoryMenu(
    state: MenuState.Categories
) {
    val items: List<MenuItem> =
        state.context.categories.map { MenuItem(title = it, showChevron = true) }

    MenuList(
        items = items,
        selectedIndex = state.selectedIndex
    )
}