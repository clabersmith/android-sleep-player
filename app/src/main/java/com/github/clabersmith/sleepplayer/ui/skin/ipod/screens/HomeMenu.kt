package com.github.clabersmith.sleepplayer.ui.skin.ipod.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import com.github.clabersmith.sleepplayer.ui.skin.ipod.components.MenuRow

@Composable
fun HomeMenu(
    selectedIndex: Int
) {

    val menuItems = listOf(
        "Download",
        "Play",
        "Settings",
        "Exit"
    )

    Column {
        menuItems.forEachIndexed { index, item ->
            MenuRow(
                text = item,
                selected = index == selectedIndex
            )
        }
    }
}