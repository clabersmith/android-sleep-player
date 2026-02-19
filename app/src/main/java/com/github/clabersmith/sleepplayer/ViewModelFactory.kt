package com.github.clabersmith.sleepplayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.viewmodel.MenuViewModel
import com.github.clabersmith.sleepplayer.features.podcasts.domain.repository.PodcastRepository

class ViewModelFactory(
    private val container: AppContainer
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MenuViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MenuViewModel(
                podcastRepository = container.podcastRepository,
                persistedSlotRepository = container.persistedSlotRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}