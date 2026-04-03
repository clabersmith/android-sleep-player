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
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuRow
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuItem

@Composable
fun MenuList(
    items: List<MenuItem>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    customRow: (@Composable (Int, MenuItem, Boolean) -> Unit)? = null
) {
    val listState = rememberLazyListState()

    LaunchedEffect(selectedIndex) {
        val visibleItems = listState.layoutInfo.visibleItemsInfo
        if (visibleItems.isEmpty()) return@LaunchedEffect

        val firstVisible = visibleItems.first().index
        val lastVisible = visibleItems.last().index

        when {
            selectedIndex < firstVisible -> listState.scrollToItem(selectedIndex)
            selectedIndex >= lastVisible -> {
                val targetIndex = (selectedIndex - 1).coerceAtLeast(0)
                listState.scrollToItem(targetIndex)
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        itemsIndexed(items) { index, item ->

            val selected = index == selectedIndex

            if (customRow != null) {
                customRow(index, item, selected)
            } else {
                MenuRow(item, selected)
            }

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}