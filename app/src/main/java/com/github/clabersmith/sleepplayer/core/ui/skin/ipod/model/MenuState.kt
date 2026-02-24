package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model

import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.viewmodel.MenuActions
import com.github.clabersmith.sleepplayer.features.podcasts.domain.model.PodcastEpisode
import com.github.clabersmith.sleepplayer.features.podcasts.domain.model.PodcastFeed

sealed class MenuState() {

    abstract val selectedIndex: Int
    abstract val itemCount: Int

    abstract val title: String

    abstract fun copyWithIndex(newIndex: Int): MenuState

    abstract fun onConfirm(actions: MenuActions): MenuState

    open fun onPlayPause(actions: MenuActions): MenuState = this
    open fun onScanForwardDown(actions: MenuActions) = this
    open fun onScanForwardUp(actions: MenuActions) = this
    open fun onScanBackDown(actions: MenuActions) = this
    open fun onScanBackUp(actions: MenuActions) = this

    data class Home(
        override val selectedIndex: Int = 0
    ) : MenuState() {

        override val itemCount: Int get() = 4

        override val title = "Home"

        override fun copyWithIndex(newIndex: Int) = copy(selectedIndex = newIndex)

        override fun onConfirm(actions: MenuActions): MenuState {
            return when (selectedIndex) {
                0 -> actions.buildDownloadState()

                1 -> actions.buildPlayState()

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

        override val title = "Downloaded"

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

        override val title = "Categories"

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

        override val title = categoryName

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

        override val title = "Episodes"

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

        override val title = "Episode"

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

    data class Play(
        val slots: List<SlotState>,
        override val selectedIndex: Int = 0
    ) : MenuState() {
        override val itemCount: Int get() = slots.size

        override val title = "Play"

        override fun copyWithIndex(newIndex: Int) = copy(selectedIndex = newIndex)

        override fun onConfirm(actions: MenuActions): MenuState {
            val slot = slots[selectedIndex]
            return actions.startPlayback(slot)
        }
    }

    data class NowPlaying(
        val slot: SlotState,
        val durationMs: Long,
        val positionMs: Long,
        val isPlaying: Boolean,
        override val selectedIndex: Int = 0
    ) : MenuState() {
        override val itemCount: Int
            get() = TODO("Not yet implemented")

        override val title = "Now Playing"

        override fun copyWithIndex(newIndex: Int) = copy(selectedIndex = newIndex)

        override fun onConfirm(actions: MenuActions): MenuState {
            return this //no op
        }

        override fun onPlayPause(actions: MenuActions): MenuState {
            actions.togglePlayPause(this)
            return this
        }

        override fun onScanForwardDown(actions: MenuActions): MenuState {
            actions.startScanForward()
            return this
        }

        override fun onScanForwardUp(actions: MenuActions): MenuState {
            actions.stopScan()
            return this
        }

        override fun onScanBackDown(actions: MenuActions): MenuState {
            actions.startScanBack()
            return this
        }

        override fun onScanBackUp(actions: MenuActions): MenuState {
            actions.stopScan()
            return this
        }
    }

}
