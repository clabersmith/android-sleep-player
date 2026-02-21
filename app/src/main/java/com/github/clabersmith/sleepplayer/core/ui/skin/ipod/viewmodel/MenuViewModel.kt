package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuState
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuState.EpisodeDetail.Origin
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuState.EpisodeDetail.Origin.DOWNLOAD
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuState.EpisodeDetail.Origin.EPISODES
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.ActionRow
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.SlotState
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.toPersisted
import com.github.clabersmith.sleepplayer.features.podcasts.data.local.PersistedSlotRepository
import com.github.clabersmith.sleepplayer.features.podcasts.data.local.SlotRepository
import com.github.clabersmith.sleepplayer.features.podcasts.domain.model.PodcastFeed
import com.github.clabersmith.sleepplayer.features.podcasts.domain.repository.PodcastRepository
import com.github.clabersmith.sleepplayer.features.podcasts.domain.DownloadConstants.MAX_SLOT_SIZE
import com.github.clabersmith.sleepplayer.features.podcasts.domain.model.PodcastEpisode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class MenuViewModel(
    private val podcastRepository: PodcastRepository,
    private val persistedSlotRepository: SlotRepository
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
        _menuState.value = when (val state = _menuState.value) {

            is MenuState.Home ->
                state.copy(selectedIndex = nextIndex(state.selectedIndex, delta, 4))

            is MenuState.Downloaded -> {
                val total =
                    state.slots.size + (if (state.slots.size < state.maxSlots) 1 else 0)
                state.copy(selectedIndex = nextIndex(state.selectedIndex, delta, total))
            }

            is MenuState.Categories ->
                state.copy(selectedIndex = nextIndex(state.selectedIndex, delta, state.categories.size))

            is MenuState.Feeds ->
                state.copy(selectedIndex = nextIndex(state.selectedIndex, delta, state.feeds.size))

            is MenuState.Episodes ->
                state.copy(selectedIndex = nextIndex(state.selectedIndex, delta, state.episodes.size))

            is MenuState.EpisodeDetail -> {
                val count = state.actionRows.count { it.enabled }
                state.copy(selectedIndex = nextIndex(state.selectedIndex, delta, count))
            }
        }
    }


    // -----------------------------
    // Click Wheel Center Button
    // -----------------------------
    fun confirmSelection() {
        val state = _menuState.value

        when (state) {

            is MenuState.Home -> {
                when (state.selectedIndex) {
                    0 -> _menuState.value = MenuState.Downloaded(
                        slots = _slots.value,
                        maxSlots = MAX_SLOT_SIZE
                    )
                }
            }

            is MenuState.Downloaded -> {
                val slotCount = state.slots.size
                val hasAddNew = slotCount < state.maxSlots
                val addNewIndex = slotCount

                when (state.selectedIndex) {

                    addNewIndex.takeIf { hasAddNew } -> goCategories()

                    else -> {
                        val slot = state.slots[state.selectedIndex]
                        _menuState.value = buildEpisodeDetailState(
                            feedIndex = slot.feedIndex,
                            episodeIndex = slot.episodeIndex,
                            episode = slot.loadedEpisode,
                            origin = DOWNLOAD
                        )
                    }
                }
            }

            is MenuState.Categories -> {
                val category = state.categories[state.selectedIndex]
                goFeeds(category)
            }

            is MenuState.Feeds -> {
                val selectedFeed = state.feeds[state.selectedIndex]
                val feedIndex = _feeds.value.indexOf(selectedFeed)
                goEpisodes(feedIndex, state.categoryName)
            }

            is MenuState.Episodes -> {
                val episode = state.episodes[state.selectedIndex]

                _menuState.value = buildEpisodeDetailState(
                    feedIndex = state.feedIndex,
                    episodeIndex = state.selectedIndex,
                    episode = episode,
                    origin = EPISODES
                )
            }

            is MenuState.EpisodeDetail -> {

                val selectedAction =
                    state.actionRows
                        .filter { it.enabled }
                        .getOrNull(state.selectedIndex)
                        ?: return

                val feed = _feeds.value[state.feedIndex]
                val episode = feed.episodes[state.episodeIndex]

                when (selectedAction.type) {

                    ActionRow.Type.DOWNLOAD -> {
                        addSlot(state.feedIndex, state.episodeIndex, episode)
                        goDownloaded()
                    }

                    ActionRow.Type.DELETE -> {
                        removeSlot(state.feedIndex, state.episodeIndex)
                        goDownloaded()
                    }
                }
            }
        }
    }

    // -----------------------------
    // MENU BUTTON (CLICK WHEEL BACK)
    // -----------------------------
    fun onMenuShortPress() {
        _menuState.value = goUp()
    }

    fun onMenuLongPress() {
        _menuState.value = MenuState.Home()
    }
    fun goUp(): MenuState {
        return when (val state = _menuState.value) {

            is MenuState.Home -> state

            is MenuState.Downloaded -> MenuState.Home()

            is MenuState.Categories -> MenuState.Home()

            is MenuState.Feeds -> MenuState.Categories(
                categories = sortedCategories()
            )

            is MenuState.Episodes ->
                MenuState.Feeds(
                    categoryName = state.categoryName,
                    feeds = filteredFeeds(state.categoryName)
                )

            is MenuState.EpisodeDetail -> {

                when (state.origin) {

                    DOWNLOAD ->
                        MenuState.Downloaded(
                            slots = _slots.value,
                            maxSlots = MAX_SLOT_SIZE
                        )

                    EPISODES ->
                        MenuState.Episodes(
                            feedIndex = state.feedIndex,
                            episodes = _feeds.value[state.feedIndex].episodes,
                            categoryName = _feeds.value[state.feedIndex].category
                        )
                }
            }
        }
    }

    private fun buildEpisodeDetailState(
        feedIndex: Int,
        episodeIndex: Int,
        episode: PodcastEpisode,
        origin: Origin
    ): MenuState.EpisodeDetail {

        val alreadyDownloaded = _slots.value.any {
            it.feedIndex == feedIndex &&
                    it.episodeIndex == episodeIndex
        }

        val primaryAction = when {
            origin == DOWNLOAD -> ActionRow(
                label = "Delete",
                type = ActionRow.Type.DELETE,
                enabled = true
            )

            origin == EPISODES &&
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

        return MenuState.EpisodeDetail(
            feedIndex = feedIndex,
            episodeIndex = episodeIndex,
            episode = episode,
            actionRows = listOf(primaryAction),
            origin = origin
        )
    }
    // -----------------------------
    // Data Helpers
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

    // -----------------------------
    // Navigation Helpers
    // -----------------------------
    private fun nextIndex(current: Int, delta: Int, size: Int): Int {
        if (size <= 0) return current
        return (current + delta + size) % size
    }

    private fun goDownloaded() {
        _menuState.value = MenuState.Downloaded(
            slots = _slots.value,
            maxSlots = MAX_SLOT_SIZE
        )
    }

    private fun goCategories() {
        _menuState.value = MenuState.Categories(
            categories = sortedCategories()
        )
    }

    private fun goFeeds(category: String?) {
        _menuState.value = MenuState.Feeds(
            categoryName = category,
            feeds = filteredFeeds(category)
        )
    }

    private fun goEpisodes(feedIndex: Int, category: String?) {
        val feed = _feeds.value.getOrNull(feedIndex) ?: return
        _menuState.value = MenuState.Episodes(
            feedIndex = feedIndex,
            episodes = feed.episodes,
            categoryName = category
        )
    }

    // -----------------------------
    // Download Slot Management
    // -----------------------------
    private fun addSlot(feedIndex: Int, episodeIndex: Int, episode: PodcastEpisode) {
        val newSlot = SlotState(
            feedIndex = feedIndex,
            episodeIndex = episodeIndex,
            loadedEpisode = episode,
            fileName = ""
        )

        _slots.value = (_slots.value + newSlot).take(MAX_SLOT_SIZE)
        persistSlots()
    }

    private fun removeSlot(feedIndex: Int, episodeIndex: Int) {
        _slots.value = _slots.value.filterNot {
            it.feedIndex == feedIndex &&
                    it.episodeIndex == episodeIndex
        }
        persistSlots()
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
