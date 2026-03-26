package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.device

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.clabersmith.sleepplayer.R
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuState
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.theme.IpodMenuText
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.theme.IpodTextPrimary
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.viewmodel.NowPlayingUiState
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.viewmodel.WhiteNoiseUiState

@Composable
fun Header(
    menuState: MenuState,
    nowPlayingUiState: NowPlayingUiState,
    whiteNoiseUiState: WhiteNoiseUiState
) {

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
        ) {

            // LEFT ICON GROUP
            Row(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                if (nowPlayingUiState.slot != null) {
                    NowPlayingPlayIcon(
                        isPlaying = nowPlayingUiState.isPlaying,
                        color = IpodTextPrimary,
                        iconSize = 20.dp,
                        modifier = Modifier.padding(end = 4.dp) // spacing AFTER play icon
                    )
                }

                if (whiteNoiseUiState.isPlaying) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_white_noise),
                        contentDescription = "White noise active",
                        modifier = Modifier.size(20.dp).padding(end = 1.dp),
                        tint = IpodTextPrimary
                    )

                    Spacer(modifier = Modifier.width(4.dp)) // spacing AFTER white noise icon
                }
            }

            // CENTER TITLE
            Text(
                text = menuState.title,
                style = IpodMenuText,
                color = IpodTextPrimary,
                modifier = Modifier.align(Alignment.Center)
            )

            // RIGHT BATTERY
            Icon(
                painter = painterResource(R.drawable.ic_ipod_battery_full),
                contentDescription = "Battery",
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 6.dp),
                tint = Color.Unspecified
            )
        }

        HorizontalDivider(
            thickness = 1.dp,
            color = IpodTextPrimary.copy(alpha = 0.4f)
        )
    }
}

@Composable
fun NowPlayingPlayIcon(
    isPlaying: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    iconSize: Dp = 16.dp
) {
    Canvas(
        modifier = modifier.size(iconSize)
    ) {
        val w = size.width
        val h = size.height

        if (isPlaying) {
            // Pause icon (two bars)
            val barWidth = w * 0.22f
            val gap = w * 0.12f

            drawRect(
                color = color,
                topLeft = Offset(w * 0.20f, h * 0.20f),
                size = Size(barWidth, h * 0.60f)
            )

            drawRect(
                color = color,
                topLeft = Offset(w * 0.20f + barWidth + gap, h * 0.20f),
                size = Size(barWidth, h * 0.60f)
            )

        } else {
            // Play icon (triangle — matches ClickWheel style)
            drawPath(
                path = Path().apply {
                    moveTo(w * 0.25f, h * 0.18f)
                    lineTo(w * 0.75f, h * 0.50f)
                    lineTo(w * 0.25f, h * 0.82f)
                    close()
                },
                color = color
            )
        }
    }
}