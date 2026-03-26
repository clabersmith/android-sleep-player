package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.menu

import androidx.compose.runtime.Composable
import com.github.clabersmith.sleepplayer.R
import com.github.clabersmith.sleepplayer.core.playback.WhiteNoiseTrack
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuItem
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuState

@Composable
fun WhiteNoiseMenu(state: MenuState.WhiteNoisePlay) {

    val context = state.context

    val tracks = listOf(
        "Phase" to WhiteNoiseTrack(R.raw.phase),
        "Hypnag" to WhiteNoiseTrack(R.raw.hypnag),
        "Air Conditioner" to WhiteNoiseTrack(R.raw.ac),
        "Box Fan" to WhiteNoiseTrack(R.raw.boxfan),
        "Metal Fan" to WhiteNoiseTrack(R.raw.metalfan),
        "Brown" to WhiteNoiseTrack(R.raw.brown),
        "Pink" to WhiteNoiseTrack(R.raw.pink),
        "Green" to WhiteNoiseTrack(R.raw.green)
    )

    val currentTrack = context.currentWhiteNoiseTrack

    val items = tracks.mapIndexed { index, (title, track) ->
        MenuItem(
            title = title,
            isChecked = (track == currentTrack) // KEY CHANGE
        )
    }

    MenuList(
        items = items,
        selectedIndex = state.selectedIndex
    )
}