package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model

import com.github.clabersmith.sleepplayer.features.podcasts.domain.model.PodcastFeed

sealed class MenuState(
    open val selectedIndex: Int = 0
) {

    data class Home(
        override val selectedIndex: Int = 0
    ) : MenuState()

    data class Downloaded(
        override val selectedIndex: Int = 0,
        val downloadedFeeds: List<PodcastFeed> = emptyList()
    ) : MenuState()

    data class Categories(
        override val selectedIndex: Int = 0
    ) : MenuState()

    data class Feeds(
        override val selectedIndex: Int = 0,
        val categoryName: String? = null
    ) : MenuState()

    data class Episodes(
        val feedIndex: Int,
        val categoryName: String? = null,
        override val selectedIndex: Int = 0
    ) : MenuState()

    data class EpisodeDetail(
        val feedIndex: Int,
        val episodeIndex: Int,
        val actionRows: List<ActionRow> = emptyList(), // new
        override val selectedIndex: Int = 0
    ) : MenuState(selectedIndex)
}

