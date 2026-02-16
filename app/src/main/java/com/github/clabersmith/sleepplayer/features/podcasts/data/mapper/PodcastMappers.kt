package com.github.clabersmith.sleepplayer.features.podcasts.data.mapper

import com.github.clabersmith.sleepplayer.features.podcasts.data.remote.dto.PodcastCatalogDto
import com.github.clabersmith.sleepplayer.features.podcasts.data.remote.dto.PodcastEpisodeDto
import com.github.clabersmith.sleepplayer.features.podcasts.data.remote.dto.PodcastFeedDto
import com.github.clabersmith.sleepplayer.features.podcasts.domain.model.PodcastEpisode
import com.github.clabersmith.sleepplayer.features.podcasts.domain.model.PodcastFeed

fun PodcastCatalogDto.toDomain(): List<PodcastFeed> =
    feeds.map { it.toDomain() }

fun PodcastEpisodeDto.toDomain() =
    PodcastEpisode(
        id = id,
        title = title,
        description = description,
        audioUrl = audioUrl,
        durationSec = durationSec,
        published = published
    )

fun PodcastFeedDto.toDomain() =
    PodcastFeed(
        id = id,
        title = title,
        artworkUrl = artworkUrl,
        category = category,          // ← your new field
        episodes = episodes.map { it.toDomain() }
    )