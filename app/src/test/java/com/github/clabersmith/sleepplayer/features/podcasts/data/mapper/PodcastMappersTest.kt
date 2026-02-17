package com.github.clabersmith.sleepplayer.features.podcasts.data.mapper

import com.github.clabersmith.sleepplayer.features.podcasts.data.remote.dto.PodcastEpisodeDto
import com.github.clabersmith.sleepplayer.features.podcasts.data.remote.dto.PodcastFeedDto
import org.junit.Assert.assertEquals
import org.junit.Test

class PodcastMappersTest {
    @Test
    fun `maps feed dto to domain correctly`() {

        val episodeDto = PodcastEpisodeDto(
            id = "ep1",
            title = "Episode 1",
            description = "The first episode",
            audioUrl = "https://example.com/audio.mp3",
            durationSec = 1800,
            published = "2024-01-01T00:00:00Z"
        )

        val feedDto = PodcastFeedDto(
            id = "feed1",
            title = "Sleep Podcast",
            category = "Sleep",
            artworkUrl = "https://example.com/artwork.jpg",
            rssUrl = "https://example.com/rss.xml",
            episodes = listOf(episodeDto)
        )

        val domain = feedDto.toDomain()

        assertEquals("feed1", domain.id)
        assertEquals("Sleep Podcast", domain.title)
        assertEquals("Sleep", domain.category)
        assertEquals("https://example.com/artwork.jpg", domain.artworkUrl)

        assertEquals(1, domain.episodes.size)

        val episode = domain.episodes.first()
        assertEquals("ep1", episode.id)
        assertEquals("Episode 1", episode.title)
        assertEquals("https://example.com/audio.mp3", episode.audioUrl)
        assertEquals(1800, episode.durationSec)
    }

    @Test
    fun `maps feed dto with empty episodes`() {

        val feedDto = PodcastFeedDto(
            id = "feed1",
            title = "Empty Podcast, No Episodes Yet",
            category = "Sleep",
            artworkUrl = "https://example.com/artwork.jpg",
            rssUrl = "https://example.com/rss.xml",
            episodes = emptyList()
        )

        val domain = feedDto.toDomain()

        assertEquals(0, domain.episodes.size)
    }
}