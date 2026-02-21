package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuState
import com.github.clabersmith.sleepplayer.features.podcasts.data.local.PersistedSlot
import com.github.clabersmith.sleepplayer.features.podcasts.data.local.PersistedSlotRepository
import com.github.clabersmith.sleepplayer.features.podcasts.data.local.SlotRepository
import com.github.clabersmith.sleepplayer.features.podcasts.domain.model.PodcastEpisode
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import org.junit.Assert.assertFalse

class MenuViewModelTest() {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeRepository = object : PodcastRepository {
        override suspend fun getFeeds(): List<PodcastFeed> {
            return listOf(
                PodcastFeed(
                    id = "1",
                    title = "Test Podcast 1",
                    artworkUrl = "",
                    category = "Relaxation",
                    episodes = listOf(
                        PodcastEpisode(
                            id = "ep1",
                            title = "Episode 1",
                            description = "Test",
                            audioUrl = ""
                        )
                    )
                )
            )
        }

        override suspend fun getCategories(): List<String> {
            return listOf("Sleep", "Relaxation")
        }
    }

    private val fakePersistedSlotRepository = object : SlotRepository {

        var saveCallCount = 0

        private var stored: List<PersistedSlot> = emptyList()

        override suspend fun saveSlots(slots: List<PersistedSlot>) {
            stored = slots
        }

        override suspend fun loadSlots(): List<PersistedSlot> {
            return stored
        }

        override suspend fun clear() {
            stored = emptyList()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `loads feeds on init`() = runTest {

        val viewModel = MenuViewModel(
            fakeRepository,
            fakePersistedSlotRepository
        )

        advanceUntilIdle()

        // Navigate Home -> Downloads
                click(viewModel)
        // Downloads -> Categories
        click(viewModel)
        // Categories -> Feeds
        click(viewModel)
        val state = viewModel.menuState.value as MenuState.Feeds
        assertEquals("Test Podcast 1", state.feeds.first().title)
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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `moveSelection ignored in episode detail`() = runTest {

        val viewModel = MenuViewModel(
            fakeRepository,
            fakePersistedSlotRepository
        )

        navigateToEpisodeDetailDownload(viewModel)

        val initialIndex = viewModel.menuState.value.selectedIndex

        viewModel.moveSelection(1)

        assertEquals(initialIndex, viewModel.menuState.value.selectedIndex)
    }

    //--------------
    // Tests for actions in Episode Detail menu
    //--------------


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `download action adds slot`() = runTest {

        val viewModel = MenuViewModel(
            fakeRepository,
            fakePersistedSlotRepository
        )

        navigateToEpisodeDetailDownload(viewModel)

        // EpisodeDetail
        click(viewModel) // DOWNLOAD

        val state = viewModel.menuState.value as MenuState.Downloaded
        assertEquals(1, state.slots.size)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `delete action removes slot`() = runTest {

        val viewModel = MenuViewModel(
            fakeRepository,
            fakePersistedSlotRepository
        )

        navigateToEpisodeDetailDownload(viewModel)

        // Download first
        click(viewModel)

        // Open downloaded slot
        click(viewModel)

        // Delete
        click(viewModel)

        val state = viewModel.menuState.value as MenuState.Downloaded
        assertEquals(0, state.slots.size)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `already downloaded episode shows disabled action`() = runTest {

        val viewModel = MenuViewModel(
            fakeRepository,
            fakePersistedSlotRepository
        )

        navigateToEpisodeDetailDownload(viewModel)

        // Download first, should take back to Downloads menu
        click(viewModel)
        assertTrue(viewModel.menuState.value is MenuState.Downloaded)

        viewModel.moveSelection(1) // Move to 'Add New'

        navigateToEpisodeDetailDownloaded(viewModel)

        assertTrue(viewModel.menuState.value is MenuState.EpisodeDetail)

        val state = viewModel.menuState.value as MenuState.EpisodeDetail
        assertFalse(state.actionRows.firstOrNull { it.label == "Already Downloaded" }?.enabled ?: true)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `restoreSlots restores valid persisted slot`() = runTest {

        fakePersistedSlotRepository.saveSlots(
            listOf(
                PersistedSlot(
                    feedIndex = 0,
                    episodeIndex = 1,
                    episodeId = "ep1",
                    fileName = ""
                )
            )
        )

        val viewModel = MenuViewModel(
            fakeRepository,
            fakePersistedSlotRepository
        )

        advanceUntilIdle()

        viewModel.confirmSelection() // Home -> Downloads
        advanceUntilIdle()

        val state = viewModel.menuState.value as MenuState.Downloaded

        assertEquals(1, state.slots.size)
    }

    //--------------
    // Helper functions to set up specific menu states
    //--------------
    private fun createViewModelWithMenuState(
        state: MenuState //unused for now
    ): MenuViewModel {
        when(state) {
            is MenuState.Feeds -> {
                val viewModel = MenuViewModel(fakeRepository, fakePersistedSlotRepository)
                runTest {
                    navigateToFeedsMenu(viewModel)
                }
                return viewModel
            }

            is MenuState.EpisodeDetail -> {
                val viewModel = MenuViewModel(fakeRepository, fakePersistedSlotRepository)
                runTest {
                    navigateToFeedsMenu(viewModel)
                    navigateToEpisodeDetailDownload(viewModel)
                }
                return viewModel
            }
            else -> {
                //default state is Home
                return MenuViewModel(fakeRepository, fakePersistedSlotRepository)
            }
        }

    }

    private fun TestScope.navigateToFeedsMenu(
        viewModel: MenuViewModel
    ) {
        // Home -> Downloads
        click(viewModel)

        //Downloads -> Categories (via 'Add New')
        click(viewModel)

        //Categories -> Feeds (via 'Relaxation' category)
        click(viewModel)    }

    private fun TestScope.navigateToEpisodeDetailDownload(
        viewModel: MenuViewModel
    ) {
        navigateToFeedsMenu(viewModel)

        //Feeds -> Episodes
        click(viewModel)

        //Episodes -> Episode Detail
        click(viewModel)    }

    private fun TestScope.navigateToEpisodeDetailDownloaded(
        viewModel: MenuViewModel
    ) {
        //Downloads (Add New) -> Categories
        click(viewModel)

        //Categories -> Feeds (via 'Relaxation' category)
        click(viewModel)

        //Feeds -> Episodes
        click(viewModel)

        //Episodes -> Episode Detail
        click(viewModel)
    }

    private fun TestScope.click(viewModel: MenuViewModel) {
        viewModel.confirmSelection()
        advanceUntilIdle()
    }

}



