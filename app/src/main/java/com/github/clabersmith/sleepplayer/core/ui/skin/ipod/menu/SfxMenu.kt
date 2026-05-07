package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.menu

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuItem
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuState
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.theme.IpodMenuText
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.theme.IpodTextPrimary

@Composable
fun SfxMenu(
    state: MenuState.Sfx // or whatever your menu is called
) {
    val context = state.context

    val isWhiteNoiseActive = context.currentWhiteNoiseTrack != null

    val items = listOf(
        MenuItem(
            title = "Download"
        ),
        MenuItem(
            title = "Play",
            isDisabled = !isWhiteNoiseActive
        )
    )

    val description = when (state.selectedIndex) {
        0 -> "FX files are updated nightly. Choose download to get the current ones."
        1 -> "FX files are playable with white noise. Select a white noise option first."
        else -> ""
    }

    Column(modifier = Modifier.fillMaxSize()) {

        MenuList(
            items = items,
            selectedIndex = state.selectedIndex,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = description,
            style = IpodMenuText,
            fontSize = 12.sp,
            color = IpodTextPrimary.copy(alpha = 0.7f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
    }
}