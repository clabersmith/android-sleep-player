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
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.menu.NowPlayingMenu
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.menu.PlayMenu
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.menu.PodcastsMenu
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuState
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.viewmodel.NowPlayingUiState
import kotlin.math.min

@Composable
fun LcdScreen(
    menuState: MenuState,
    nowPlayingUiState: NowPlayingUiState,
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
        val scale = min(scaleX, scaleY).coerceAtMost(1.3f)

        LcdSurface(
            modifier = Modifier.size(
                width = baseWidth * scale,
                height = baseHeight * scale
            )
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {

                // Dynamic header
                Header(
                    menuState,
                    nowPlayingUiState
                )

                Spacer(modifier = Modifier.height(8.dp))

                // --- Dynamic Menu ---
                when (menuState) {
                    is MenuState.Home -> HomeMenu(
                        menuState,
                        nowPlayingUiState
                    )
                    is MenuState.Podcasts -> PodcastsMenu(menuState)
                    is MenuState.Download -> DownloadMenu(menuState)
                    is MenuState.Categories -> CategoryMenu(menuState)
                    is MenuState.Feeds -> FeedMenu(menuState)
                    is MenuState.Episodes -> EpisodeMenu(menuState)
                    is MenuState.EpisodeDetail -> {
                        EpisodeDetailMenu(menuState)
                    }
                    is MenuState.Play -> PlayMenu(menuState)
                    is MenuState.NowPlaying -> NowPlayingMenu(
                        menuState,
                        nowPlayingUiState)
                }
            }
        }
    }
}