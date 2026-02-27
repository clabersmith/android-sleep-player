package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model

import com.github.clabersmith.sleepplayer.features.podcasts.domain.DownloadConstants.MAX_SLOT_SIZE
import com.github.clabersmith.sleepplayer.features.podcasts.domain.model.PodcastEpisode
import com.github.clabersmith.sleepplayer.features.podcasts.domain.model.PodcastFeed

sealed class MenuState() {

    abstract val selectedIndex: Int
    abstract val itemCount: Int

    abstract val title: String

    open fun reduce(event: MenuEvent): MenuTransition {
        return MenuTransition(this)
    }

    abstract fun copyWithIndex(newIndex: Int): MenuState

    data class Home(
        val slots: List<SlotState>,
        val categories: List<String>,
        val allFeeds: List<PodcastFeed>,
        override val selectedIndex: Int = 0
    ) : MenuState() {

        override val itemCount: Int get() = 4

        override val title = "Home"

        override fun copyWithIndex(newIndex: Int) = copy(selectedIndex = newIndex)

        override fun reduce(event: MenuEvent): MenuTransition {
            println("Home slots when navigating to Play: ${slots.size}")
            return when (event) {

                MenuEvent.Confirm -> {
                    when (selectedIndex) {

                        0 -> MenuTransition(
                            newState = Download(
                                slots = slots,
                                maxSlots = MAX_SLOT_SIZE,
                                categories = categories,
                                allFeeds = allFeeds,
                                selectedIndex = 0
                            )
                        )

                        1 -> MenuTransition(
                            newState = Play(
                                slots = slots,
                                selectedIndex = 0
                            )
                        )

                        else -> MenuTransition(this)
                    }
                }

                else -> MenuTransition(this)
            }
        }
    }

    data class Download(
        val slots: List<SlotState>,
        val maxSlots: Int,
        val categories: List<String>,
        val allFeeds: List<PodcastFeed>,
        override val selectedIndex: Int = 0
    ) : MenuState() {

        override val itemCount: Int
            get() = slots.size + if (slots.size < maxSlots) 1 else 0

        override val title = "Downloads"

        override fun copyWithIndex(newIndex: Int) = copy(selectedIndex = newIndex)

        override fun reduce(event: MenuEvent): MenuTransition {
            return when (event) {

                MenuEvent.Confirm -> {

                    val slotCount = slots.size
                    val hasAddNew = slotCount < maxSlots
                    val addNewIndex = slotCount

                    when {
                        hasAddNew && selectedIndex == addNewIndex ->
                            MenuTransition(
                                newState = Categories(
                                    categories = categories,
                                    allFeeds = allFeeds,
                                    slots = slots,
                                    selectedIndex = 0
                                )
                            )

                        else -> {
                            val slot = slots[selectedIndex]

                            MenuTransition(
                                newState = EpisodeDetail(
                                    feedIndex = slot.feedIndex,
                                    episodeIndex = slot.episodeIndex,
                                    episode = slot.loadedEpisode,
                                    origin = EpisodeDetail.Origin.DOWNLOAD,
                                    actionRows = listOf(ActionRow.Delete),
                                    selectedIndex = 0
                                )
                            )
                        }
                    }
                }

                else ->
                    MenuTransition(this)
            }
        }
    }

    data class Categories(
        val categories: List<String>,
        val allFeeds: List<PodcastFeed>,
        val slots: List<SlotState>,
        override val selectedIndex: Int = 0
    ) : MenuState() {

        override val itemCount: Int get() = categories.size

        override val title = "Categories"

        override fun copyWithIndex(newIndex: Int) = copy(selectedIndex = newIndex)

        override fun reduce(event: MenuEvent): MenuTransition {
            return when (event) {

                MenuEvent.Confirm -> {
                    val category = categories[selectedIndex]

                    val categoryFeeds = allFeeds
                        .filter { it.category == category }

                    MenuTransition(
                        newState = Feeds(
                            categoryFeeds = categoryFeeds,
                            allFeeds = allFeeds,
                            slots = slots,
                            categoryName = category,
                            selectedIndex = 0
                        )
                    )
                }

                else ->
                    MenuTransition(this)
            }
        }
    }

    data class Feeds(
        val categoryName: String,
        val categoryFeeds: List<PodcastFeed>,
        val allFeeds: List<PodcastFeed>,
        val slots: List<SlotState>,
        override val selectedIndex: Int = 0
    ) : MenuState() {

        override val itemCount: Int get() = categoryFeeds.size

        override val title = categoryName

        override fun copyWithIndex(newIndex: Int) = copy(selectedIndex = newIndex)

        override fun reduce(event: MenuEvent): MenuTransition {
            return when (event) {

                MenuEvent.Confirm -> {
                    val selectedFeed = categoryFeeds[selectedIndex]
                    val feedIndex = allFeeds.indexOf(selectedFeed)

                    MenuTransition(
                        newState = Episodes(
                            feedIndex = feedIndex,
                            categoryName = categoryName,
                            episodes = selectedFeed.episodes,
                            slots = slots,
                            selectedIndex = 0
                        )
                    )
                }

                else ->
                    MenuTransition(this)
            }
        }
    }

    data class Episodes(
        val feedIndex: Int,
        val episodes: List<PodcastEpisode>,
        val categoryName: String?,
        val slots: List<SlotState>,
        override val selectedIndex: Int = 0
    ) : MenuState() {

        override val itemCount: Int get() = episodes.size

        override val title = "Episodes"

        override fun copyWithIndex(newIndex: Int) = copy(selectedIndex = newIndex)

        override fun reduce(event: MenuEvent): MenuTransition {
            return when (event) {

                MenuEvent.Confirm -> {
                    val episode = episodes[selectedIndex]

                    val alreadyDownloaded =
                        slots.any {
                            it.feedIndex == feedIndex &&
                                    it.episodeIndex == selectedIndex
                        }

                    val rows =
                        if (alreadyDownloaded)
                            listOf(ActionRow.AlreadyDownloaded)
                        else
                            listOf(ActionRow.Download)

                    MenuTransition(
                        newState = EpisodeDetail(
                            feedIndex = feedIndex,
                            episodeIndex = selectedIndex,
                            episode = episode,
                            origin = EpisodeDetail.Origin.EPISODES,
                            actionRows = rows,
                            selectedIndex = 0
                        )
                    )
                }

                else ->
                    MenuTransition(this)
            }
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

        override fun reduce(event: MenuEvent): MenuTransition {
            return when (event) {

                MenuEvent.Confirm -> {

                    val action = actionRows.getOrNull(selectedIndex)
                        ?: return MenuTransition(this)

                    when (action) {

                        ActionRow.Download ->
                            MenuTransition(
                                newState = copy(
                                    isDownloading = true,
                                    actionRows = listOf(
                                        ActionRow.Downloading(),
                                        ActionRow.Cancel
                                    ),
                                    selectedIndex = 1
                                ),
                                effects = listOf(
                                    MenuEffect.StartDownload(this)
                                )
                            )

                        ActionRow.Cancel ->
                            MenuTransition(
                                newState = copy(
                                    isDownloading = false,
                                    actionRows = listOf(ActionRow.Download),
                                    selectedIndex = 0
                                ),
                                effects = listOf(
                                    MenuEffect.CancelDownload(this)
                                )
                            )

                        ActionRow.Delete ->
                            MenuTransition(
                                newState = this, // temporary
                                effects = listOf(
                                    MenuEffect.DeleteEpisode(this),
                                    MenuEffect.BuildDownloadState
                                )
                            )

                        else ->
                            MenuTransition(this)
                    }
                }

                else ->
                    MenuTransition(this)
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

        override fun reduce(event: MenuEvent): MenuTransition {
            return when (event) {

                MenuEvent.Confirm -> {
                    val slot = slots[selectedIndex]

                    MenuTransition(
                        newState = NowPlaying(
                            slot = slot,
                            durationMs = 0L,
                            positionMs = 0L,
                            isPlaying = true
                        ),
                        effects = listOf(MenuEffect.StartPlayback(slot))
                    )
                }

                else -> MenuTransition(this)
            }
        }
    }

    data class NowPlaying(
        val slot: SlotState,
        val durationMs: Long,
        val positionMs: Long,
        val isPlaying: Boolean,
        override val selectedIndex: Int = 0
    ) : MenuState() {
        override val itemCount: Int = 1

        override val title = "Now Playing"

        override fun reduce(event: MenuEvent): MenuTransition {
            return when (event) {

                MenuEvent.PlayPause -> {
                    MenuTransition(
                        newState = copy(isPlaying = !isPlaying),
                        effects = listOf(MenuEffect.TogglePlayPause)
                    )
                }

                is MenuEvent.PlaybackProgress -> {
                    MenuTransition(
                        newState = copy(
                            positionMs = event.positionMs,
                            durationMs = event.durationMs,
                            isPlaying = event.isPlaying
                        )
                    )
                }

                MenuEvent.ScanForwardDown ->
                    MenuTransition(
                        this,
                        effects = listOf(MenuEffect.StartScanForward)
                    )

                MenuEvent.ScanForwardUp ->
                    MenuTransition(
                        this,
                        effects = listOf(MenuEffect.StopScan)
                    )

                MenuEvent.ScanBackDown ->
                    MenuTransition(
                        this,
                        effects = listOf(MenuEffect.StartScanBack)
                    )

                MenuEvent.ScanBackUp ->
                    MenuTransition(
                        this,
                        effects = listOf(MenuEffect.StopScan)
                    )

                MenuEvent.MenuShortPress ->
                    MenuTransition(
                        newState = this, // placeholder until we build the actual menu state
                        effects = listOf(
                            MenuEffect.StopPlayback,
                            MenuEffect.ExitNowPlaying
                        )
                    )

                else -> MenuTransition(this)
            }
        }

        override fun copyWithIndex(newIndex: Int) = copy(selectedIndex = newIndex)
    }

}
