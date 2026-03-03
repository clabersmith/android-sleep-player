package com.github.clabersmith.sleepplayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.viewmodel.MenuViewModel
import kotlinx.coroutines.Dispatchers

class ViewModelFactory(
    private val container: AppContainer
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MenuViewModel::class.java)) {

            @Suppress("UNCHECKED_CAST")
            return MenuViewModel(
                podcastRepository = container.podcastRepository,
                slotRepository = container.persistedSlotRepository,
                downloader = container.downloader,
                storage = container.storage,
                player = container.audioPlayer) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}