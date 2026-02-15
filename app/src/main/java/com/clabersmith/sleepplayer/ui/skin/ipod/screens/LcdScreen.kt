package com.clabersmith.sleepplayer.ui.skin.ipod.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.clabersmith.sleepplayer.ui.skin.ipod.components.LcdSurface
import kotlin.math.min

@Composable
fun LcdScreen(
    selectedIndex: Int,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        val baseWidth = 260.dp
        val baseHeight = 200.dp

        val scaleX = maxWidth / baseWidth
        val scaleY = maxHeight / baseHeight
        val scale = min(scaleX, scaleY).coerceAtMost(1.25f)

        LcdSurface(
            modifier = Modifier.size(
                width = baseWidth * scale,
                height = baseHeight * scale
            )
        ) {
            Column {
                Header(title = "SleepPod")
                Spacer(modifier = Modifier.height(8.dp))
                HomeMenu(selectedIndex = selectedIndex)
            }
        }
    }
}