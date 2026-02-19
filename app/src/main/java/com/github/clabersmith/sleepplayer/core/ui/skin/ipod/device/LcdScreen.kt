package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.device

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.menu.CategoryMenu
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.menu.DownloadMenu
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.menu.EpisodeDetailMenu
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.menu.EpisodeMenu
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.menu.FeedMenu
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.menu.HomeMenu
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuConfig
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuState
import kotlin.math.min

@Composable
fun LcdScreen(
    menuState: MenuState,
    config: MenuConfig,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        val baseWidth = 260.dp
        val baseHeight = 200.dp

        val scaleX = maxWidth / baseWidth
        val scaleY = maxHeight / baseHeight
        val scale = min(scaleX, scaleY).coerceAtMost(1.25f)

        LcdSurface(
            modifier = Modifier.size(
                width = baseWidth * scale,
                height = baseHeight * scale
            )
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {

                // 🔥 Dynamic header
                Header(title = menuState.title())

                Spacer(modifier = Modifier.height(8.dp))

                // --- Dynamic Menu ---
                when (menuState) {
                    is MenuState.Home -> HomeMenu(config)
                    is MenuState.Downloaded -> DownloadMenu(config)
                    is MenuState.Categories -> CategoryMenu(config)
                    is MenuState.Feeds -> FeedMenu(config)
                    is MenuState.Episodes -> EpisodeMenu(config)
                    is MenuState.EpisodeDetail -> EpisodeDetailMenu(config)
                }
            }
        }
    }
}

private fun MenuState.title(): String {
    return when (this) {
        is MenuState.Home -> "SleepPod"
        is MenuState.Downloaded -> "Downloaded"
        is MenuState.Categories -> "Categories"
        is MenuState.Feeds -> categoryName ?: "Feeds"
        is MenuState.Episodes -> "Episodes"
        is MenuState.EpisodeDetail -> "Episode"
    }
}