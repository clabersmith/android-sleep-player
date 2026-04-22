package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.menu

import androidx.compose.runtime.Composable
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuItem
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuState

@Composable
fun SfxMenu(
    state: MenuState.Sfx
) {
    val items = listOf(
        MenuItem("Download", showChevron = true),
        MenuItem("Play", showChevron = true) // placeholder
    )

    MenuList(
        items = items,
        selectedIndex = state.selectedIndex
    )
}