package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.theme

import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuState

fun MenuState.DisplaySettings.Theme.toIpodTheme(): IpodTheme {
    return when (this) {
        MenuState.DisplaySettings.Theme.White -> IpodThemes.White
        MenuState.DisplaySettings.Theme.Black -> IpodThemes.Black
        MenuState.DisplaySettings.Theme.Silver -> IpodThemes.Silver
        MenuState.DisplaySettings.Theme.Blue -> IpodThemes.Blue
        MenuState.DisplaySettings.Theme.Green -> IpodThemes.Green
        MenuState.DisplaySettings.Theme.Pink -> IpodThemes.Pink
    }
}