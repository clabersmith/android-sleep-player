package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.menu

import androidx.compose.runtime.Composable
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuItem
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuState

@Composable
fun EpisodeMenu(
    state: MenuState.Episodes
) {
    val items: List<MenuItem>  =
        state.episodes.map { MenuItem(title = it.title) }

    MenuList(
        items = items,
        selectedIndex = state.selectedIndex
    )
}