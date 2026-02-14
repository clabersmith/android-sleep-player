package com.clabersmith.sleepplayer.ui.skin.ipod

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun IpodShell(
    darkMode: Boolean,
    content: @Composable () -> Unit
) {
    val baseColor = if (darkMode) Color(0xFF1E1E1E) else Color(0xFFF2F2F2)
    val topAlpha = if (darkMode) 0.05f else 0.07f
    val bloomAlpha = if (darkMode) 0.035f else 0.05f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(baseColor)
            .drawWithContent {
                drawContent()

                // ─────────────────────────────
                // Soft top highlight
                // ─────────────────────────────
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = topAlpha),
                            Color.Transparent
                        ),
                        endY = size.height * 0.25f
                    )
                )

                // ─────────────────────────────
                // Edge bloom (subtle inner glow)
                // ─────────────────────────────
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = bloomAlpha),
                            Color.Transparent
                        ),
                        radius = size.minDimension * 0.75f,
                        center = Offset(size.width / 2f, size.height / 2f)
                    )
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxHeight(0.85f)
                .fillMaxWidth(0.85f)
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            content()

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}