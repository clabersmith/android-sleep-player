package com.github.clabersmith.sleepplayer.core.ui.skin.ipod

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun IpodShell(
    darkMode: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    val baseColor = if (darkMode) Color(0xFF1E1E1E) else Color(0xFFF2F2F2)
    val topAlpha = 0.4f
    val bloomAlpha = 0.4f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(baseColor)
            .drawWithContent {
                drawContent()
                // Single-pass plastic effect
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.14f),   // top gloss
                            Color.Transparent,                 // mid body
                            Color.Black.copy(alpha = 0.06f)    // bottom depth
                        )
                    )
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // scale entire content to 85% of the shell
        Box(
            modifier = Modifier.fillMaxSize(0.85f),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier.fillMaxSize(), // now fills the scaled box
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                content()
            }
        }
    }
}