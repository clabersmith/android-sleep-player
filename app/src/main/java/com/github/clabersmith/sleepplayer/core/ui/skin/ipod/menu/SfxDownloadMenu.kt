package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.menu

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuItem
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuState
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.theme.IpodMenuText
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.theme.IpodTextPrimary
import com.github.clabersmith.sleepplayer.core.util.formatTimestamp

@Composable
fun SfxDownloadMenu(
    state: MenuState.SfxDownload
) {
    val context = state.context
    val status = state.status

    val lastUpdated = context.sfxLastUpdatedAt
    val hasDownloads = lastUpdated != null

    val textLines = buildList {

        when {
            status.isDownloading -> {
                add(status.message)
            }

            status.message.isNotBlank() -> {
                // Show feedback AFTER pressing Download
                add(status.message)

                if (hasDownloads) {
                    add("")
                    add("Last update: ${formatTimestamp(lastUpdated!!)}")
                }
            }

            hasDownloads -> {
                add("Last update: ${formatTimestamp(lastUpdated!!)}")
            }

            else -> {
                add("No downloads yet")
            }
        }
    }

    val items = listOf(
        MenuItem("Download")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {

        textLines.forEach { line ->
            Text(
                text = line,
                style = IpodMenuText.copy(fontSize = IpodMenuText.fontSize * 0.8f),
                color = IpodTextPrimary.copy(alpha = 0.8f),
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        MenuList(
            items = items,
            selectedIndex = state.selectedIndex
        )
    }
}