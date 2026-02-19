package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.viewmodel

import androidx.compose.animation.ExperimentalSharedTransitionApi
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuState
import com.github.clabersmith.sleepplayer.features.podcasts.domain.model.PodcastFeed
import com.github.clabersmith.sleepplayer.features.podcasts.domain.repository.PodcastRepository
import com.github.clabersmith.sleepplayer.testutil.MainDispatcherRule
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope

class MenuViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    val fakeRepository = object : PodcastRepository {
        override suspend fun getFeeds(): List<PodcastFeed> {
            return listOf(
                PodcastFeed(
                    id = "1",
                    title = "Test Podcast 1",
                    artworkUrl = "",
                    category = "Relaxation",
                    episodes = emptyList()
                )
            )
        }

        override suspend fun getCategories(): List<String> {
            return listOf("Sleep", "Relaxation")
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `loads feeds on init`() = runTest {

        // the feeds data is loaded in the init block of the ViewModel and is exposed via the menuState
        // and menuConfig state flows. We need to advance the test dispatcher until idle to ensure all
        // coroutines have completed before we can assert on the state.
        val viewModel = createViewModelWithMenuState(MenuState.Feeds())
        advanceUntilIdle()

        // Move the ViewModel into a state that uses feeds
        //navigateToFeedsMenu(viewModel);

        val state = viewModel.menuState.value
        assertTrue(state is MenuState.Feeds)

        val config = viewModel.menuConfig.value

        assertTrue(config.items.contains("Test Podcast 1"))
        assertTrue(config.items.contains("Back"))
    }

    @Test
    fun `rotate forward increments index`() {
        val viewModel = createViewModelWithMenuState(MenuState.Home())

        viewModel.moveSelection(1)

        val state = viewModel.menuState.value
        assertEquals(1, state.selectedIndex)
    }

    @Test
    fun `rotate backward wraps to last index`() {
        val viewModel = createViewModelWithMenuState(MenuState.Home())

        viewModel.moveSelection(-1)

        val state = viewModel.menuState.value
        assertEquals(3, state.selectedIndex)
    }

    @Test
    fun `forward wrap goes to zero`() {
        val viewModel = createViewModelWithMenuState(MenuState.Home())

        viewModel.moveSelection(3)
        viewModel.moveSelection(1)

        assertEquals(0, viewModel.menuState.value.selectedIndex)
    }

    @Test
    fun `moveSelection ignored in episode detail`() {
        val viewModel = createViewModelWithMenuState(MenuState.EpisodeDetail(feedIndex = 0, episodeIndex = 0))

        viewModel.moveSelection(1)

        assertEquals(0, viewModel.menuState.value.selectedIndex)
    }

    private fun createViewModelWithMenuState(
        state: MenuState //unused for now
    ): MenuViewModel {
        when(state) {
            is MenuState.Feeds -> {
                val viewModel = MenuViewModel(fakeRepository)
                runTest {
                    navigateToFeedsMenu(viewModel)
                }
                return viewModel
            }

            is MenuState.EpisodeDetail -> {
                val viewModel = MenuViewModel(fakeRepository)
                runTest {
                    navigateToFeedsMenu(viewModel)
                    navigateToEpisodes(viewModel)
                }
                return viewModel
            }
            else -> {
                //default state is Home
                return MenuViewModel(fakeRepository)
            }
        }

    }

    private suspend fun TestScope.navigateToFeedsMenu(
        viewModel: MenuViewModel
    ) {
        // Home -> Downloads
        viewModel.confirmSelection() // Move to 'Downloads'
        advanceUntilIdle()

        //Downloads -> Categories (via 'Add New')
        viewModel.confirmSelection()
        advanceUntilIdle()

        //Categories -> Feeds (via 'Relaxation' category)
        viewModel.confirmSelection()
        advanceUntilIdle()
    }

    private suspend fun TestScope.navigateToEpisodes(
        viewModel: MenuViewModel
    ) {
        navigateToFeedsMenu(viewModel)

        //Feeds -> Episodes
        viewModel.confirmSelection()
        advanceUntilIdle()

        //Episodes -> Episode Detail
        viewModel.confirmSelection()
        advanceUntilIdle()
    }

}

