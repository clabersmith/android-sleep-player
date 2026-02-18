package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.menu

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.device.MenuRow
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuConfig

@Composable
fun MenuList(
    config: MenuConfig
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {

        config.items.forEachIndexed { index, item ->

            val isSelected = index == config.selectedIndex
            Log.d("MenuVM", "Selected: $isSelected for item: $item at index: $index")

            MenuRow(
                text = item,
                selected = isSelected
            )

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}