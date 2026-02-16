package com.github.clabersmith.sleepplayer.features.podcasts.domain.repository

import com.github.clabersmith.sleepplayer.features.podcasts.domain.model.PodcastFeed

interface PodcastRepository {

    suspend fun getFeeds(): List<PodcastFeed>
    suspend fun getCategories(): List<String>
}