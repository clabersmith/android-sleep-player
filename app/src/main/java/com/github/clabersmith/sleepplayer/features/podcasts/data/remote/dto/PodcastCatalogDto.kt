package com.github.clabersmith.sleepplayer.features.podcasts.data.remote.dto
import kotlinx.serialization.Serializable

@Serializable
data class PodcastCatalogDto(
    val generatedAt: String,
    val feeds: List<PodcastFeedDto>
)