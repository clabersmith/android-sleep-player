package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model

import com.github.clabersmith.sleepplayer.features.podcasts.domain.model.PodcastEpisode
import com.github.clabersmith.sleepplayer.features.podcasts.domain.model.PodcastFeed

/**
 * Factory for creating [MenuState] instances used by the iPod skin UI.
 *
 * Centralizes construction of the different menu screens (home, downloads,
 * categories, feeds, episodes, episode detail, play, and now playing) using
 * provider functions to obtain the latest data.
 *
 * No coroutines or side effects.
 *
 * @param feedsProvider function that returns the current list of [PodcastFeed]s
 * @param categoriesProvider function that returns the current list of category names
 * @param slotsProvider function that returns the current list of [SlotState]s
 * @param maxSlots maximum number of slots available for downloads/plays
 */
class MenuStateFactory(
    private val feedsProvider: () -> List<PodcastFeed>,
    private val categoriesProvider: () -> List<String>,
    private val slotsProvider: () -> List<SlotState>,
    private val maxSlots: Int
) {

    fun home(selectedIndex: Int = 0) =
        MenuState.Home(selectedIndex)

    fun download(selectedIndex: Int = 0) =
        MenuState.Download(
            slots = slotsProvider(),
            maxSlots = maxSlots,
            selectedIndex = selectedIndex
        )

    fun categories(selectedIndex: Int = 0) =
        MenuState.Categories(
            categories = categoriesProvider().distinct().sorted(),
            selectedIndex = selectedIndex
        )

    fun feeds(category: String, selectedIndex: Int = 0) =
        MenuState.Feeds(
            categoryName = category,
            feeds = feedsProvider().filter { it.category == category },
            selectedIndex = selectedIndex
        )

    fun episodes(
        feedIndex: Int,
        categoryName: String?,
        selectedIndex: Int = 0
    ): MenuState.Episodes {
        val feed = feedsProvider()[feedIndex]

        return MenuState.Episodes(
            feedIndex = feedIndex,
            episodes = feed.episodes,
            categoryName = categoryName,
            selectedIndex = selectedIndex
        )
    }

    fun episodeDetail(
        feedIndex: Int,
        episodeIndex: Int,
        episode: PodcastEpisode,
        origin: MenuState.EpisodeDetail.Origin,
        selectedIndex: Int = 0
    ): MenuState.EpisodeDetail {

        val alreadyDownloaded = slotsProvider().any {
            it.feedIndex == feedIndex &&
                    it.episodeIndex == episodeIndex
        }

        val primaryAction = when {
            origin == MenuState.EpisodeDetail.Origin.DOWNLOAD ->
                ActionRow.Delete

            origin == MenuState.EpisodeDetail.Origin.EPISODES &&
                    alreadyDownloaded ->
                ActionRow.AlreadyDownloaded

            else -> ActionRow.Download
        }

        return MenuState.EpisodeDetail(
            feedIndex = feedIndex,
            episodeIndex = episodeIndex,
            episode = episode,
            actionRows = listOf(primaryAction),
            origin = origin,
            selectedIndex = selectedIndex
        )
    }

    fun play(selectedIndex: Int = 0) =
        MenuState.Play(
            slots = slotsProvider(),
            selectedIndex = selectedIndex
        )

    fun nowPlaying(
        slot: SlotState,
        isPlaying: Boolean = false
    ) = MenuState.NowPlaying(
        slot = slot,
        durationMs = 0L,
        positionMs = 0L,
        isPlaying = isPlaying
    )
}