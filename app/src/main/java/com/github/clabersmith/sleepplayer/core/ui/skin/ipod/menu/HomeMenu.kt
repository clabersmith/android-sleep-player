package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.menu

import androidx.compose.runtime.Composable
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuItem
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuState
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.viewmodel.NowPlayingUiState

@Composable
fun HomeMenu(
    state: MenuState.Home,
    nowPlayingUiState: NowPlayingUiState
) {

    val items : MutableList<MenuItem> = mutableListOf()

    items.add(
        MenuItem(title = "Podcasts", showChevron = true)
    )

    items.add(
        MenuItem(title = "White Noise", showChevron = true)
    )

    items.add(
        MenuItem(title = "Settings", showChevron = true)
    )

    items.add(
        MenuItem(title = "Extras", showChevron = true)
    )

    if (nowPlayingUiState.slot != null) items.add(
        MenuItem("Now Playing", showChevron = true)
    )


    MenuList(
        items = items,
        selectedIndex = state.selectedIndex
    )
}