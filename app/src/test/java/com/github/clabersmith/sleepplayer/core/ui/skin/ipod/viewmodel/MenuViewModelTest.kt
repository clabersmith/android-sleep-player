package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.viewmodel

import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.ActionRow
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuState
import com.github.clabersmith.sleepplayer.features.podcasts.data.download.Downloader
import com.github.clabersmith.sleepplayer.features.podcasts.data.local.PersistedSlot
import com.github.clabersmith.sleepplayer.features.podcasts.data.local.SlotRepository
import com.github.clabersmith.sleepplayer.features.podcasts.domain.repository.PodcastRepository
import com.github.clabersmith.sleepplayer.testutil.MainDispatcherRule
import com.github.clabersmith.sleepplayer.testutil.data.local.FakePersistedSlotRepository
import com.github.clabersmith.sleepplayer.testutil.domain.repository.FakePodcastRepository
import com.github.clabersmith.sleepplayer.testutil.data.download.FakeDownloaderFailing
import com.github.clabersmith.sleepplayer.testutil.data.download.FakeDownloaderHanging
import com.github.clabersmith.sleepplayer.testutil.data.download.FakeDownloaderProgress
import com.github.clabersmith.sleepplayer.testutil.data.download.FakeDownloaderSuccess
import com.github.clabersmith.sleepplayer.testutil.data.local.FakeFileStorage
import com.github.clabersmith.sleepplayer.testutil.playback.FakePodcastPlayer
import com.github.clabersmith.sleepplayer.testutil.helpers.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before

@OptIn(ExperimentalCoroutinesApi::class)
class MenuViewModelTest() {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakePodcastRepository : PodcastRepository
    private lateinit var fakePersistedSlotRepository : SlotRepository
    private lateinit var fakeDownloaderSuccess : Downloader
    private lateinit var fakeFileStorage : FakeFileStorage
    private lateinit var fakePodcastPlayer : FakePodcastPlayer

    @Before
    fun setup() {
        //setup new text fixture for each test to ensure clean state
        fakePodcastRepository = FakePodcastRepository()
        fakePersistedSlotRepository = FakePersistedSlotRepository()
        fakeDownloaderSuccess = FakeDownloaderSuccess()
        fakeFileStorage = FakeFileStorage()
        fakePodcastPlayer = FakePodcastPlayer()
    }

    @Test
    fun `loads feeds on init`() = runTest {
        val viewModel = createNewViewModel()
        advanceUntilIdle()  // Wait for load/init to complete

        // Navigate Home -> Downloads
        click(viewModel)
        // Downloads -> Categories
        click(viewModel)
        // Categories -> Feeds
        click(viewModel)

        val state = viewModel.menuState.value as MenuState.Feeds
        assertEquals("Test Podcast 1", state.categoryFeeds.first().title)
    }

    @Test
    fun `rotate forward increments index`() = runTest {
        val viewModel = createNewViewModel()
        advanceUntilIdle()

        viewModel.moveSelection(1)

        val state = viewModel.menuState.value
        assertEquals(1, state.selectedIndex)
    }

    @Test
    fun `rotate backward wraps to last index`() = runTest {
        val viewModel = createNewViewModel()  // Home menu has 3 items, so index 0-2
        advanceUntilIdle()

        viewModel.moveSelection(-1)

        val state = viewModel.menuState.value
        assertEquals(2, state.selectedIndex)
    }

    @Test
    fun `forward wrap goes to zero`() = runTest {
        val viewModel = createNewViewModel() // Home menu has 3 items, so index 0-2
        advanceUntilIdle()

        viewModel.moveSelection(2)
        viewModel.moveSelection(1)

        assertEquals(0, viewModel.menuState.value.selectedIndex)
    }

    @Test
    fun `moveSelection ignored in episode detail`() = runTest {
        persistFakeSlot()
        val viewModel = createNewViewModel()
        advanceUntilIdle()

        navigateToEpisodeDetailDownload(viewModel)

        val initialIndex = viewModel.menuState.value.selectedIndex

        viewModel.moveSelection(1)

        assertEquals(initialIndex, viewModel.menuState.value.selectedIndex)
    }

    //--------------
    // Tests for actions in Episode Detail menu
    //--------------

    @Test
    fun `download enters downloading state`() = runTest {
        val viewModel = createNewViewModel()
        advanceUntilIdle()

        navigateToEpisodeDetailDownload(viewModel)

        viewModel.confirmSelection()

        val state = viewModel.menuState.value as MenuState.EpisodeDetail

        assertTrue(state.isDownloading)
        assertTrue(state.actionRows.first() is ActionRow.Downloading)
    }

    @Test
    fun `download action adds slot`() = runTest {
        val viewModel = createNewViewModel()
        advanceUntilIdle()

        navigateToEpisodeDetailDownload(viewModel)

        // EpisodeDetail
        click(viewModel) // DOWNLOAD

        advanceUntilIdle() // wait for download to complete

        val state = viewModel.menuState.value as MenuState.Download
        assertEquals(1, state.context.slots.size)
    }

    @Test
    fun `already downloaded episode shows correct action`() = runTest {
        val viewModel = createNewViewModel()
        advanceUntilIdle()

        navigateToEpisodeDetailDownload(viewModel)

        // Download first, should take back to Downloads menu
        click(viewModel)
        assertTrue(viewModel.menuState.value is MenuState.Download)

        val downloadState = viewModel.menuState.value as MenuState.Download

        viewModel.moveSelection(1) // Move to 'Add New'

        navigateToEpisodeDetailDownloaded(viewModel)

        assertTrue(viewModel.menuState.value is MenuState.EpisodeDetail)

        val state = viewModel.menuState.value as MenuState.EpisodeDetail
        assertTrue(state.actionRows.firstOrNull { it is ActionRow.AlreadyDownloaded } != null)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `cancel download returns to non-downloading state`() = runTest {
        persistFakeSlot()
        val fakeDownloaderHanging = FakeDownloaderHanging()
        val viewModel = createNewViewModel(fakeDownloaderHanging)
        advanceUntilIdle()

        navigateToEpisodeDetailDownload(viewModel)

        viewModel.confirmSelection() // start download
        advanceUntilIdle()

        viewModel.confirmSelection() // should be on "Cancel" row now
        advanceUntilIdle()

        val state = viewModel.menuState.value as MenuState.EpisodeDetail

        assertFalse(state.isDownloading)
        assertTrue(state.actionRows.first() is ActionRow.Download)
    }

    @Test
    fun `cancel download does not add slot`() = runTest {
        persistFakeSlot()
        val fakeDownloaderHanging = FakeDownloaderHanging()
        val viewModel = createNewViewModel(fakeDownloaderHanging)
        advanceUntilIdle()

        navigateToEpisodeDetailDownload(viewModel)

        viewModel.confirmSelection() // start
        advanceUntilIdle()

        viewModel.confirmSelection() // cancel
        advanceUntilIdle()

        assertTrue(fakePersistedSlotRepository.loadSlots().isEmpty())
    }

    @Test
    fun `progress updates downloading row`() = runTest {
        val progressFakeDownloader = FakeDownloaderProgress()
        val viewModel = createNewViewModel(progressFakeDownloader)
        advanceUntilIdle()

        navigateToEpisodeDetailDownload(viewModel)

        viewModel.confirmSelection()
        advanceUntilIdle()

        progressFakeDownloader.progressCallback(0.5f)

        val state = viewModel.menuState.value as MenuState.EpisodeDetail
        val downloadingRow = state.actionRows.first() as ActionRow.Downloading

        assertEquals(0.5f, downloadingRow.progress)
    }

    @Test
    fun `failed download does not add slot`() = runTest {
        persistFakeSlot()
        val fakeDownloaderFailing = FakeDownloaderFailing()
        val viewModel = createNewViewModel(fakeDownloaderFailing)
        advanceUntilIdle()

        navigateToEpisodeDetailDownload(viewModel)

        viewModel.confirmSelection()
        advanceUntilIdle()

        assertTrue(fakePersistedSlotRepository.loadSlots().isEmpty())
    }

    @Test
    fun `delete action removes slot`() = runTest {
        val viewModel = createNewViewModel()
        advanceUntilIdle()

        navigateToEpisodeDetailDownload(viewModel)

        // Download first
        click(viewModel)

        // Open downloaded slot
        click(viewModel)

        // Delete
        click(viewModel)

        val state = viewModel.menuState.value as MenuState.Download
        assertEquals(0, state.context.slots.size)
    }

    @Test
    fun `delete calls storage deleteFile`() = runTest {
        val viewModel = createNewViewModel()
        persistFakeSlot()

        advanceUntilIdle()

        navigateToEpisodeDetailDownload(viewModel)

        // Download
        click(viewModel)

        // Open downloaded slot
        click(viewModel)

        // Delete
        click(viewModel)

        assertNotNull(fakeFileStorage.deletedFileName)
    }

    @Test
    fun `restoreSlots restores valid persisted slot`() = runTest {
        val viewModel = createNewViewModel()
        persistFakeSlot()

        advanceUntilIdle()

        viewModel.confirmSelection() // Home -> Downloads
        advanceUntilIdle()

        val state = viewModel.menuState.value as MenuState.Download

        assertEquals(1, state.context.slots.size)
    }

    @Test
    fun `play pause toggles playback`() = runTest {
        val viewModel = createNewViewModel()
        persistFakeSlot()

        advanceUntilIdle()

        println("test context.slots: ${viewModel.menuState.value.context.slots}")
        println("test context.slots.size: ${viewModel.menuState.value.context.slots.size}")

        navigateToNowPlaying(viewModel)

        advanceUntilIdle()

        viewModel.onPlayPausePressed()

        assertTrue(fakePodcastPlayer.playCalled)

        viewModel.onPlayPausePressed()

        assertTrue(fakePodcastPlayer.pauseCalled)
    }

    @Test
    fun `isPlaying updates NowPlaying state`() = runTest {
        val viewModel = createNewViewModel()
        persistFakeSlot()

        advanceUntilIdle()

        navigateToNowPlaying(viewModel)  // Start playback

        advanceUntilIdle()

        val state = viewModel.menuState.value as MenuState.NowPlaying
        assertTrue(state.isPlaying)

        viewModel.onPlayPausePressed()

        advanceUntilIdle()

        val updated = viewModel.menuState.value as MenuState.NowPlaying
        assertFalse(updated.isPlaying)
    }

    @Test
    fun `scan forward increases position`() = runTest {
        val viewModel = createNewViewModel()
        persistFakeSlot()

        advanceUntilIdle()

        navigateToNowPlaying(viewModel)

        viewModel.onScanForwardDown()

        advanceTimeBy(200) // simulate 2 ticks

        viewModel.stopScan()

        assertTrue(fakePodcastPlayer.lastSeekPosition > 0)
    }

    @Test
    fun `menu press stops playback`() = runTest {
        val viewModel = createNewViewModel()
        persistFakeSlot()

        advanceUntilIdle()

        navigateToNowPlaying(viewModel)

        viewModel.onMenuShortPress()

        assertTrue(fakePodcastPlayer.stopCalled)
    }

    @Test
    fun `scan job cancels on release`() = runTest {
        val viewModel = createNewViewModel()
        persistFakeSlot()

        advanceUntilIdle()

        navigateToNowPlaying(viewModel)

        viewModel.onScanForwardDown()
        advanceTimeBy(300)

        val positionDuringScan = fakePodcastPlayer.currentPosition

        viewModel.onScanForwardUp()

        advanceTimeBy(500)

        assertEquals(positionDuringScan, fakePodcastPlayer.currentPosition)
    }

    private suspend fun createNewViewModel(
        downloader: Downloader = fakeDownloaderSuccess): MenuViewModel = MenuViewModel(
            podcastRepository = fakePodcastRepository,
            slotRepository = fakePersistedSlotRepository,
            downloader = downloader,
            storage = fakeFileStorage,
            player = fakePodcastPlayer,
            playbackDispatcher = Dispatchers.Default
        )

    private suspend fun persistFakeSlot() {
        fakePersistedSlotRepository.saveSlots(
            listOf(
                PersistedSlot(
                    feedIndex = 0,
                    feedName = "Test Podcast 1",
                    episodeIndex = 1,
                    episodeId = "ep1",
                    fileName = ""
                )
            )
        )
    }

}


