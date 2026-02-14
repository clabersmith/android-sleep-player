package com.clabersmith.sleepplayer.ui.skin.ipod.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun IpodClickWheelPlaceholder(
    darkMode: Boolean
) {
    val bodyColor = if (darkMode) Color(0xFF1C1C1C) else Color(0xFFF2F2F2)

    Box(
        modifier = Modifier
            .size(200.dp)
            .background(bodyColor, CircleShape)
    )
}