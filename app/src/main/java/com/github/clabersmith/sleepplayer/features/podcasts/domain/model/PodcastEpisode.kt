package com.github.clabersmith.sleepplayer.features.podcasts.domain.model

data class PodcastEpisode(
    val id: String,
    val title: String,
    val description: String,
    val audioUrl: String,
    val durationSec: Int? = null,
    val published: String? = null,
    val fileName: String? = null,
    val feedName: String? = null,
)