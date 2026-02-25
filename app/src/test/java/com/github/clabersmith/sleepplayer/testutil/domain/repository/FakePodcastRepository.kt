package com.github.clabersmith.sleepplayer.testutil.domain.repository

import com.github.clabersmith.sleepplayer.features.podcasts.domain.model.PodcastEpisode
import com.github.clabersmith.sleepplayer.features.podcasts.domain.model.PodcastFeed
import com.github.clabersmith.sleepplayer.features.podcasts.domain.repository.PodcastRepository

class FakePodcastRepository : PodcastRepository {
    override suspend fun getFeeds(): List<PodcastFeed> {
        return listOf(
            PodcastFeed(
                id = "1",
                title = "Test Podcast 1",
                artworkUrl = "",
                category = "Relaxation",
                episodes = listOf(
                    PodcastEpisode(
                        id = "ep1",
                        title = "Episode 1",
                        description = "Test",
                        audioUrl = ""
                    )
                )
            )
        )
    }

    override suspend fun getCategories(): List<String> {
        return listOf("Sleep", "Relaxation")
    }
}