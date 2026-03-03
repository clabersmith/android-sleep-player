package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model

import com.github.clabersmith.sleepplayer.features.podcasts.domain.model.PodcastEpisode
import com.github.clabersmith.sleepplayer.features.podcasts.domain.model.PodcastFeed

sealed class MenuState() {
    abstract val context: MenuContext

    abstract val selectedIndex: Int
    abstract val itemCount: Int

    abstract val title: String

    abstract fun reduce(event: MenuEvent): MenuTransition

    abstract fun copyWithIndex(newIndex: Int): MenuState

    abstract fun withContext(context: MenuContext): MenuState

    protected fun handleCommonEvents(event: MenuEvent): MenuTransition? {
        return when (event) {
            MenuEvent.ScanForwardDown -> MenuTransition(this, effects = listOf(MenuEffect.StartScanForward))
            MenuEvent.ScanForwardUp   -> MenuTransition(this, effects = listOf(MenuEffect.StopScan))
            MenuEvent.ScanBackDown    -> MenuTransition(this, effects = listOf(MenuEffect.StartScanBack))
            MenuEvent.ScanBackUp      -> MenuTransition(this, effects = listOf(MenuEffect.StopScan))
            MenuEvent.MenuLongPress   -> MenuTransition(Home(context))
            else -> null
        }
    }

    data class Home(
        override val context: MenuContext,
        override val selectedIndex: Int = 0
    ) : MenuState() {

        init {
            println("Home constructed: selectedIndex=$selectedIndex")
        }

        override val itemCount: Int get() = 0 //not used, Home has dynamic items based on now playing state

        override val title = "Menu"

        override fun copyWithIndex(newIndex: Int) = copy(selectedIndex = newIndex)

        override fun withContext(context: MenuContext) = copy(context = context)

        override fun reduce(event: MenuEvent): MenuTransition {

            println("Home reducer called with event: $event, selectedIndex: $selectedIndex")

            handleCommonEvents(event)?.let { return it }

            return when (event) {
                MenuEvent.Confirm -> {
                    when(selectedIndex) {
                        0 ->
                            MenuTransition(
                                newState = Podcasts(context)
                            )

                        1 ->
                            MenuTransition(
                                newState = this
                            )

                        else -> MenuTransition(this)
                    }
                }

                else -> MenuTransition(this)
            }

        }
    }

    data class Podcasts(
        override val context: MenuContext,
        override val selectedIndex: Int = 0
    ) : MenuState() {

        override val itemCount: Int get() = 2

        override val title = "Podcasts"

        override fun copyWithIndex(newIndex: Int) = copy(selectedIndex = newIndex)

        override fun withContext(context: MenuContext) = copy(context = context)

        override fun reduce(event: MenuEvent): MenuTransition {
            handleCommonEvents(event)?.let { return it }

            return when (event) {

                MenuEvent.Confirm -> {
                    when (selectedIndex) {

                        0 -> MenuTransition(
                            newState = Play(
                                context,
                                selectedIndex = 0
                            )
                        )

                        1 -> MenuTransition(
                            newState = Download(
                                context,
                                selectedIndex = 0
                            )
                        )

                        else -> MenuTransition(this)
                    }
                }

                MenuEvent.MenuShortPress -> MenuTransition(
                    Home(
                        context,
                        selectedIndex = 0
                    )
                )

                else -> MenuTransition(this)
            }

        }
    }

    data class Download(
        override val context: MenuContext,
        override val selectedIndex: Int = 0
    ) : MenuState() {

        override val itemCount: Int
            get() = context.slots.size + if (context.slots.size < context.maxSlotsCount) 1 else 0

        override val title = "Downloads"

        override fun copyWithIndex(newIndex: Int) = copy(selectedIndex = newIndex)

        override fun withContext(context: MenuContext) = copy(context = context)

        override fun reduce(event: MenuEvent): MenuTransition {
            handleCommonEvents(event)?.let { return it }

            return when (event) {

                MenuEvent.Confirm -> {

                    val slotCount = context.slots.size
                    val hasAddNew = slotCount < context.maxSlotsCount
                    val addNewIndex = slotCount

                    println("hasAddNew: $hasAddNew, addNewIndex: $addNewIndex, selectedIndex: $selectedIndex")

                    when {
                        hasAddNew && selectedIndex == addNewIndex ->
                            MenuTransition(
                                newState = Categories(
                                    context = context,
                                    selectedIndex = 0
                                )
                            )

                        else -> {
                            val slot = context.slots[selectedIndex]

                            MenuTransition(
                                newState = EpisodeDetail(
                                    context,
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

                MenuEvent.MenuShortPress -> MenuTransition(
                    Podcasts(context)
                )

                else ->
                    MenuTransition(this)
            }
        }
    }

    data class Categories(
        override val context: MenuContext,
        override val selectedIndex: Int = 0
    ) : MenuState() {

        override val itemCount: Int get() = context.categories.size

        override val title = "Categories"

        override fun copyWithIndex(newIndex: Int) = copy(selectedIndex = newIndex)

        override fun withContext(context: MenuContext) = copy(context = context)

        override fun reduce(event: MenuEvent): MenuTransition {
            handleCommonEvents(event)?.let { return it }

            return when (event) {

                MenuEvent.Confirm -> {
                    val category = context.categories[selectedIndex]

                    println("feeds ${context.feeds} category $category")

                    val categoryFeeds = context.feeds
                        .filter { it.category == category }

                    MenuTransition(
                        newState = Feeds(
                            context = context,
                            categoryName = category,
                            categoryFeeds = categoryFeeds,
                            selectedIndex = 0
                        )
                    )
                }

                MenuEvent.MenuShortPress -> MenuTransition(
                    Download(
                        context,
                        selectedIndex = 0
                    )
                )

                else ->
                    MenuTransition(this)
            }
        }
    }

    data class Feeds(
        override val context: MenuContext,
        val categoryName: String,
        val categoryFeeds: List<PodcastFeed>,
        override val selectedIndex: Int = 0
    ) : MenuState() {

        override val itemCount: Int get() = categoryFeeds.size

        override val title = "$categoryName Podcasts"

        override fun copyWithIndex(newIndex: Int) = copy(selectedIndex = newIndex)

        override fun withContext(context: MenuContext) = copy(context = context)

        override fun reduce(event: MenuEvent): MenuTransition {
            handleCommonEvents(event)?.let { return it }

            return when (event) {

                MenuEvent.Confirm -> {
                    val selectedFeed = categoryFeeds[selectedIndex]
                    val feedIndex = context.feeds.indexOf(selectedFeed)

                    MenuTransition(
                        newState = Episodes(
                            context = context,
                            feedIndex = feedIndex,
                            episodes = selectedFeed.episodes,
                            categoryFeeds = categoryFeeds,

                            selectedIndex = 0
                        )
                    )
                }

                MenuEvent.MenuShortPress -> MenuTransition(
                    Categories(
                        context
                    )
                )

                else -> MenuTransition(this)
            }
        }
    }

    data class Episodes(
        override val context: MenuContext,
        val feedIndex: Int,
        val episodes: List<PodcastEpisode>,
        val categoryFeeds: List<PodcastFeed>,
        override val selectedIndex: Int = 0
    ) : MenuState() {

        override val itemCount: Int get() = episodes.size

        override val title = "Podcast Episodes"

        override fun copyWithIndex(newIndex: Int) = copy(selectedIndex = newIndex)

        override fun withContext(context: MenuContext) = copy(context = context)

        override fun reduce(event: MenuEvent): MenuTransition {
            handleCommonEvents(event)?.let { return it }

            return when (event) {

                MenuEvent.Confirm -> {
                    val episode = episodes[selectedIndex]

                    val alreadyDownloaded =
                        context.slots.any {
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
                            context = context,
                            feedIndex = feedIndex,
                            episodeIndex = selectedIndex,
                            episode = episode,
                            origin = EpisodeDetail.Origin.EPISODES,
                            actionRows = rows,
                            selectedIndex = 0
                        )
                    )
                }

                MenuEvent.MenuShortPress -> MenuTransition(
                    Feeds(
                        context = context,
                        categoryName = context.feeds[feedIndex].category,
                        categoryFeeds = categoryFeeds,
                    )
                )

                else ->
                    MenuTransition(this)
            }
        }
    }

    data class EpisodeDetail(
        override val context: MenuContext,
        val feedIndex: Int,
        val episodeIndex: Int,
        val episode: PodcastEpisode,
        val actionRows: List<ActionRow> = emptyList(),
        val isDownloading: Boolean = false,
        val origin: Origin,
        override val selectedIndex: Int = 0
    ) : MenuState() {
        override val itemCount: Int get() = actionRows.size

        override val title = "Episode Detail"

        override fun copyWithIndex(newIndex: Int) = copy(selectedIndex = newIndex)

        override fun withContext(context: MenuContext) = copy(context = context)

        override fun reduce(event: MenuEvent): MenuTransition {

            handleCommonEvents(event)?.let { return it }

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
                                newState = Download(context, selectedIndex = 0),
                                effects = listOf(
                                    MenuEffect.DeleteEpisode(this)
                                )
                            )

                        else -> MenuTransition(this)
                    }
                }

                MenuEvent.MenuShortPress -> {
                    when (origin) {
                        Origin.DOWNLOAD -> MenuTransition(
                            Download(context)
                        )

                        Origin.EPISODES -> MenuTransition(
                            Episodes(
                                context = context,
                                feedIndex = feedIndex,
                                episodes = context.feeds[feedIndex].episodes,
                                categoryFeeds = context.feeds.filter { it.category == context.feeds[feedIndex].category },
                                selectedIndex = episodeIndex
                            )
                        )

                        else -> MenuTransition(this)
                    }
                }

                else ->
                    MenuTransition(this)
            }
        }

        enum class Origin {
            EPISODES,
            DOWNLOAD,
        }
    }

    data class Play(
        override val context: MenuContext,
        override val selectedIndex: Int = 0
    ) : MenuState() {
        override val itemCount: Int get() = context.slots.size

        override val title = "Play"

        override fun copyWithIndex(newIndex: Int) = copy(selectedIndex = newIndex)

        override fun withContext(context: MenuContext) = copy(context = context)

        override fun reduce(event: MenuEvent): MenuTransition {

            handleCommonEvents(event)?.let { return it }

            return when (event) {

                MenuEvent.Confirm -> {
                    if(context.slots.isEmpty()) {
                        return MenuTransition(this)
                    }

                    val slot = context.slots[selectedIndex]

                    println("NowPlaying state created with slot: $slot")

                    MenuTransition(
                        newState = NowPlaying(
                            context = context,
                            slot = slot,
                            origin = NowPlaying.Origin.PLAY,
                            selectedIndex = 0
                        ),
                        effects = listOf(
                            MenuEffect.CheckStartPlayback(slot),
                            MenuEffect.GoToNowPlaying(slot, NowPlaying.Origin.PLAY)
                        )
                    )
                }

                MenuEvent.MenuShortPress -> {
                    MenuTransition (
                        newState = Podcasts(context, selectedIndex = 0)
                    )
                }

                else -> MenuTransition(this)
            }
        }
    }

    data class NowPlaying(
        override val context: MenuContext,
        val slot: SlotState,
        val origin: Origin,
        override val selectedIndex: Int = 0
    ) : MenuState() {
        override val itemCount: Int = 1

        override val title = "Now Playing"

        override fun withContext(context: MenuContext) = copy(context = context)

        override fun reduce(event: MenuEvent): MenuTransition {
            println("NowPlaying reducer called with slot: $slot")
            handleCommonEvents(event)?.let { return it }

            return when (event) {

                MenuEvent.MenuShortPress ->

                    when (origin) {
                        Origin.PLAY -> MenuTransition(
                            Play(context)
                        )

                        Origin.HOME -> MenuTransition(
                            Home(context, selectedIndex = 1)
                        )

                        else -> MenuTransition(this)
                    }

                    else -> MenuTransition(this)
            }
        }

        enum class Origin {
            HOME,
            PLAY
        }

        override fun copyWithIndex(newIndex: Int) = copy(selectedIndex = newIndex)
    }

}
