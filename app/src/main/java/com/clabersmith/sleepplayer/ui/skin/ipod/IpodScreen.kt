package com.clabersmith.sleepplayer.ui.skin.ipod

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.clabersmith.sleepplayer.ui.skin.ipod.components.IpodClickWheelPlaceholder
import com.clabersmith.sleepplayer.ui.skin.ipod.screens.LcdScreen

@Composable
fun IpodScreen() {
    val darkMode = isSystemInDarkTheme()

    IpodShell(darkMode = darkMode) {

        Spacer(modifier = Modifier.height(36.dp))

        LcdScreen()

        Spacer(modifier = Modifier.height(48.dp))

        IpodClickWheelPlaceholder(
            modifier = Modifier.size(190.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}