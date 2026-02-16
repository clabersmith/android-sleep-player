package com.github.clabersmith.sleepplayer.features.podcasts.data.repository

import com.github.clabersmith.sleepplayer.features.podcasts.data.remote.PodcastDataSource
import com.github.clabersmith.sleepplayer.features.podcasts.domain.model.PodcastFeed
import com.github.clabersmith.sleepplayer.features.podcasts.domain.repository.PodcastRepository

class DefaultPodcastRepository(
    private val remote: PodcastDataSource
) : PodcastRepository {

    private var cachedFeeds: List<PodcastFeed>? = null

    override suspend fun getFeeds(): List<PodcastFeed> {
        cachedFeeds?.let { return it }

        val feeds = remote.loadFeeds()
        cachedFeeds = feeds
        return feeds
    }

    override suspend fun getCategories(): List<String> {
        return getFeeds()
            .map { it.category }
            .distinct()
    }
}