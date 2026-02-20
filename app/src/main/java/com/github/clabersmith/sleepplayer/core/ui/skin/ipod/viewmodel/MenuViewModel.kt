package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuState
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.ActionRow
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.SlotState
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.toPersisted
import com.github.clabersmith.sleepplayer.features.podcasts.data.local.PersistedSlotRepository
import com.github.clabersmith.sleepplayer.features.podcasts.domain.model.PodcastFeed
import com.github.clabersmith.sleepplayer.features.podcasts.domain.repository.PodcastRepository
import com.github.clabersmith.sleepplayer.features.podcasts.domain.DownloadConstants.MAX_SLOT_SIZE
import com.github.clabersmith.sleepplayer.features.podcasts.domain.model.PodcastEpisode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
class MenuViewModel(
    private val podcastRepository: PodcastRepository,
    private val persistedSlotRepository: PersistedSlotRepository
) : ViewModel() {

    private val _menuState = MutableStateFlow<MenuState>(MenuState.Home())
    val menuState: StateFlow<MenuState> = _menuState

    private val _feeds = MutableStateFlow<List<PodcastFeed>>(emptyList())

    private val _categories = MutableStateFlow<List<String>>(emptyList())

    private val _slots = MutableStateFlow<List<SlotState>>(emptyList())

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _feeds.value = podcastRepository.getFeeds()
            _categories.value = podcastRepository.getCategories()

            restoreSlots()   // after feeds exist
        }
    }


    // -----------------------------
    // Click Wheel Movement
    // -----------------------------

    fun moveSelection(delta: Int) {
        val state = _menuState.value

        _menuState.value = when (state) {

            // -------------------------
            // Home
            // -------------------------
            is MenuState.Home -> {
                val itemCount = 4

                val next =
                    (state.selectedIndex + delta + itemCount) % itemCount

                state.copy(selectedIndex = next)
            }

            // -------------------------
            // Downloaded
            // -------------------------
            is MenuState.Downloaded -> {

                val slotCount = state.slots.size
                val hasAddNew = slotCount < state.maxSlots

                val totalItems =
                    slotCount +
                            (if (hasAddNew) 1 else 0) +
                            1 // Back

                if (totalItems == 0) return

                val next =
                    (state.selectedIndex + delta + totalItems) %
                            totalItems

                state.copy(selectedIndex = next)
            }

            // -------------------------
            // Categories
            // -------------------------
            is MenuState.Categories -> {

                val totalItems =
                    state.categories.size + 1 // Back

                val next =
                    (state.selectedIndex + delta + totalItems) %
                            totalItems

                state.copy(selectedIndex = next)
            }

            // -------------------------
            // Feeds
            // -------------------------
            is MenuState.Feeds -> {

                val totalItems =
                    state.feeds.size + 1 // Back

                val next =
                    (state.selectedIndex + delta + totalItems) %
                            totalItems

                state.copy(selectedIndex = next)
            }

            // -------------------------
            // Episodes
            // -------------------------
            is MenuState.Episodes -> {

                val totalItems =
                    state.episodes.size + 1 // Back

                val next =
                    (state.selectedIndex + delta + totalItems) %
                            totalItems

                state.copy(selectedIndex = next)
            }

            // -------------------------
            // Episode Detail
            // -------------------------
            is MenuState.EpisodeDetail -> {

                val selectableCount =
                    state.actionRows.count { it.enabled }

                if (selectableCount == 0) return

                val next =
                    (state.selectedIndex + delta + selectableCount) %
                            selectableCount

                state.copy(selectedIndex = next)
            }
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
                    0 -> _menuState.value = MenuState.Downloaded(
                        slots = _slots.value,
                        maxSlots = MAX_SLOT_SIZE
                    )

                    1 -> _menuState.value = MenuState.Categories(
                        categories = sortedCategories()
                    )
                }
            }

            is MenuState.Downloaded -> {

                val slots = _slots.value
                val slotCount = slots.take(MAX_SLOT_SIZE).size
                val hasAddNew = slots.size < MAX_SLOT_SIZE
                val addNewIndex = slotCount
                val backIndex = slotCount + if (hasAddNew) 1 else 0

                when (state.selectedIndex) {

                    addNewIndex.takeIf { hasAddNew } -> {
                        _menuState.value = MenuState.Categories(
                            categories = sortedCategories()
                        )
                    }

                    backIndex -> {
                        _menuState.value = MenuState.Home()
                    }

                    else -> {
                        // Selecting a downloaded episode slot
                        val slot = _slots.value[state.selectedIndex]

                        _menuState.value = buildEpisodeDetailState(
                            feedIndex = slot.feedIndex,
                            episodeIndex = slot.episodeIndex,
                            episode = slot.loadedEpisode,
                            selectedFromSlot = true // indicates DELETE mode if selected from Downloaded menu
                        )
                    }
                }
            }

            is MenuState.Categories -> {
                val categories = sortedCategories()

                if (state.selectedIndex == categories.size) {
                    _menuState.value = MenuState.Home()
                } else {
                    val selectedCategory = categories[state.selectedIndex]
                    val filtered = filteredFeeds(selectedCategory)

                    _menuState.value = MenuState.Feeds(
                        categoryName = selectedCategory,
                        feeds = filtered
                    )
                }
            }

            is MenuState.Feeds -> {
                val filtered = filteredFeeds(state.categoryName)

                if (state.selectedIndex == filtered.size) {
                    _menuState.value = MenuState.Categories(
                        categories = sortedCategories()
                    )
                } else {
                    val selectedFeed = filtered[state.selectedIndex]
                    val feedIndex = feeds.indexOf(selectedFeed)

                    _menuState.value = MenuState.Episodes(
                        feedIndex = feedIndex,
                        episodes = selectedFeed.episodes,
                        categoryName = state.categoryName
                    )
                }
            }

            is MenuState.Episodes -> {
                val filtered = filteredFeeds(state.categoryName)

                val feed = feeds.getOrNull(state.feedIndex) ?: run {
                    _menuState.value = MenuState.Feeds(
                        categoryName = state.categoryName,
                        feeds = filtered
                    )

                    return
                }

                if (state.selectedIndex == feed.episodes.size) {
                    _menuState.value = MenuState.Feeds(
                        categoryName = state.categoryName,
                        feeds = filtered
                    )
                } else {
                    _menuState.value = buildEpisodeDetailState(
                        feedIndex = state.feedIndex,
                        episodeIndex = state.selectedIndex,
                        episode = feed.episodes[state.selectedIndex],
                        selectedFromSlot = false
                    )
                }
            }

            is MenuState.EpisodeDetail -> {

                val selectedAction =
                    state.actionRows
                        .filter { it.enabled }
                        .getOrNull(state.selectedIndex)
                        ?: return

                val feed = feeds[state.feedIndex]
                val episode = feed.episodes[state.episodeIndex]

                when (selectedAction.type) {

                    ActionRow.Type.DOWNLOAD -> {

                        val newSlot = SlotState(
                            feedIndex = state.feedIndex,
                            episodeIndex = state.episodeIndex,
                            loadedEpisode = episode,
                            fileName = ""
                        )

                        _slots.value = _slots.value + newSlot
                        persistSlots()

                        _menuState.value = MenuState.Downloaded(
                            slots = _slots.value,
                            maxSlots = MAX_SLOT_SIZE
                        )
                    }

                    ActionRow.Type.DELETE -> {

                        _slots.value = _slots.value.filterNot {
                            it.feedIndex == state.feedIndex &&
                                    it.episodeIndex == state.episodeIndex
                        }

                        persistSlots()

                        _menuState.value = MenuState.Downloaded(
                            slots = _slots.value,
                            maxSlots = MAX_SLOT_SIZE
                        )
                    }

                    ActionRow.Type.BACK -> {
                        _menuState.value =
                            MenuState.Episodes(
                                feedIndex = state.feedIndex,
                                episodes = feed.episodes,
                                categoryName = feed.category
                            )
                    }
                }
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
            is MenuState.Feeds -> MenuState.Categories(
                categories = sortedCategories()
            )
            is MenuState.Episodes -> {
                val feedsForCategory = filteredFeeds(state.categoryName)
                MenuState.Feeds(
                    categoryName = state.categoryName,
                    feeds = feedsForCategory
                )
            }

            is MenuState.EpisodeDetail ->
                MenuState.Episodes(
                    feedIndex = state.feedIndex,
                    episodes = _feeds.value[state.feedIndex].episodes,
                    categoryName = _feeds.value[state.feedIndex].category
                )
        }
    }

    private fun buildEpisodeDetailState(
        feedIndex: Int,
        episodeIndex: Int,
        episode: PodcastEpisode,
        selectedFromSlot: Boolean
    ): MenuState.EpisodeDetail {

        val alreadyDownloaded = _slots.value.any {
            it.feedIndex == feedIndex &&
                    it.episodeIndex == episodeIndex
        }

        val primaryAction = when {
            selectedFromSlot -> ActionRow(
                label = "Delete",
                type = ActionRow.Type.DELETE,
                enabled = true
            )

            alreadyDownloaded -> ActionRow(
                label = "Already Downloaded",
                type = ActionRow.Type.DOWNLOAD,
                enabled = false
            )

            else -> ActionRow(
                label = "Download",
                type = ActionRow.Type.DOWNLOAD,
                enabled = true
            )
        }

        val backAction = ActionRow(
            label = "Back",
            type = ActionRow.Type.BACK,
            enabled = true
        )

        return MenuState.EpisodeDetail(
            feedIndex = feedIndex,
            episodeIndex = episodeIndex,
            episode = episode,
            actionRows = listOf(primaryAction, backAction),
            selectedIndex = 0
        )
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

    private fun persistSlots() {
        viewModelScope.launch {
            persistedSlotRepository.saveSlots(
                _slots.value.map { it.toPersisted() }
            )
        }
    }

    private fun restoreSlots() {
        viewModelScope.launch {
            val persisted = persistedSlotRepository.loadSlots()

            _slots.value = persisted.mapNotNull { p ->

                val feed = _feeds.value.getOrNull(p.feedIndex)
                    ?: return@mapNotNull null

                val episode = feed.episodes
                    .firstOrNull { it.id == p.episodeId }
                    ?: return@mapNotNull null

                SlotState(
                    feedIndex = p.feedIndex,
                    episodeIndex = p.episodeIndex,
                    loadedEpisode = episode,
                    fileName = p.fileName
                )
            }.take(MAX_SLOT_SIZE)

            persistSlots()

        }
    }
}
