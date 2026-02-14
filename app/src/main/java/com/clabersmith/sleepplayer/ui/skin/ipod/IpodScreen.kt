package com.clabersmith.sleepplayer.ui.skin.ipod

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.clabersmith.sleepplayer.ui.skin.ipod.components.IpodClickWheelPlaceholder
import com.clabersmith.sleepplayer.ui.skin.ipod.screens.IpodHomeMenu
import com.clabersmith.sleepplayer.ui.skin.ipod.screens.IpodLcdScreen

@Composable
fun IpodScreen() {
    val darkMode = isSystemInDarkTheme()

    IpodShell(darkMode = darkMode) {

        IpodLcdScreen {
            IpodHomeMenu()
        }

        Spacer(modifier = Modifier.height(90.dp))

        IpodClickWheelPlaceholder(darkMode)
    }
}