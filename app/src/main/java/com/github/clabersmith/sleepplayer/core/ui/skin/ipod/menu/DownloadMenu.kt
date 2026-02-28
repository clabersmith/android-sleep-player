package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.menu

import androidx.compose.runtime.Composable
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuState

@Composable
fun DownloadMenu(
    state: MenuState.Download
) {
    val slotTitles = state.context.slots.map {
        it.loadedEpisode.title
    }

    val hasAddNew = state.context.slots.size < state.context.maxSlotsCount

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