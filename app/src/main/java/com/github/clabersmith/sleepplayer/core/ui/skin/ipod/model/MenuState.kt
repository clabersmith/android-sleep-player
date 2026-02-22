package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model

import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.viewmodel.MenuActions
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.viewmodel.MenuViewModel
import com.github.clabersmith.sleepplayer.features.podcasts.domain.model.PodcastEpisode
import com.github.clabersmith.sleepplayer.features.podcasts.domain.model.PodcastFeed

sealed class MenuState() {

    abstract val selectedIndex: Int
    abstract val itemCount: Int

    abstract fun copyWithIndex(newIndex: Int): MenuState

    abstract fun onConfirm(actions: MenuActions): MenuState

    data class Home(
        override val selectedIndex: Int = 0
    ) : MenuState() {

        override val itemCount: Int get() = 4

        override fun copyWithIndex(newIndex: Int) = copy(selectedIndex = newIndex)

        override fun onConfirm(actions: MenuActions): MenuState {
            return when (selectedIndex) {
                0 -> actions.buildDownloadState()
                else -> this
            }
        }
    }

    data class Download(
        val slots: List<SlotState>,
        val maxSlots: Int,
        override val selectedIndex: Int = 0
    ) : MenuState() {

        override val itemCount: Int
            get() = slots.size + if (slots.size < maxSlots) 1 else 0

        override fun copyWithIndex(newIndex: Int) = copy(selectedIndex = newIndex)

        override fun onConfirm(actions: MenuActions): MenuState {

            val slotCount = slots.size
            val hasAddNew = slotCount < maxSlots
            val addNewIndex = slotCount

            return when (selectedIndex) {
                addNewIndex.takeIf { hasAddNew } ->
                    actions.buildCategoriesState()

                else -> {
                    val slot = slots[selectedIndex]
                    actions.buildEpisodeDetailState(
                        slot.feedIndex,
                        slot.episodeIndex,
                        slot.loadedEpisode,
                        origin = EpisodeDetail.Origin.DOWNLOAD
                    )
                }
            }
        }
    }

    data class Categories(
        val categories: List<String>,
        override val selectedIndex: Int = 0
    ) : MenuState() {

        override val itemCount: Int get() = categories.size

        override fun copyWithIndex(newIndex: Int) = copy(selectedIndex = newIndex)

        override fun onConfirm(actions: MenuActions): MenuState {
            val category = categories[selectedIndex]
            return actions.buildFeedsState(category)
        }
    }

    data class Feeds(
        val categoryName: String,
        val feeds: List<PodcastFeed>,
        override val selectedIndex: Int = 0
    ) : MenuState() {

        override val itemCount: Int get() = feeds.size

        override fun copyWithIndex(newIndex: Int) = copy(selectedIndex = newIndex)

        override fun onConfirm(actions: MenuActions): MenuState {
            val selectedFeed = feeds[selectedIndex]
            val feedIndex = actions.feedIndexOf(selectedFeed)
            return actions.buildEpisodesState(feedIndex, categoryName)
        }
    }

    data class Episodes(
        val feedIndex: Int,
        val episodes: List<PodcastEpisode>,
        val categoryName: String?,
        override val selectedIndex: Int = 0
    ) : MenuState() {

        override val itemCount: Int get() = episodes.size

        override fun copyWithIndex(newIndex: Int) = copy(selectedIndex = newIndex)

        override fun onConfirm(actions: MenuActions): MenuState {
            val episode = episodes[selectedIndex]
            return actions.buildEpisodeDetailState(
                feedIndex,
                selectedIndex,
                episode,
                origin = EpisodeDetail.Origin.EPISODES
            )
        }
    }

    data class EpisodeDetail(
        val feedIndex: Int,
        val episodeIndex: Int,
        val episode: PodcastEpisode,
        val actionRows: List<ActionRow> = emptyList(),
        val isDownloading: Boolean = false,
        val origin: Origin,
        override val selectedIndex: Int = 0
    ) : MenuState() {
        override val itemCount: Int get() = actionRows.size

        override fun copyWithIndex(newIndex: Int) = copy(selectedIndex = newIndex)

        override fun onConfirm(actions: MenuActions): MenuState {

            val action = actionRows.getOrNull(selectedIndex) ?: return this

            return when (action) {

                ActionRow.Download -> {
                    actions.startDownload(this)
                    // Immediately return "downloading" UI state
                    copy(
                        isDownloading = true,
                        actionRows = listOf(ActionRow.Downloading(), ActionRow.Cancel),
                        selectedIndex = 1
                    )
                }

                ActionRow.Cancel -> {
                    actions.cancelDownload(this)
                    copy(
                        isDownloading = false,
                        actionRows = listOf(ActionRow.Download),
                        selectedIndex = 0
                    )
                }

                ActionRow.Delete -> {
                    actions.deleteEpisode(this)
                    actions.buildDownloadState()
                }

                else -> this
            }
        }

        enum class Origin {
            EPISODES,
            DOWNLOAD
        }
    }
}

