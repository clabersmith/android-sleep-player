package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuRow
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.ActionRow
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuItem
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuState
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.theme.IpodMenuDownloadProgress
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.theme.IpodMenuText
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.theme.IpodTextPrimary

@Composable
fun EpisodeDetailMenu(
    state: MenuState.EpisodeDetail
) {
    val episode = state.episode

    //display a good preview of the description, truncating if it's too long
    val descriptionPreview = if (episode.description.length > 200) {
        episode.description.take(200)
    } else {
        episode.description
    }

    val staticItems = listOf(
        episode.title,
        "",
        descriptionPreview,
        ""
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {

        // ----------------------------
        // Static Episode Info
        // ----------------------------
        staticItems.forEachIndexed { index, item ->

            when {
                item.isBlank() -> {
                    Spacer(modifier = Modifier.height(6.dp))
                }

                else -> {
                    EpisodeDetailTextRow(
                        text = item,
                        isTitle = index == 0
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
        }

        // ----------------------------
        // Action Rows
        // ----------------------------
        state.actionRows.forEachIndexed { index, row ->

            val isSelected = index == state.selectedIndex

            when (row) {

                is ActionRow.Download -> {
                    MenuRow(
                        item = MenuItem(title = "Download"),
                        selected = isSelected
                    )
                }

                is ActionRow.Downloading -> {
                    DownloadProgressRow(
                        progress = row.progress
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                }

                is ActionRow.Cancel -> {
                    MenuRow(
                        item = MenuItem(title = "Cancel"),
                        selected = isSelected
                    )
                }

                is ActionRow.Delete -> {
                    MenuRow(
                        item = MenuItem(title = "Delete"),
                        selected = isSelected
                    )
                }

                is ActionRow.AlreadyDownloaded -> {
                    MenuRow(
                        item = MenuItem(title = "Already Downloaded"),
                        selected = false
                    )
                }
            }
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
        color = IpodTextPrimary.copy(
            alpha = if (isTitle) 1f else 0.75f
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp)
    )
}

@Composable
fun DownloadProgressRow(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(24.dp)
    ) {

        // Filling highlight background
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .background(
                    IpodMenuDownloadProgress.copy(alpha = 0.6f)
                )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 4.dp)
        ) {
            Text(
                text = "Download",
                style = IpodMenuText,
                color = IpodTextPrimary.copy(alpha = 0.6f),
            )
        }
    }
}

