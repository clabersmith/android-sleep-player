package com.clabersmith.sleepplayer.ui.skin.ipod.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clabersmith.sleepplayer.ui.skin.ipod.theme.IpodFontFamily
import com.clabersmith.sleepplayer.ui.skin.ipod.theme.IpodMenuHighlight
import com.clabersmith.sleepplayer.ui.skin.ipod.theme.IpodTextPrimary

@Composable
fun MenuRow(
    text: String,
    selected: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (selected) IpodMenuHighlight else Color.Transparent
            )
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontFamily = IpodFontFamily,
            fontSize = 16.sp,
            color = if (selected) Color.White else IpodTextPrimary
        )

    }
}