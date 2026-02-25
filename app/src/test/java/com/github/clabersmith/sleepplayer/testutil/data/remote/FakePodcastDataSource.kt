package com.github.clabersmith.sleepplayer.testutil.data.remote

import com.github.clabersmith.sleepplayer.features.podcasts.data.remote.PodcastDataSource
import com.github.clabersmith.sleepplayer.features.podcasts.domain.model.PodcastEpisode
import com.github.clabersmith.sleepplayer.features.podcasts.domain.model.PodcastFeed

class FakePodcastDataSource : PodcastDataSource {
    override suspend fun loadFeeds(): List<PodcastFeed> {
        return listOf(
            PodcastFeed(
                id = "1",
                title = "Test Podcast 1",
                artworkUrl = "https://example.com/artwork1.jpg",
                category = "Sleep",
                episodes = listOf(
                    PodcastEpisode(
                        id = "ep1",
                        title = "Episode 1",
                        description = "First episode of Test Podcast 1",
                        audioUrl = "https://example.com/episode1.mp3",
                        durationSec = 3600
                    ),
                    PodcastEpisode(
                        id = "ep2",
                        title = "Episode 2",
                        description = "Second episode of Test Podcast 1",
                        audioUrl = "https://example.com/episode2.mp3",
                        durationSec = 4200
                    )
                )
            ),
            PodcastFeed(
                id = "2",
                title = "Test Podcast 2",
                artworkUrl = "https://example.com/artwork2.jpg",
                category = "Relaxation",
                episodes = listOf(
                    PodcastEpisode(
                        id = "ep3",
                        title = "Episode 1",
                        description = "First episode of Test Podcast 2",
                        audioUrl = "https://example.com/episode3.mp3",
                        durationSec = 3000
                    )
                )
            )
        )
    }
}