package com.github.clabersmith.sleepplayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.viewmodel.IpodUiViewModel
import com.github.clabersmith.sleepplayer.features.podcasts.domain.repository.PodcastRepository

class IpodUiViewModelFactory(
    private val repository: PodcastRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IpodUiViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return IpodUiViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}