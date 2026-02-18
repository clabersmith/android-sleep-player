package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.menu

import androidx.compose.runtime.Composable
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuConfig

@Composable
fun HomeMenu(
    config: MenuConfig
) {
    MenuList(
        config = config
    )
}