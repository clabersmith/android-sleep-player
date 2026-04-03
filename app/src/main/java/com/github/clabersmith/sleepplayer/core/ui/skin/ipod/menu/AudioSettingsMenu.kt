package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.menu

import androidx.compose.runtime.Composable
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuItem
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuState

@Composable
fun AudioSettingsMenu(
    state: MenuState.AudioSettings
) {
    val settings = state.context.audioSettings

    val items = listOf(
        MenuItem(
            title = "Click Wheel Sound",
            isChecked = settings.clickEnabled
        ),
        MenuItem(
            title = "Master Volume",
            volume = settings.masterVolume
        )
    )

    MenuList(
        items = items,
        selectedIndex = state.selectedIndex
    )
}