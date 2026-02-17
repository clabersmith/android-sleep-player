package com.github.clabersmith.sleepplayer.features.podcasts.data.repository

import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*
import com.github.clabersmith.sleepplayer.testutil.FakePodcastDataSource

class DefaultPodcastRepositoryTest {

    @Test
    fun `returns mapped feeds`() = runTest {
        val repository = DefaultPodcastRepository(
            remote = FakePodcastDataSource()
        )

        val feeds = repository.getFeeds()

        assertEquals(2, feeds.size)
        assertEquals("Test Podcast 1", feeds.first().title)
    }
}