package com.github.clabersmith.sleepplayer.features.podcasts.data.remote.dto
import kotlinx.serialization.Serializable

@Serializable
data class PodcastEpisodeDto(
    val id: String,
    val title: String,
    val description: String,
    val audioUrl: String,
    val durationSec: Int? = null,
    val published: String? = null
)