package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.menu

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuItem
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuState
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.theme.IpodMenuText
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.theme.IpodTextPrimary

@Composable
fun PlaybackSettingsMenu(
    state: MenuState.PlaybackSettings
) {
    val settings = state.context.playbackSettings

    val items: List<MenuItem> = listOf(
        MenuItem(
            title = "Fade Level",
            value = "${settings.duckVolumePercent}%"
        ),
        MenuItem(
            title = "Fade Starts After",
            value = settings.autoFadeMinutes?.let { "$it min" } ?: "None"
        ),
        MenuItem(
            title = "Stop Playback After",
            value = settings.autoStopMinutes?.let { "$it min" } ?: "None"
        )
    )

    val description = when (state.selectedIndex) {
        0 -> "Adjust how much white noise volume lowers during playback."
        1 -> "After this time, white noise fades in while playback fades out."
        2 -> "Automatically stops playback after selected time."
        else -> ""
    }

    Column(modifier = Modifier.fillMaxSize()) {

        MenuList(
            items = items,
            selectedIndex = state.selectedIndex,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = description,
            style = IpodMenuText,
            fontSize = 12.sp,
            color = IpodTextPrimary.copy(alpha = 0.7f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
    }
}