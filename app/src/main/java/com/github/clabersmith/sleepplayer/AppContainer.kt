package com.github.clabersmith.sleepplayer

import com.github.clabersmith.sleepplayer.features.podcasts.data.remote.HttpClientProvider
import com.github.clabersmith.sleepplayer.features.podcasts.data.remote.HttpPodcastDataSource
import com.github.clabersmith.sleepplayer.features.podcasts.data.repository.DefaultPodcastRepository
import com.github.clabersmith.sleepplayer.features.podcasts.domain.repository.PodcastRepository

class AppContainer {

    private val httpClient = HttpClientProvider.client

    private val dataSource = HttpPodcastDataSource(
        client = httpClient,
        url = "https://mypod-s3-demo-bucket.s3.us-east-1.amazonaws.com/podcasts.json"
    )

    val podcastRepository: PodcastRepository =
        DefaultPodcastRepository(dataSource)
}