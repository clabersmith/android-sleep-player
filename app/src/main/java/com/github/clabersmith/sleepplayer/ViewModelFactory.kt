package com.github.clabersmith.sleepplayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.viewmodel.MenuViewModel
import com.github.clabersmith.sleepplayer.features.podcasts.domain.repository.PodcastRepository

class ViewModelFactory(
    private val repository: PodcastRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MenuViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MenuViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}