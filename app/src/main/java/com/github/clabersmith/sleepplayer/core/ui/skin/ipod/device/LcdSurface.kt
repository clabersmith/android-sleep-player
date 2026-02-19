package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.device


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun LcdSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .border(
                width = 2.dp,
                color = Color(0xFF1A1A1A),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(1.dp)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.18f),
                shape = RoundedCornerShape(7.dp)
            )
            .clip(RoundedCornerShape(6.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFDDE8F6),
                        Color(0xFFBFD1EA)
                    )
                )
            )
            .background(Color.White.copy(alpha = 0.12f))
            .drawBehind {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.16f),
                            Color.Black.copy(alpha = 0.06f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = size.height * 0.16f
                    )
                )
            }
            .padding(8.dp)
    ) {
        content()
        LcdScanlines(Modifier.matchParentSize())
        LcdGlassOverlay(Modifier.matchParentSize())
    }
}