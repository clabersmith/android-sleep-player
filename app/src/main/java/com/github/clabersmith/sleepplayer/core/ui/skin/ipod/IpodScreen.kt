package com.github.clabersmith.sleepplayer.core.ui.skin.ipod

import android.media.SoundPool
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.github.clabersmith.sleepplayer.R
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.device.ClickWheel
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
    val nowPlayingUiState by viewModel.nowPlayingUiState.collectAsState()

    val context = LocalContext.current

    val soundPool = remember {
        SoundPool.Builder()
            .setMaxStreams(4)
            .build()
    }

    val clickSoundId = remember {
        soundPool.load(context, R.raw.ipod_click, 1)
    }

    var size by remember { mutableStateOf(280f) }

    DisposableEffect(soundPool) {
        onDispose {
            soundPool.release()
        }
    }

    fun playClick() {
        soundPool.play(
            clickSoundId,
            1f, // left volume
            1f, // right volume
            0,  // priority
            0,  // loop
            1f  // playback rate
        )
    }

    IpodShell(darkMode = darkMode) {

        Spacer(Modifier.height(24.dp))

        LcdScreen(
            menuState = menuState,
            nowPlayingUiState = nowPlayingUiState
        )

        Spacer(Modifier.height(36.dp))

        ClickWheel(
            bodyColor = bodyColor,
            textColor = wheelTextColor,
            modifier = Modifier.size(size.dp),

            onRotate = { delta ->
                viewModel.moveSelection(delta)
                playClick()
            },

            onConfirm = {
                viewModel.confirmSelection()
                playClick()
            },

            onMenuLongPress = {
                viewModel.onMenuLongPress()
                playClick()
            },

            onMenuShortPress = {
                viewModel.onMenuShortPress()
                playClick()
            },
            onPlayPauseShortPress = {
                viewModel.onPlayPauseShortPressed()
                playClick()
            },
            onPlayPauseLongPress = {
                viewModel.onPlayPauseLongPressed()
                playClick()
            },
            onScanForwardDown = {
                viewModel.onScanForwardDown()
                playClick()
            },
            onScanForwardUp = {
                viewModel.onScanForwardUp()
                playClick()
            },
            onScanBackDown = {
                viewModel.onScanBackDown()
                playClick()
            },
            onScanBackUp = {
                viewModel.onScanBackUp()
                playClick()
            }
        )
//        Slider(
//            value = size,
//            onValueChange = { size = it },
//            valueRange = 100f..400f
//        )
//        Text("Size: ${size.toInt()}dp")

        Spacer(Modifier.height(32.dp))
    }
}

