package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuState
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.theme.IpodMenuDownloadProgress
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.theme.IpodMenuHighlight
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.theme.IpodMenuText
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.theme.IpodTextPrimary

@Composable
fun NowPlayingMenu(
    state: MenuState.NowPlaying
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {

        // Top header row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "1 of 1",
                style = IpodMenuText,
                color = IpodTextPrimary
            )
        }

        Spacer(Modifier.height(10.dp))

        // Track info
        Text(
            text = state.slot.loadedEpisode.title,
            maxLines = 2,
            style = IpodMenuText,
            color = IpodTextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = state.slot.feedName,
            maxLines = 1,
            style = IpodMenuText,
            color = IpodTextPrimary,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(52.dp))

        // Progress bar
        LcdProgressBar(
            progress = if (state.durationMs == 0L) 0f
            else state.positionMs.toFloat() / state.durationMs
        )

        Spacer(Modifier.height(4.dp))

        // Time row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(state.positionMs),
                style = IpodMenuText,
                color = IpodTextPrimary
            )
            Text(
                text = "-${formatTime(state.durationMs - state.positionMs)}",
                style = IpodMenuText,
                color = IpodTextPrimary
            )
        }
    }
}

@Composable
fun LcdProgressBar(progress: Float) {
    val p = progress.coerceIn(0f, 1f)
    val corner = 5.dp
    val outerShape = androidx.compose.foundation.shape.RoundedCornerShape(corner)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(16.dp)
            .border(1.dp, IpodTextPrimary, outerShape)
            .clip(outerShape)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(p)
                .background(
                    IpodTextPrimary.copy(alpha = 0.65f),
                    shape = if (p >= 1f) outerShape
                    else androidx.compose.foundation.shape.RoundedCornerShape(
                        topStart = corner,
                        bottomStart = corner
                    )
                )
        )
    }
}

fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}