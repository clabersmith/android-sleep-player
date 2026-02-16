package com.github.clabersmith.sleepplayer.features.podcasts.domain.model

data class PodcastFeed(
    val id: String,
    val title: String,
    val artworkUrl: String,
    val category: String,
    val episodes: List<PodcastEpisode>,
)