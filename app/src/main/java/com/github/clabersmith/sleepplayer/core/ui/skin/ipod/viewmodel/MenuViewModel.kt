package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuConfig
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuState
import com.github.clabersmith.sleepplayer.features.podcasts.domain.model.PodcastFeed
import com.github.clabersmith.sleepplayer.features.podcasts.domain.repository.PodcastRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
class MenuViewModel(
    private val repository: PodcastRepository
) : ViewModel() {

    private val _menuState = MutableStateFlow<MenuState>(MenuState.Home())
    val menuState: StateFlow<MenuState> = _menuState

    private val _feeds = MutableStateFlow<List<PodcastFeed>>(emptyList())

    private val _categories = MutableStateFlow<List<String>>(emptyList())

    private val _downloaded = MutableStateFlow<List<PodcastFeed>>(emptyList())

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _feeds.value = repository.getFeeds()
            _categories.value = repository.getCategories()
        }
    }

    // -----------------------------
    // MenuConfig exposed to UI
    // -----------------------------

    val menuConfig: StateFlow<MenuConfig> =
        combine(
            _menuState,
            _feeds,
            _categories,
            _downloaded
        ) { state, feeds, categories, downloaded ->

            when (state) {

                is MenuState.Home -> {
                    MenuConfig(
                        items = listOf(
                            "Download",
                            "Play",
                            "Settings",
                            "Exit"
                        ),
                        selectedIndex = state.selectedIndex
                    )
                }

                is MenuState.Downloaded -> {

                    val downloaded = _downloaded.value
                    val items = mutableListOf<String>()

                    downloaded.take(4).forEachIndexed { index, _ ->
                        items.add("Slot ${index + 1}")
                    }

                    if (downloaded.size < 4) {
                        items.add("Add New")
                    }

                    items.add("Back")

                    MenuConfig(
                        items = items,
                        selectedIndex = state.selectedIndex
                    )
                }

                is MenuState.Categories -> {
                    val sorted = categories.distinct().sorted()
                    MenuConfig(
                        items = sorted + "Back",
                        selectedIndex = state.selectedIndex
                    )
                }

                is MenuState.Feeds -> {
                    val filtered = feeds.filter {
                        state.categoryName == null ||
                                it.category.equals(
                                    state.categoryName,
                                    ignoreCase = true
                                )
                    }

                    MenuConfig(
                        items = filtered.map { it.title } + "Back",
                        selectedIndex = state.selectedIndex
                    )
                }

                is MenuState.Episodes -> {
                    val feed = feeds[state.feedIndex]
                    MenuConfig(
                        items = feed.episodes.map { it.title } + "Back",
                        selectedIndex = state.selectedIndex
                    )
                }

                is MenuState.EpisodeDetail -> {
                    val feed = feeds[state.feedIndex]
                    val episode = feed.episodes[state.episodeIndex]
                    val items = listOf(
                        episode.title,
                        "",
                        episode.description.take(120),
                        "",
                        "Back"
                    )

                    MenuConfig(
                        items = items,
                        selectedIndex = items.lastIndex
                    )
                }
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            MenuConfig(emptyList(), 0)
        )


    // -----------------------------
    // Wheel Movement
    // -----------------------------

    fun moveSelection(delta: Int) {
        val state = _menuState.value

        if (state is MenuState.EpisodeDetail) return

        val itemCount = computeItemCount(state)
        if (itemCount <= 0) return

        val next =
            (state.selectedIndex + delta + itemCount) % itemCount

        _menuState.value = when (state) {
            is MenuState.Home -> state.copy(selectedIndex = next)
            is MenuState.Downloaded -> state.copy(selectedIndex = next)
            is MenuState.Categories -> state.copy(selectedIndex = next)
            is MenuState.Feeds -> state.copy(selectedIndex = next)
            is MenuState.Episodes -> state.copy(selectedIndex = next)
            is MenuState.EpisodeDetail -> state
        }
    }


    // -----------------------------
    // Confirm
    // -----------------------------

    fun confirmSelection() {
        val state = _menuState.value
        val feeds = _feeds.value

        when (state) {

            is MenuState.Home -> {
                when (state.selectedIndex) {
                    0 -> _menuState.value = MenuState.Downloaded()
                    1 -> _menuState.value = MenuState.Categories()
                }
            }

            is MenuState.Downloaded -> {

                val downloaded = _downloaded.value
                val slotCount = downloaded.take(4).size
                val hasAddNew = downloaded.size < 4
                val addNewIndex = slotCount
                val backIndex = slotCount + if (hasAddNew) 1 else 0

                when (state.selectedIndex) {

                    addNewIndex.takeIf { hasAddNew } -> {
                        _menuState.value = MenuState.Categories()
                    }

                    backIndex -> {
                        _menuState.value = MenuState.Home()
                    }

                    else -> {
                        // Later: open episode list for that slot
                    }
                }
            }

            is MenuState.Categories -> {
                val categories = sortedCategories()

                if (state.selectedIndex == categories.size) {
                    _menuState.value = MenuState.Home()
                } else {
                    val selectedCategory = categories[state.selectedIndex]
                    _menuState.value = MenuState.Feeds(
                        categoryName = selectedCategory
                    )
                }
            }

            is MenuState.Feeds -> {
                val filtered = filteredFeeds(state.categoryName)

                if (state.selectedIndex == filtered.size) {
                    _menuState.value = MenuState.Categories()
                } else {
                    val selectedFeed = filtered[state.selectedIndex]
                    val feedIndex = feeds.indexOf(selectedFeed)

                    _menuState.value = MenuState.Episodes(feedIndex)
                }
            }

            is MenuState.Episodes -> {
                val feed = feeds.getOrNull(state.feedIndex) ?: run {
                    _menuState.value = MenuState.Feeds()
                    return
                }

                if (state.selectedIndex == feed.episodes.size) {
                    _menuState.value = MenuState.Feeds()
                } else {
                    _menuState.value = MenuState.EpisodeDetail(
                        feedIndex = state.feedIndex,
                        episodeIndex = state.selectedIndex
                    )
                }
            }

            is MenuState.EpisodeDetail -> {
                _menuState.value =
                    MenuState.Episodes(feedIndex = state.feedIndex)
            }
        }
    }

    // -----------------------------
    // Back Button
    // -----------------------------

    fun onBack() {
        val state = _menuState.value

        _menuState.value = when (state) {
            is MenuState.Home -> state
            is MenuState.Downloaded -> MenuState.Home()
            is MenuState.Categories -> MenuState.Home()
            is MenuState.Feeds -> MenuState.Categories()
            is MenuState.Episodes ->
                MenuState.Feeds(categoryName = state.categoryName)
            is MenuState.EpisodeDetail ->
                MenuState.Episodes(feedIndex = state.feedIndex)
        }
    }

    // -----------------------------
    // Helpers
    // -----------------------------
    private fun filteredFeeds(categoryName: String?): List<PodcastFeed> {
        return _feeds.value.filter { feed ->
            categoryName == null ||
                    feed.category.equals(categoryName, ignoreCase = true)
        }
    }

    private fun sortedCategories(): List<String> {
        return _categories.value
            .distinct()
            .sorted()
    }

    private fun computeItemCount(state: MenuState): Int {
        return when (state) {

            is MenuState.Home -> {
                4 // Download, Play, Settings, Exit
            }

            is MenuState.Downloaded -> {
                val downloaded = _downloaded.value
                val count = downloaded.take(4).size
                val addNew = if (downloaded.size < 4) 1 else 0
                count + addNew + 1 // + Back
            }

            is MenuState.Categories -> {
                sortedCategories().size + 1 // + Back
            }

            is MenuState.Feeds -> {
                filteredFeeds(state.categoryName).size + 1 // + Back
            }

            is MenuState.Episodes -> {
                _feeds.value
                    .getOrNull(state.feedIndex)
                    ?.episodes
                    ?.size
                    ?.plus(1) // + Back
                    ?: 0
            }

            is MenuState.EpisodeDetail -> {
                5
                // title
                // blank
                // truncated description
                // blank
                // Back
            }
        }
    }
}
