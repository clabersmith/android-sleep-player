package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.menu

import androidx.compose.runtime.Composable
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuItem
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuState

@Composable
fun DisplaySettingsMenu(
    state: MenuState.DisplaySettings
) {
    val currentTheme = state.context.displaySettings.theme

    val items = listOf(
        MenuItem(
            title = "White",
            isChecked = currentTheme == MenuState.DisplaySettings.Theme.White
        ),
        MenuItem(
            title = "Black",
            isChecked = currentTheme == MenuState.DisplaySettings.Theme.Black
        ),
        MenuItem(
            title = "Silver",
            isChecked = currentTheme == MenuState.DisplaySettings.Theme.Silver
        ),
        MenuItem(
            title = "Blue",
            isChecked = currentTheme == MenuState.DisplaySettings.Theme.Blue
        ),
        MenuItem(
            title = "Green",
            isChecked = currentTheme == MenuState.DisplaySettings.Theme.Green
        ),
        MenuItem(
            title = "Pink",
            isChecked = currentTheme == MenuState.DisplaySettings.Theme.Pink
        )
    )

    MenuList(
        items = items,
        selectedIndex = state.selectedIndex
    )
}