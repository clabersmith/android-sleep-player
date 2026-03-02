package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.menu

import androidx.compose.runtime.Composable
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuItem
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuState

@Composable
fun PodcastsMenu(
    state: MenuState.Podcasts
) {
    val items = listOf(
        MenuItem("Play", showChevron = true),
        MenuItem("Downloads", showChevron = true)
    )

    MenuList(
        items = items,
        selectedIndex = state.selectedIndex
    )
}