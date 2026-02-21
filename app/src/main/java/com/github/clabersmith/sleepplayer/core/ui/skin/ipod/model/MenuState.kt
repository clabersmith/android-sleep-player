package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model

import com.github.clabersmith.sleepplayer.features.podcasts.domain.model.PodcastEpisode
import com.github.clabersmith.sleepplayer.features.podcasts.domain.model.PodcastFeed

sealed class MenuState(
    open val selectedIndex: Int = 0
) {

    data class Home(
        override val selectedIndex: Int = 0
    ) : MenuState()

    data class Download(
        val slots: List<SlotState>,
        val maxSlots: Int,
        override val selectedIndex: Int = 0
    ) : MenuState(selectedIndex)

    data class Categories(
        val categories: List<String>,
        override val selectedIndex: Int = 0
    ) : MenuState(selectedIndex)

    data class Feeds(
        val categoryName: String?,
        val feeds: List<PodcastFeed>,
        override val selectedIndex: Int = 0
    ) : MenuState(selectedIndex)

    data class Episodes(
        val feedIndex: Int,
        val episodes: List<PodcastEpisode>,
        val categoryName: String?,
        override val selectedIndex: Int = 0
    ) : MenuState()

    data class EpisodeDetail(
        val feedIndex: Int,
        val episodeIndex: Int,
        val episode: PodcastEpisode,
        val actionRows: List<ActionRow> = emptyList(),
        val isDownloading: Boolean = false,
        val origin: Origin,
        override val selectedIndex: Int = 0
    ) : MenuState(selectedIndex) {
        enum class Origin {
            EPISODES,
            DOWNLOAD
        }
    }
}

