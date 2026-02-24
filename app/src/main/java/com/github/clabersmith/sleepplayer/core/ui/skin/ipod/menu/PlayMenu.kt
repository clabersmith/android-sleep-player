package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.menu

import androidx.compose.runtime.Composable
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuState

@Composable
fun PlayMenu(
    state: MenuState.Play
) {
    val slotTitles = state.slots.map {
        it.loadedEpisode.title
    }

    val items = buildList {
        addAll(slotTitles)
    }

    MenuList(
        items = items,
        selectedIndex = state.selectedIndex
    )
}