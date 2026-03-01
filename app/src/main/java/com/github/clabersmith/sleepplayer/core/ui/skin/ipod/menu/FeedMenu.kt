package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.menu

import androidx.compose.runtime.Composable
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuItem
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuState

@Composable
fun FeedMenu(
    state: MenuState.Feeds
) {
    val items: List<MenuItem>  =
        state.categoryFeeds.map { MenuItem(title = it.title, showChevron = true) }

    MenuList(
        items = items,
        selectedIndex = state.selectedIndex
    )
}