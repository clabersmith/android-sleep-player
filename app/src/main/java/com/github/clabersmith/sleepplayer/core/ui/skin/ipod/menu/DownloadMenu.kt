package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.menu

import androidx.compose.runtime.Composable
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuState

@Composable
fun DownloadMenu(
    state: MenuState.Download
) {
    val slotTitles = state.slots.map {
        it.loadedEpisode.title
    }

    val hasAddNew = state.slots.size < state.maxSlots

    val items = buildList {
        addAll(slotTitles)

        if (hasAddNew) {
            add("Add New")
        }
    }

    MenuList(
        items = items,
        selectedIndex = state.selectedIndex
    )
}