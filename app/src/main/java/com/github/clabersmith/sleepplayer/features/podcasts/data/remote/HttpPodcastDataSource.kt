package com.github.clabersmith.sleepplayer.features.podcasts.data.remote

import com.github.clabersmith.sleepplayer.features.podcasts.data.mapper.toDomain
import com.github.clabersmith.sleepplayer.features.podcasts.data.remote.dto.PodcastCatalogDto
import com.github.clabersmith.sleepplayer.features.podcasts.domain.model.PodcastFeed
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class HttpPodcastDataSource(
    private val client: HttpClient,
    private val url: String
) : PodcastDataSource {

    override suspend fun loadFeeds(): List<PodcastFeed> {
        val dto: PodcastCatalogDto =
            client.get(url).body()

        return dto.toDomain()
    }
}