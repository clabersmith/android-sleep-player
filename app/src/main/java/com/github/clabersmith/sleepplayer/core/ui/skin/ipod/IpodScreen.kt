package com.github.clabersmith.sleepplayer.core.ui.skin.ipod

import android.media.SoundPool
import android.util.Log
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.github.clabersmith.sleepplayer.R
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.device.ClickWheel
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.device.LcdScreen
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.theme.toIpodTheme
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.viewmodel.MenuViewModel

@Composable
fun IpodScreen(
    viewModel: MenuViewModel
) {
    val menuState by viewModel.menuState.collectAsState()
    val nowPlayingUiState by viewModel.nowPlayingUiState.collectAsState()
    val whiteNoiseUiState by viewModel.whiteNoiseUiState.collectAsState()
    val sfxUIState by viewModel.sfxUiState.collectAsState()
    val navDirection by viewModel.navDirection.collectAsState()

    // resolve full theme
    val theme = menuState.context.displaySettings.theme.toIpodTheme()

    val context = LocalContext.current

    val soundPool = remember {
        SoundPool.Builder()
            .setMaxStreams(4)
            .build()
    }

    val clickSoundId = remember {
        soundPool.load(context, R.raw.ipod_click, 1)
    }

    var size by remember { mutableStateOf(320f) }

    DisposableEffect(soundPool) {
        onDispose {
            soundPool.release()
        }
    }

    val audioSettings = menuState.context.audioSettings
    val currentAudioSettings by rememberUpdatedState(audioSettings)

    fun playClick() {
        if (!currentAudioSettings.clickEnabled) return

        soundPool.play(
            clickSoundId,
            .1f,
            .1f,
            0,
            0,
            1f
        )
    }

    IpodShell(theme = theme) {

        Spacer(Modifier.height(24.dp))

        LcdScreen(
            menuState = menuState,
            nowPlayingUiState = nowPlayingUiState,
            whiteNoiseUiState = whiteNoiseUiState,
            sfxUIState = sfxUIState,
            navDirection = navDirection
        )

        Spacer(Modifier.height(36.dp))

        ClickWheel(
            bodyColor = theme.bodyColor,
            wheelColor = theme.clickWheelColor,
            textColor = theme.clickWheelTextColor,
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
                viewModel.stopPlaybackCompletely()
                viewModel.stopWhiteNoise()
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

        Spacer(Modifier.height(32.dp))
    }
}