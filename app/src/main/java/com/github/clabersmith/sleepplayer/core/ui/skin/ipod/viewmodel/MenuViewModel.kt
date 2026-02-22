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
import com.github.clabersmith.sleepplayer.features.podcasts.data.download.Downloader
import com.github.clabersmith.sleepplayer.features.podcasts.data.local.FileStorage
import com.github.clabersmith.sleepplayer.features.podcasts.data.local.SlotRepository
import com.github.clabersmith.sleepplayer.features.podcasts.domain.model.PodcastFeed
import com.github.clabersmith.sleepplayer.features.podcasts.domain.repository.PodcastRepository
import com.github.clabersmith.sleepplayer.features.podcasts.domain.DownloadConstants.MAX_SLOT_SIZE
import com.github.clabersmith.sleepplayer.features.podcasts.domain.model.PodcastEpisode
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException


class MenuViewModel(
    private val podcastRepository: PodcastRepository,
    private val slotRepository: SlotRepository,
    private val downloader: Downloader,
    private val storage: FileStorage
) : ViewModel(), MenuActions {

    private val _menuState = MutableStateFlow<MenuState>(MenuState.Home())
    val menuState: StateFlow<MenuState> = _menuState

    private val _feeds = MutableStateFlow<List<PodcastFeed>>(emptyList())
    private val _categories = MutableStateFlow<List<String>>(emptyList())
    private val _slots = MutableStateFlow<List<SlotState>>(emptyList())

    private var downloadJob: Job? = null
    private var currentDownloadFileName: String? = null

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

        _menuState.value = state.copyWithIndex(
            nextIndex(state.selectedIndex, delta, state.itemCount)
        )
    }

    // -----------------------------
    // Click Wheel Center Button
    // -----------------------------
    fun confirmSelection() {
        val current = _menuState.value
        _menuState.value = current.onConfirm(actions = this)
    }

    override fun buildDownloadState(): MenuState.Download {
        println("Building Download State with slots: ${_slots.value}")
        return MenuState.Download(
            slots = _slots.value,
            maxSlots = MAX_SLOT_SIZE,
            selectedIndex = 0
        )
    }

    override fun buildCategoriesState(): MenuState.Categories {
        return MenuState.Categories(
            categories = _categories.value.sorted(),
            selectedIndex = 0
        )
    }

    override fun buildFeedsState(category: String): MenuState.Feeds {
        val feeds = _feeds.value
            .filter { it.category == category }

        return MenuState.Feeds(
            feeds = feeds,
            categoryName = category,
            selectedIndex = 0
        )
    }

    override fun buildEpisodesState(
        feedIndex: Int,
        categoryName: String
    ): MenuState.Episodes {

        val feed = _feeds.value[feedIndex]

        return MenuState.Episodes(
            episodes = feed.episodes,
            feedIndex = feedIndex,
            categoryName = categoryName,
            selectedIndex = 0
        )
    }

    override fun feedIndexOf(feed: PodcastFeed): Int {
        return _feeds.value.indexOf(feed)
    }


    override fun startDownload(state: MenuState.EpisodeDetail) {
        if (state.isDownloading) return

        downloadJob?.cancel()

        downloadJob = viewModelScope.launch {

            val result = downloadEpisode(state)

            println("Download result: $result")

            when (result) {
                is DownloadResult.Success -> {
                    _menuState.value = buildDownloadState()
                }

                is DownloadResult.Cancelled -> {
                    // Return to non-downloading episode detail
                    _menuState.value = state.copy(
                        isDownloading = false,
                        actionRows = listOf(ActionRow.Download),
                        selectedIndex = 0
                    )
                }

                is DownloadResult.Error -> {
                    _menuState.value = state.copy(
                        isDownloading = false,
                        actionRows = listOf(ActionRow.Download),
                        selectedIndex = 0
                    )
                }
            }
        }
    }

    private suspend fun downloadEpisode(
        state: MenuState.EpisodeDetail
    ): DownloadResult {

        val feed = _feeds.value.getOrNull(state.feedIndex)
            ?: return DownloadResult.Error(IllegalStateException("Invalid feed index"))

        val episode = feed.episodes.getOrNull(state.episodeIndex)
            ?: return DownloadResult.Error(IllegalStateException("Invalid episode index"))

        return try {

            val file = downloader.download(
                url = episode.audioUrl,
                fileName = generateSafeFileName(episode),
                onProgress = { progress ->
                    updateDownloadProgress(progress)
                }
            )

            addSlot(
                state.feedIndex,
                state.episodeIndex,
                episode,
                file.name
            )

            DownloadResult.Success(file.name)

        } catch (e: CancellationException) {
            DownloadResult.Cancelled

        } catch (e: Exception) {
            DownloadResult.Error(e)
        }
    }

    fun updateDownloadProgress(progress: Float) {
        _menuState.update { current ->
            if (current is MenuState.EpisodeDetail && current.isDownloading) {
                current.copy(
                    actionRows = listOf(
                        ActionRow.Downloading(progress),
                        ActionRow.Cancel
                    )
                )
            } else current
        }
    }

    override fun cancelDownload(state: MenuState.EpisodeDetail) {
        downloadJob?.cancel()
        restoreEpisodeDetail(state)
    }

    override fun deleteEpisode(state: MenuState.EpisodeDetail) {
        val slot = _slots.value.find {
            it.feedIndex == state.feedIndex &&
                    it.episodeIndex == state.episodeIndex
        } ?: return

        storage.deleteFile(slot.fileName)
        removeSlot(slot.feedIndex, slot.episodeIndex)
        goDownloaded()
    }


    // -----------------------------
    // Menu Button (Click Wheel Back
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

            is MenuState.Download -> MenuState.Home()

            is MenuState.Categories -> MenuState.Home()

            is MenuState.Feeds -> MenuState.Categories(
                categories = sortedCategories()
            )

            is MenuState.Episodes ->
                MenuState.Feeds(
                    categoryName = state.categoryName ?: "",
                    feeds = filteredFeeds(state.categoryName)
                )

            is MenuState.EpisodeDetail -> {

                when (state.origin) {

                    DOWNLOAD ->
                        MenuState.Download(
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

    override fun buildEpisodeDetailState(
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
            origin == DOWNLOAD -> ActionRow.Delete

            origin == EPISODES &&
                    alreadyDownloaded -> ActionRow.AlreadyDownloaded

            else -> ActionRow.Download
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

    fun goDownloaded(): MenuState {
        return buildDownloadState()
    }

    // -----------------------------
    // Download Slot Management
    // -----------------------------
    private fun addSlot(feedIndex: Int, episodeIndex: Int, episode: PodcastEpisode, fileName: String) {
        val newSlot = SlotState(
            feedIndex = feedIndex,
            episodeIndex = episodeIndex,
            loadedEpisode = episode,
            fileName = fileName
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
            slotRepository.saveSlots(
                _slots.value.map { it.toPersisted() }
            )
        }
    }

    private fun restoreSlots() {
        viewModelScope.launch {
            val persisted = slotRepository.loadSlots()

            _slots.value = persisted.mapNotNull { p ->

                if (!storage.fileExists(p.fileName)) {
                    return@mapNotNull null
                }

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

    //----------------
    // Download Helpers
    //----------------
    private fun generateSafeFileName(episode: PodcastEpisode): String {
        val base = episode.title
            .replace("[^A-Za-z0-9_]".toRegex(), "_")
            .take(50)

        return "$base.mp3"
    }

    private fun restoreEpisodeDetail(
        previous: MenuState.EpisodeDetail
    ) {
        val alreadyDownloaded = _slots.value.any {
            it.feedIndex == previous.feedIndex &&
                    it.episodeIndex == previous.episodeIndex
        }

        _menuState.value = previous.copy(
            isDownloading = false,
            actionRows = when {
                alreadyDownloaded -> listOf(ActionRow.AlreadyDownloaded)
                else -> listOf(ActionRow.Download)
            },
            selectedIndex = 0
        )
    }
}

interface MenuActions {
    fun buildDownloadState(): MenuState
    fun buildCategoriesState(): MenuState
    fun buildFeedsState(category: String): MenuState
    fun buildEpisodesState(feedIndex: Int, categoryName: String): MenuState
    fun buildEpisodeDetailState(
        feedIndex: Int,
        episodeIndex: Int,
        episode: PodcastEpisode,
        origin: MenuState.EpisodeDetail.Origin
    ): MenuState

    fun feedIndexOf(feed: PodcastFeed): Int

    // Side-effect actions
    fun startDownload(state: MenuState.EpisodeDetail)
    fun cancelDownload(state: MenuState.EpisodeDetail)
    fun deleteEpisode(state: MenuState.EpisodeDetail)
}

sealed class DownloadResult {
    data class Success(val fileName: String) : DownloadResult()
    object Cancelled : DownloadResult()
    data class Error(val throwable: Throwable) : DownloadResult()
}


