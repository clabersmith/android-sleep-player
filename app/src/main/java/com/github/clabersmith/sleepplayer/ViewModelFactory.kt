package com.github.clabersmith.sleepplayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.viewmodel.MenuViewModel

class ViewModelFactory(
    private val container: AppContainer
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MenuViewModel::class.java)) {

            @Suppress("UNCHECKED_CAST")
            return MenuViewModel(
                podcastRepository = container.podcastRepository,
                slotRepository = container.persistedSlotRepository,
                settingsRepository = container.persistedSettingsRepository,
                downloader = container.downloader,
                storage = container.storage,
                player = container.audioPlayer,
                whiteNoisePlayer = container.whiteNoisePlayer,
                playbackClock = container.playbackClock) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}