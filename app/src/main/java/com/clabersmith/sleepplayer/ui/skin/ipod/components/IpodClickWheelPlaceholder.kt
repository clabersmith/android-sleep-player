package com.clabersmith.sleepplayer.ui.skin.ipod.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun IpodClickWheelPlaceholder(
    modifier: Modifier = Modifier
) {
    val wheelColor = Color(0xFFD6D6D6)

    Box(
        modifier = modifier
            .background(wheelColor, CircleShape)
    )
}