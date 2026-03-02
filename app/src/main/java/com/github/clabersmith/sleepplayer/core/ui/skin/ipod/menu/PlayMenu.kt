package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.menu

import androidx.compose.runtime.Composable
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.device.MenuRow
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuItem
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuState

@Composable
fun PlayMenu(
    state: MenuState.Play
) {
    val slotTitles: List<MenuItem> = state.context.slots.map {
        MenuItem(title = it.loadedEpisode.title)
    }

    val items = buildList {
        addAll(slotTitles)
    }

    if (items.isEmpty()) {
        MenuRow(
            item = MenuItem(title = "No Podcasts"),
            selected = true
        )
    } else {
        MenuList(
            items = items,
            selectedIndex = state.selectedIndex
        )
    }
}