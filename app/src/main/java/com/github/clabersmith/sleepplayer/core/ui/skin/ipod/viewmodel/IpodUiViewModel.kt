package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.input.WheelEvent
import com.github.clabersmith.sleepplayer.features.podcasts.domain.model.PodcastFeed
import com.github.clabersmith.sleepplayer.features.podcasts.domain.repository.PodcastRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the iPod-style UI.
 *
 * Note: any debug logging in this class needs to not use the android.util.Log class
 * to keep the class easily testable, as Junit test do not run on Android.
 * Instead, we can use println for simplicity, eg. println("Loaded feeds: ${_feeds.value.size}")
 */
class IpodUiViewModel(
    private val podcastRepository: PodcastRepository
) : ViewModel() {

    private val _feeds = MutableStateFlow<List<PodcastFeed>>(emptyList())
    val feeds: StateFlow<List<PodcastFeed>> = _feeds

    init {
        loadFeeds()
    }

    private fun loadFeeds() {
        viewModelScope.launch {
            _feeds.value = podcastRepository.getFeeds()
        }
    }

    private val _selectedIndex = mutableIntStateOf(0)
    val selectedIndex: State<Int> = _selectedIndex

    private val menuSize = 4 // derive from list later
    fun onWheelEvent(event: WheelEvent) {
        when (event) {

            is WheelEvent.Rotate -> {
                val current = _selectedIndex.value

                val next =
                    if (event.delta > 0) current + 1
                    else current - 1

                _selectedIndex.value =
                    (next + menuSize) % menuSize
            }

            else -> Unit
        }
    }

}