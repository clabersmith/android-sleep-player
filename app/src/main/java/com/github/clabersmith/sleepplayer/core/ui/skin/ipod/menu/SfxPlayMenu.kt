package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.menu

import android.util.Log
import androidx.compose.runtime.Composable
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuItem
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuState

@Composable
fun SfxPlayMenu(state: MenuState.SfxPlay) {

    val context = state.context

    val items = context.sfxSlots.map { slot ->
        MenuItem(
            title = slot.fileName.ifBlank { "Empty Slot" },
            isChecked = slot.index == context.activeSfxIndex
        )
    }

    MenuList(
        items = items,
        selectedIndex = state.selectedIndex
    )
}