package com.github.clabersmith.sleepplayer.features.podcasts.data.remote.dto
import kotlinx.serialization.Serializable

@Serializable
data class PodcastFeedDto(
    val id: String,
    val title: String,
    val category: String,
    val artworkUrl: String,
    val rssUrl: String,
    val episodes: List<PodcastEpisodeDto>
)
