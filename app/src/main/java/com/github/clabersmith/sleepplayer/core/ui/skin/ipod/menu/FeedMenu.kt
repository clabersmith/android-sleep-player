package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.menu

import androidx.compose.runtime.Composable
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuConfig

@Composable
fun FeedMenu(
    config: MenuConfig
) {
    MenuList(
        config = config
    )
}