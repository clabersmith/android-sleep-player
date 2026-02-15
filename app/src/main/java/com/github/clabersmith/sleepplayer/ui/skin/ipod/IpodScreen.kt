package com.github.clabersmith.sleepplayer.ui.skin.ipod

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.clabersmith.sleepplayer.ui.skin.ipod.components.ClickWheel
import com.github.clabersmith.sleepplayer.ui.skin.ipod.screens.LcdScreen
import com.github.clabersmith.sleepplayer.ui.skin.ipod.viewmodel.IpodUiViewModel
import com.github.clabersmith.sleepplayer.ui.skin.ipod.theme.IpodBodyColorDark
import com.github.clabersmith.sleepplayer.ui.skin.ipod.theme.IpodBodyColorLight
import com.github.clabersmith.sleepplayer.ui.skin.ipod.theme.IpodClickWheelTextColorDark
import com.github.clabersmith.sleepplayer.ui.skin.ipod.theme.IpodClickWheelTextColorLight


@Composable
fun IpodScreen(
        viewModel: IpodUiViewModel
) {
    val darkMode = isSystemInDarkTheme()
    val bodyColor = if(darkMode) IpodBodyColorDark else IpodBodyColorLight
    val wheelTextColor = if (darkMode) IpodClickWheelTextColorDark else
        IpodClickWheelTextColorLight


    IpodShell(darkMode = darkMode) {

        Spacer(Modifier.height(24.dp))

        LcdScreen(
            selectedIndex = viewModel.selectedIndex.value
        )

        Spacer(Modifier.height(36.dp))

        ClickWheel(
            bodyColor = bodyColor,
            textColor = wheelTextColor,
            modifier = Modifier.size(280.dp),
            onEvent = viewModel::onWheelEvent
        )

        Spacer(Modifier.height(32.dp))
    }
}