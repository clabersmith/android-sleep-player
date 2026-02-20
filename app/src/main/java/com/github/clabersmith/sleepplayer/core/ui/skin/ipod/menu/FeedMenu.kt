package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.menu

import androidx.compose.runtime.Composable
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuState

@Composable
fun FeedMenu(
    state: MenuState.Feeds
) {
    val items = state.feeds.map { it.title } + "Back"

    MenuList(
        items = items,
        selectedIndex = state.selectedIndex
    )
}