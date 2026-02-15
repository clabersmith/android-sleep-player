package com.github.clabersmith.sleepplayer.ui.skin.ipod.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun LcdScanlines(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val lineHeight = 2.dp.toPx()
        val gap = 2.dp.toPx()

        var y = 0f
        while (y < size.height) {
            drawRect(
                color = Color.Black.copy(alpha = 0.025f),
                topLeft = Offset(0f, y),
                size = Size(size.width, lineHeight)
            )
            y += lineHeight + gap
        }
    }
}