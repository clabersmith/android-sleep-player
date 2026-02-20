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
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.ActionRow
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuConfig
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.theme.IpodMenuText

@Composable
fun EpisodeDetailMenu(
    config: MenuConfig,
    actionRows: List<ActionRow>
) {
    val actionStartIndex = 4
    val enabledActions = actionRows.filter { it.enabled }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {

        config.items.forEachIndexed { index, item ->

            when {

                item.isBlank() -> {
                    Spacer(modifier = Modifier.height(6.dp))
                }

                index < actionStartIndex -> {
                    EpisodeDetailTextRow(
                        text = item,
                        isTitle = index == 0
                    )
                }

                else -> {
                    val actionIndex = index - actionStartIndex
                    val actionRow = actionRows.getOrNull(actionIndex)

                    val selectedAction =
                        enabledActions.getOrNull(config.selectedIndex)

                    val isSelected =
                        actionRow != null &&
                                actionRow.enabled &&
                                actionRow == selectedAction

                    if (actionRow != null) {
                        MenuRow(
                            text = actionRow.label,
                            selected = isSelected
                        )
                    }
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