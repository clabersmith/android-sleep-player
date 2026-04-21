package com.github.clabersmith.sleepplayer.features.sfx.data.repository

import com.github.clabersmith.sleepplayer.features.sfx.data.local.PersistedSfxSlot
import com.github.clabersmith.sleepplayer.features.sfx.data.remote.dto.SfxFeedDto
import com.github.clabersmith.sleepplayer.features.sfx.data.remote.dto.SfxFeedItemDto
import com.github.clabersmith.sleepplayer.testutil.data.download.FakeSfxDownloader
import com.github.clabersmith.sleepplayer.testutil.data.remote.FakeSfxRemoteDataSource
import com.github.clabersmith.sleepplayer.testutil.domain.repository.FakeSfxSlotRepository
import org.junit.Assert.*
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultSfxRepositoryTest {

    @Test
    fun `only outdated slots are downloaded`() = runTest {

        // Given feed (remote state)
        val feed = SfxFeedDto(
            items = listOf(
                SfxFeedItemDto(index = 1, url = "url1", lastModified = 100),
                SfxFeedItemDto(index = 2, url = "url2", lastModified = 200),
                SfxFeedItemDto(index = 3, url = "url3", lastModified = 300),
                SfxFeedItemDto(index = 4, url = "url4", lastModified = 400),
            )
        )

        val remote = FakeSfxRemoteDataSource(feed)

        // Local state:
        // slot 1 is up-to-date
        // slot 2 outdated
        // slot 3 outdated
        // slot 4 up-to-date
        val slots = mutableListOf(
            PersistedSfxSlot(index = 1, lastDownloadedAt = 100),
            PersistedSfxSlot(index = 2, lastDownloadedAt = 150),
            PersistedSfxSlot(index = 3, lastDownloadedAt = 0),
            PersistedSfxSlot(index = 4, lastDownloadedAt = 400),
        )

        val slotRepo = FakeSfxSlotRepository(slots)
        val downloader = FakeSfxDownloader()

        val repo = DefaultSfxRepository(
            remote = remote,
            downloader = downloader,
            slotRepo = slotRepo
        )

        // When
        repo.startDownload()

        // Then
        assertEquals(listOf(2, 3), downloader.downloaded)
    }

    }