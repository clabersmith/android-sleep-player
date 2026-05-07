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
            title = "Podcast Volume",
            volume = settings.defaultPodcastVolume
        ),
        MenuItem(
            title = "Noise Volume",
            volume = settings.defaultWhiteNoiseVolume
        ),
        MenuItem(
            title = "FX Volume", // ADD
            volume = settings.defaultSfxVolume
        )
    )

    MenuList(
        items = items,
        selectedIndex = state.selectedIndex
    )
}