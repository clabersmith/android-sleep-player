package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.theme.IpodMenuText
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.theme.IpodTextPrimary

@Composable
fun InlineVolumeBar(
    volume: Int,
    selected: Boolean
) {
    val fillFraction = volume / 100f

    Box(
        modifier = Modifier
            .width(120.dp)   // short enough for one row
            .height(20.dp)   // taller to match text
            .border(
                1.dp,
                if (selected) Color.White else IpodTextPrimary,
                RectangleShape
            )
            .clip(RectangleShape)
    ) {

        // Fill
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fillFraction)
                .background(
                    if (selected) Color.White.copy(alpha = 0.8f)
                    else IpodTextPrimary.copy(alpha = 0.65f)
                )
        )

        // Centered text
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = volume.toString(),
                style = IpodMenuText,
                fontSize = 14.sp,

                // Text color flips depending on fill overlap
                color = if (selected) {
                    Color.White.copy(alpha = if (fillFraction > 0.45f) 1f else .85f)
                } else {
                    IpodTextPrimary
                }
            )
        }
    }

    Spacer(Modifier.width(8.dp))
}