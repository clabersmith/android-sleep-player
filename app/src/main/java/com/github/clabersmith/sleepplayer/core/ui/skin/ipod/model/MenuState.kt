package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model

import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.viewmodel.MenuActions
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

    open fun onConfirm(actions: MenuActions): MenuState {
        return this
    }

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

                MenuEvent.MenuShortPress -> {
                    MenuTransition(
                        newState = MenuState.Play(
                            slots = emptyList(), // temp; factory will fix later
                            selectedIndex = 0
                        ),
                        effects = listOf(MenuEffect.StopPlayback)
                    )
                }

                else -> MenuTransition(this)
            }
        }

        override fun copyWithIndex(newIndex: Int) = copy(selectedIndex = newIndex)
    }

}
