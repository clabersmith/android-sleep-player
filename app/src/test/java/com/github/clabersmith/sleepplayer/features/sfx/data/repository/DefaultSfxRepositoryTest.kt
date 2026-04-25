package com.github.clabersmith.sleepplayer.features.sfx.data.repository

import com.github.clabersmith.sleepplayer.features.sfx.data.local.PersistedSfxSlot
import com.github.clabersmith.sleepplayer.features.sfx.data.remote.dto.SfxFeedDto
import com.github.clabersmith.sleepplayer.features.sfx.data.remote.dto.SfxItemDto
import com.github.clabersmith.sleepplayer.testutil.data.download.FakeSfxDownloader
import com.github.clabersmith.sleepplayer.testutil.data.remote.FakeSfxRemoteDataSource
import com.github.clabersmith.sleepplayer.testutil.domain.repository.FakeSfxSlotRepository
import org.junit.Assert.*
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.Instant

class DefaultSfxRepositoryTest {

    @Test
    fun `only outdated slots are downloaded`() = runTest {

        fun ts(millis: Long) = Instant.ofEpochMilli(millis).toString()

        val feed = SfxFeedDto(
            items = listOf(
                SfxItemDto(name = "sfx_mix_1.mp3", url = "url1", timestamp = "1970-01-01T00:00:00Z"),
                SfxItemDto(name = "sfx_mix_2.mp3", url = "url2", timestamp = "1970-01-01T00:00:01Z"),
                SfxItemDto(name = "sfx_mix_3.mp3", url = "url3", timestamp = "1970-01-01T00:00:02Z"),
                SfxItemDto(name = "sfx_mix_4.mp3", url = "url4", timestamp = "1970-01-01T00:00:03Z"),
            )
        )

        val remote = FakeSfxRemoteDataSource(feed)

        val slots = mutableListOf(
            PersistedSfxSlot(index = 0, fileName = "", lastDownloadedAt = 0),     // up-to-date
            PersistedSfxSlot(index = 1, fileName = "", lastDownloadedAt = 500),   // outdated
            PersistedSfxSlot(index = 2, fileName = "", lastDownloadedAt = 0),     // outdated
            PersistedSfxSlot(index = 3, fileName = "", lastDownloadedAt = 3000),  // up-to-date
        )

        val slotRepo = FakeSfxSlotRepository(slots)
        val downloader = FakeSfxDownloader()

        val repo = DefaultSfxRepository(
            remote = remote,
            downloader = downloader,
            slotRepo = slotRepo
        )

        repo.startDownload()

        assertEquals(listOf(1, 2), downloader.downloaded)
    }

}