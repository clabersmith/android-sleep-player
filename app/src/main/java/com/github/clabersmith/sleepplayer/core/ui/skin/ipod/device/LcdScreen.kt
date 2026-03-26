package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.device

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.unit.IntOffset
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
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.menu.WhiteNoiseMenu
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuState
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.NavDirection
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.viewmodel.NowPlayingUiState
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.viewmodel.WhiteNoiseUiState
import kotlin.math.min

@SuppressLint("UnusedContentLambdaTargetStateParameter")
@Composable
fun LcdScreen(
    menuState: MenuState,
    nowPlayingUiState: NowPlayingUiState,
    whiteNoiseUiState: WhiteNoiseUiState,
    navDirection: NavDirection,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        val baseWidth = 260.dp
        val baseHeight = 220.dp

        val scaleX = maxWidth / baseWidth
        val scaleY = maxHeight / baseHeight

        // Remove the coerceAtMost cap — let it scale freely
        //val scale = min(scaleX, scaleY)

        val scale = min(scaleX, scaleY).coerceAtMost(1.5f)
        val animationKey = menuState::class

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
                    nowPlayingUiState,
                    whiteNoiseUiState
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(modifier = Modifier.fillMaxSize()) {

                    AnimatedContent(
                        targetState = animationKey,
                        transitionSpec = {

                            val offsetAnimationSpec = tween<IntOffset>(
                                durationMillis = 160,
                                easing = FastOutSlowInEasing
                            )

                            val floatAnimationSpec = tween<Float>(
                                durationMillis = 160,
                                easing = FastOutSlowInEasing
                            )

                            when (navDirection) {

                                NavDirection.Forward ->
                                    (
                                            slideInHorizontally(
                                                animationSpec = offsetAnimationSpec,
                                                initialOffsetX = { width -> width }
                                            ) +
                                                    fadeIn(
                                                        animationSpec = floatAnimationSpec,
                                                        initialAlpha = 0.9f
                                                    )
                                            ) togetherWith
                                            (
                                                    slideOutHorizontally(
                                                        animationSpec = offsetAnimationSpec,
                                                        // 👇 parallax effect (moves half distance)
                                                        targetOffsetX = { width -> -width / 2 }
                                                    ) +
                                                            fadeOut(
                                                                animationSpec = floatAnimationSpec,
                                                                targetAlpha = 0.9f
                                                            )
                                                    )

                                NavDirection.Back ->
                                    (
                                            slideInHorizontally(
                                                animationSpec = offsetAnimationSpec,
                                                initialOffsetX = { width -> -width }
                                            ) +
                                                    fadeIn(
                                                        animationSpec = floatAnimationSpec,
                                                        initialAlpha = 0.9f
                                                    )
                                            ) togetherWith
                                            (
                                                    slideOutHorizontally(
                                                        animationSpec = offsetAnimationSpec,
                                                        targetOffsetX = { width -> width / 2 }
                                                    ) +
                                                            fadeOut(
                                                                animationSpec = floatAnimationSpec,
                                                                targetAlpha = 0.9f
                                                            )
                                                    )

                                NavDirection.None ->
                                    fadeIn(floatAnimationSpec) togetherWith
                                            fadeOut(floatAnimationSpec)
                            }
                        }
                    ) {
                        RenderMenu(menuState, nowPlayingUiState)
                    }
                }
            }
        }
    }
}

@Composable
private fun RenderMenu(
    menuState: MenuState,
    nowPlayingUiState: NowPlayingUiState,
) {
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
            nowPlayingUiState
        )

        is MenuState.WhiteNoisePlay -> WhiteNoiseMenu(menuState)
    }
}
