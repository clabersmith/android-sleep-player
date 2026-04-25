package com.github.clabersmith.sleepplayer

import android.content.Context
import com.github.clabersmith.sleepplayer.core.data.datastore.settingsDataStore
import com.github.clabersmith.sleepplayer.core.data.datastore.sfxSlotDataStore
import com.github.clabersmith.sleepplayer.core.data.datastore.slotDataStore
import com.github.clabersmith.sleepplayer.core.data.download.FileDownloader
import com.github.clabersmith.sleepplayer.core.playback.AudioPlaybackClock
import com.github.clabersmith.sleepplayer.core.playback.AudioPlayer
import com.github.clabersmith.sleepplayer.core.playback.ExoPodcastPlayer
import com.github.clabersmith.sleepplayer.core.playback.ExoSfxPlayer
import com.github.clabersmith.sleepplayer.core.playback.ExoWhiteNoisePlayer
import com.github.clabersmith.sleepplayer.core.playback.PlaybackClock
import com.github.clabersmith.sleepplayer.core.playback.SfxPlayer
import com.github.clabersmith.sleepplayer.core.playback.WhiteNoisePlayer
import com.github.clabersmith.sleepplayer.features.podcasts.data.download.DefaultPodcastDownloader
import com.github.clabersmith.sleepplayer.features.podcasts.data.local.AudioFileStorage
import com.github.clabersmith.sleepplayer.features.podcasts.data.local.PersistedSettingsRepository
import com.github.clabersmith.sleepplayer.features.podcasts.data.local.PersistedSlotRepository
import com.github.clabersmith.sleepplayer.features.podcasts.data.local.SettingsRepository
import com.github.clabersmith.sleepplayer.features.podcasts.data.local.SlotRepository
import com.github.clabersmith.sleepplayer.features.podcasts.data.remote.HttpClientProvider
import com.github.clabersmith.sleepplayer.features.podcasts.data.remote.HttpPodcastDataSource
import com.github.clabersmith.sleepplayer.features.podcasts.data.repository.DefaultPodcastRepository
import com.github.clabersmith.sleepplayer.features.podcasts.domain.repository.PodcastRepository
import com.github.clabersmith.sleepplayer.features.sfx.data.download.DefaultSfxDownloader
import com.github.clabersmith.sleepplayer.features.sfx.data.local.PersistedSfxSlotRepository
import com.github.clabersmith.sleepplayer.features.sfx.data.remote.HttpSfxRemoteDataSource
import com.github.clabersmith.sleepplayer.features.sfx.data.repository.DefaultSfxRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppContainer(context: Context) {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val httpClient = HttpClientProvider.client

    private val dataSource = HttpPodcastDataSource(
        client = httpClient,
        url = "https://mypod-s3-demo-bucket.s3.us-east-1.amazonaws.com/podcasts.json"
    )

    val storage = AudioFileStorage(context)

    val fileDownloader = FileDownloader(httpClient)

    // -----------------------------
    // Podcast download support
    // -----------------------------
    val downloader = DefaultPodcastDownloader(fileDownloader, storage)

    val podcastRepository: PodcastRepository =
        DefaultPodcastRepository(dataSource)

    val persistedSlotRepository: SlotRepository
        = PersistedSlotRepository(context.slotDataStore)

    // -----------------------------
    // SFX download support
    // -----------------------------
    val sfxRemote = HttpSfxRemoteDataSource(
        client = httpClient,
        url = "https://mypod-s3-demo-bucket.s3.us-east-1.amazonaws.com/sfx_feeds.json"
    )
    val sfxSlotRepo = PersistedSfxSlotRepository(context.sfxSlotDataStore)
    val sfxDownloader = DefaultSfxDownloader(fileDownloader, storage)
    val sfxRepository = DefaultSfxRepository(sfxRemote, sfxDownloader, sfxSlotRepo)

    // -----------------------------
    // Settings
    // -----------------------------

    val persistedSettingsRepository: SettingsRepository
            = PersistedSettingsRepository(context.settingsDataStore)

    // -----------------------------
    // Audio playback
    // -----------------------------
    val playbackClock: PlaybackClock = AudioPlaybackClock(appScope)

    val podcastPlayer: AudioPlayer = ExoPodcastPlayer(context, playbackClock)

    val whiteNoisePlayer: WhiteNoisePlayer = ExoWhiteNoisePlayer(context)

    val sfxPlayer: SfxPlayer = ExoSfxPlayer(context)


}