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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.clabersmith.sleepplayer.R
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuState
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.theme.IpodMenuText
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.theme.IpodTextPrimary
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.viewmodel.NowPlayingUiState
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.viewmodel.NowPlayingUiState.NowPlayingBarMode.TrackPosition
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.viewmodel.NowPlayingUiState.NowPlayingBarMode.Volume

@Composable
fun NowPlayingMenu(
    state: MenuState.NowPlaying,
    nowPlayingUiState: NowPlayingUiState
) {

    println("loading with slot ${state.slot} and playback state ${nowPlayingUiState.slot}")

    val isActiveTrack =
        nowPlayingUiState.slot == state.slot

    val position =
        if (isActiveTrack) nowPlayingUiState.positionMs else 0L

    val duration =
        if (isActiveTrack) nowPlayingUiState.durationMs else 0L

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

        // push progress/time to bottom
        Spacer(Modifier.weight(1f))

        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            // Track progress or volume bar
            when (nowPlayingUiState.barMode) {
                TrackPosition -> TrackProgressBar(
                    nowPlayingUiState.durationMs,
                    nowPlayingUiState.positionMs)

                Volume -> VolumeBar(nowPlayingUiState.volume)
            }
        }

    }
}

@Composable
private fun TrackProgressBar(duration: Long, position: Long) {
    LcdProgressBar(
        progress = if (duration == 0L) 0f
        else position.toFloat() / duration
    )

    Spacer(Modifier.height(4.dp))

    // Time row
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = formatTime(position),
            style = IpodMenuText,
            color = IpodTextPrimary
        )
        Text(
            text = "-${formatTime(duration - position)}",
            style = IpodMenuText,
            color = IpodTextPrimary
        )
    }
}

@Composable
fun LcdProgressBar(progress: Float) {
    val p = progress.coerceIn(0f, 1f)
    val corner = 10.dp
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

@Composable
fun VolumeBar(volume: Int) {

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painterResource(R.drawable.ic_ipod_volume_low),
            contentDescription = null,
            tint = Color.Unspecified
        )

        Box(
            modifier = Modifier
                //.fillMaxWidth()
                .weight(1f)
                .height(16.dp)
                .border(1.dp, IpodTextPrimary, RectangleShape)
                .clip(RectangleShape)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(volume / 100f)
                    .background(
                        IpodTextPrimary.copy(alpha = 0.65f)
                    )
            )
        }

        Spacer(Modifier.width(8.dp))

        Icon(
            painterResource(R.drawable.ic_ipod_volume_high),
            contentDescription = null,
            tint = Color.Unspecified
        )
    }

    Spacer(Modifier.height(20.dp))
}

fun formatTime(ms: Long): String {
    if(ms <= 0L) return "00:00"

    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}