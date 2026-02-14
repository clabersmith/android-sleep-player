package com.clabersmith.sleepplayer.ui.skin.ipod.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun LcdGlassOverlay(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.25f),
                        Color.White.copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = 120f
                )
            )
    )
}
