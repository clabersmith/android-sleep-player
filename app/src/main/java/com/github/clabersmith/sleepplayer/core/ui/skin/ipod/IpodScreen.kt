package com.github.clabersmith.sleepplayer.core.ui.skin.ipod

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.components.ClickWheel
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.device.LcdScreen
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.theme.IpodBodyColorDark
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.theme.IpodBodyColorLight
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.theme.IpodClickWheelTextColorDark
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.theme.IpodClickWheelTextColorLight
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.viewmodel.MenuViewModel


@Composable
fun IpodScreen(
    viewModel: MenuViewModel
) {
    val darkMode = isSystemInDarkTheme()

    val bodyColor =
        if (darkMode) IpodBodyColorDark else IpodBodyColorLight

    val wheelTextColor =
        if (darkMode) IpodClickWheelTextColorDark
        else IpodClickWheelTextColorLight

    val menuState by viewModel.menuState.collectAsState()
    val config by viewModel.menuConfig.collectAsState()

    IpodShell(darkMode = darkMode) {

        Spacer(Modifier.height(24.dp))

        LcdScreen(
            menuState = menuState,
            config = config
        )

        Spacer(Modifier.height(36.dp))

        ClickWheel(
            bodyColor = bodyColor,
            textColor = wheelTextColor,
            modifier = Modifier.size(280.dp),

            onRotate = { delta ->
                viewModel.moveSelection(delta)
            },

            onConfirm = {
                viewModel.confirmSelection()
            },

            onBack = {
                viewModel.onBack()
            }
        )

        Spacer(Modifier.height(32.dp))
    }
}