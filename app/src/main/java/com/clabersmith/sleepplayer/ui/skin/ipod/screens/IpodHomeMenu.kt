package com.clabersmith.sleepplayer.ui.skin.ipod.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.clabersmith.sleepplayer.ui.skin.ipod.components.IpodMenuRow

@Composable
fun IpodHomeMenu() {

    val menuItems = listOf(
        "Download",
        "Play",
        "Settings",
        "Exit"
    )

    Column {
        Text(
            text = "SleepPod",
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        menuItems.forEachIndexed { index, item ->
            IpodMenuRow(
                text = item,
                selected = index == 0
            )
        }
    }
}