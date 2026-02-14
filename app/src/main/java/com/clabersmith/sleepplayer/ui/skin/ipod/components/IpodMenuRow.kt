package com.clabersmith.sleepplayer.ui.skin.ipod.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun IpodMenuRow(
    text: String,
    selected: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (selected) Color.Black else Color.Transparent
            )
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else Color.Black
        )
    }
}