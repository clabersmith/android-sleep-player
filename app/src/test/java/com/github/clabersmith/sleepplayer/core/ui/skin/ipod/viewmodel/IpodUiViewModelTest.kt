package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.viewmodel

import com.github.clabersmith.sleepplayer.features.podcasts.domain.model.PodcastEpisode
import com.github.clabersmith.sleepplayer.features.podcasts.domain.model.PodcastFeed
import com.github.clabersmith.sleepplayer.features.podcasts.domain.repository.PodcastRepository
import com.github.clabersmith.sleepplayer.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*
import org.junit.Rule

class IpodUiViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

//    @OptIn(ExperimentalCoroutinesApi::class)
//    @Test
//    fun `loads feeds on init`() = runTest {
//        val fakeRepository = object : PodcastRepository {
//            override suspend fun getFeeds(): List<PodcastFeed> {
//                return listOf(
//                    PodcastFeed(
//                        id = "1",
//                        title = "Test Podcast 1",
//                        artworkUrl = "https://example.com/artwork1.jpg",
//                        category = "Relaxation",
//                        episodes = listOf(
//                            PodcastEpisode(
//                                id = "ep3",
//                                title = "Episode 1",
//                                description = "First episode of Test Podcast 1",
//                                audioUrl = "https://example.com/episode1.mp3",
//                                durationSec = 3000
//                            )
//                        )
//                    )
//                )
//            }
//
//            override suspend fun getCategories(): List<String> {
//                return listOf("Sleep", "Relaxation")
//            }
//        }
//
//        val viewModel = IpodUiViewModel(fakeRepository)
//
//        advanceUntilIdle()
//
//        assertEquals(1, viewModel.feeds.value.size)
//    }
//
//    @Test
//    fun `rotate forward increments index`() {
//        val viewModel = createViewModelWithMenuSize(4)
//
//        viewModel.onWheelEvent(WheelEvent.Rotate(delta = 1F))
//
//        assertEquals(1, viewModel.selectedIndex.value)
//    }
//
//    @Test
//    fun `rotate backward wraps to last index`() {
//        val viewModel = createViewModelWithMenuSize(4)
//
//        viewModel.onWheelEvent(WheelEvent.Rotate(delta = -1F))
//
//        assertEquals(4, viewModel.selectedIndex.value)
//    }
//
//    private fun createViewModelWithMenuSize(
//        size: Int = 4 // unused for now
//    ): IpodUiViewModel {
//
//        val fakeRepository = object : PodcastRepository {
//            override suspend fun getFeeds(): List<PodcastFeed> {
//                return emptyList()
//            }
//
//            override suspend fun getCategories(): List<String> {
//                return emptyList()
//            }
//        }
//
//        return IpodUiViewModel(fakeRepository)
//    }
}