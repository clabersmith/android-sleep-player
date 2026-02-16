package com.github.clabersmith.sleepplayer.features.podcasts.data.remote

import com.github.clabersmith.sleepplayer.features.podcasts.domain.model.PodcastFeed

interface PodcastDataSource {
    suspend fun loadFeeds(): List<PodcastFeed>
}