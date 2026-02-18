package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.menu

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.device.MenuRow
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuConfig
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.theme.IpodMenuText

@Composable
fun EpisodeDetailMenu(
    config: MenuConfig
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {

        config.items.forEachIndexed { index, item ->

            val isBackRow = index == config.items.lastIndex
            val isSelected = isBackRow && index == config.selectedIndex

            when {
                item.isBlank() -> {
                    Spacer(modifier = Modifier.height(6.dp))
                }

                isBackRow -> {
                    MenuRow(
                        text = item,
                        selected = isSelected
                    )
                }

                else -> {
                    // Title + description rows
                    EpisodeDetailTextRow(
                        text = item,
                        isTitle = index == 0
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun EpisodeDetailTextRow(
    text: String,
    isTitle: Boolean
) {
    Text(
        text = text,
        style = if (isTitle)
            IpodMenuText
        else
            IpodMenuText.copy(
                fontSize = IpodMenuText.fontSize * 0.8f
            ),
        color = MaterialTheme.colorScheme.onBackground.copy(
            alpha = if (isTitle) 1f else 0.75f
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp)
    )
}