package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.menu

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.device.MenuRow
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuConfig

@Composable
fun MenuList(
    config: MenuConfig
) {
    val listState = rememberLazyListState()

    LaunchedEffect(config.selectedIndex) {

        val visibleItems = listState.layoutInfo.visibleItemsInfo
        if (visibleItems.isEmpty()) return@LaunchedEffect

        val firstVisible = visibleItems.first().index
        val lastVisible = visibleItems.last().index

        when {
            config.selectedIndex < firstVisible -> {
                listState.scrollToItem(config.selectedIndex)
            }

            config.selectedIndex >= lastVisible -> {
                val targetIndex =
                    (config.selectedIndex - 1).coerceAtLeast(0)

                listState.scrollToItem(targetIndex)
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        itemsIndexed(config.items) { index, item ->

            val isSelected = index == config.selectedIndex

            MenuRow(
                text = item,
                selected = isSelected
            )

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}