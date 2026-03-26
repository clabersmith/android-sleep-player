package com.github.clabersmith.sleepplayer

import android.content.Context
import com.github.clabersmith.sleepplayer.core.data.datastore.slotDataStore
import com.github.clabersmith.sleepplayer.core.playback.AudioPlayer
import com.github.clabersmith.sleepplayer.core.playback.ExoAudioPlayer
import com.github.clabersmith.sleepplayer.core.playback.ExoWhiteNoisePlayer
import com.github.clabersmith.sleepplayer.core.playback.WhiteNoisePlayer
import com.github.clabersmith.sleepplayer.features.podcasts.data.download.PodcastDownloader
import com.github.clabersmith.sleepplayer.features.podcasts.data.local.AudioFileStorage
import com.github.clabersmith.sleepplayer.features.podcasts.data.local.PersistedSlotRepository
import com.github.clabersmith.sleepplayer.features.podcasts.data.local.SlotRepository
import com.github.clabersmith.sleepplayer.features.podcasts.data.remote.HttpClientProvider
import com.github.clabersmith.sleepplayer.features.podcasts.data.remote.HttpPodcastDataSource
import com.github.clabersmith.sleepplayer.features.podcasts.data.repository.DefaultPodcastRepository
import com.github.clabersmith.sleepplayer.features.podcasts.domain.repository.PodcastRepository

class AppContainer(context: Context) {

    private val httpClient = HttpClientProvider.client

    private val dataSource = HttpPodcastDataSource(
        client = httpClient,
        url = "https://mypod-s3-demo-bucket.s3.us-east-1.amazonaws.com/podcasts.json"
    )

    val storage = AudioFileStorage(context)

    val downloader = PodcastDownloader(httpClient, storage)

    val podcastRepository: PodcastRepository =
        DefaultPodcastRepository(dataSource)

    val persistedSlotRepository: SlotRepository
        = PersistedSlotRepository(context.slotDataStore)

    val audioPlayer: AudioPlayer = ExoAudioPlayer(context)

    val whiteNoisePlayer: WhiteNoisePlayer = ExoWhiteNoisePlayer(context)
}