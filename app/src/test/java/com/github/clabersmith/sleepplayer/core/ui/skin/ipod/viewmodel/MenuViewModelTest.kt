package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.viewmodel

import com.github.clabersmith.sleepplayer.core.playback.AudioPlayer
import com.github.clabersmith.sleepplayer.core.playback.AudioSource
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.ActionRow
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuState
import com.github.clabersmith.sleepplayer.features.podcasts.data.download.Downloader
import com.github.clabersmith.sleepplayer.features.podcasts.data.local.FileStorage
import com.github.clabersmith.sleepplayer.features.podcasts.data.local.PersistedSlot
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestScope
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
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
    private val successFakeDownloader: Downloader = object : Downloader {
        override suspend fun download(
            url: String,
            fileName: String,
            onProgress: (Float) -> Unit,
        ): File {
            delay(10) // ensures intermediate state is visible
            return File("dummy")
        }

    }

    private val hangingDownloader: Downloader = object : Downloader {
        override suspend fun download(
            url: String,
            fileName: String,
            onProgress: (Float) -> Unit,
        ): File {
            delay(Long.MAX_VALUE)
            return File("never")
        }
    }

    private val failingDownloader = object : Downloader {
        override suspend fun download(
            url: String,
            fileName: String,
            onProgress: (Float) -> Unit,
        ): File {
            throw RuntimeException("Network error")
        }
    }

    private class ProgressFakeDownloader : Downloader {

        lateinit var progressCallback: (Float) -> Unit

        override suspend fun download(
            url: String,
            fileName: String,
            onProgress: (Float) -> Unit,
        ): File {
            progressCallback = onProgress
            delay(Long.MAX_VALUE)
            return File("dummy")
        }
    }

    private class FakeFileStorage : FileStorage {

        var deletedFileName: String? = null

        override fun createFile(fileName: String): File {
            return File("dummy")
        }

        override fun fileExists(fileName: String?): Boolean = true

        override fun deleteFile(fileName: String?): Boolean {
            deletedFileName = fileName
            return true
        }

        override fun getFilePath(fileName: String): String {
            return "dummy/${fileName}"
        }
    }

    class FakePodcastPlayer : AudioPlayer {

        var playing = false
        var position = 0L
        var duration = 60_000L

        override suspend fun load(audioSource: AudioSource) { }

        override fun play() { playing = true }
        override fun pause() { playing = false }
        override fun seekTo(positionMs: Long) { position = positionMs }

        override fun currentPosition() = position
        override fun duration() = duration
        override fun isPlaying() = playing
        override fun release() {}
    }

    private val fakeFileStorage : FakeFileStorage = FakeFileStorage()

    @Test
    fun `loads feeds on init`() = runTest {

        val viewModel = createNewViewModel()

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
        val viewModel = createNewViewModel()

        viewModel.moveSelection(1)

        val state = viewModel.menuState.value
        assertEquals(1, state.selectedIndex)
    }

    @Test
    fun `rotate backward wraps to last index`() {
        val viewModel = createNewViewModel()

        viewModel.moveSelection(-1)

        val state = viewModel.menuState.value
        assertEquals(3, state.selectedIndex)
    }

    @Test
    fun `forward wrap goes to zero`() {
        val viewModel = createNewViewModel()

        viewModel.moveSelection(3)
        viewModel.moveSelection(1)

        assertEquals(0, viewModel.menuState.value.selectedIndex)
    }

    @Test
    fun `moveSelection ignored in episode detail`() = runTest {

        val viewModel = createNewViewModel()

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

        navigateToEpisodeDetailDownload(viewModel)

        viewModel.confirmSelection()

        val state = viewModel.menuState.value as MenuState.EpisodeDetail

        assertTrue(state.isDownloading)
        assertTrue(state.actionRows.first() is ActionRow.Downloading)
    }

    @Test
    fun `download action adds slot`() = runTest {

        val viewModel = createNewViewModel()

        navigateToEpisodeDetailDownload(viewModel)

        // EpisodeDetail
        click(viewModel) // DOWNLOAD

        advanceUntilIdle() // wait for download to complete

        val state = viewModel.menuState.value as MenuState.Download
        assertEquals(1, state.slots.size)
    }

    @Test
    fun `already downloaded episode shows correct action`() = runTest {

        val viewModel = createNewViewModel()

        navigateToEpisodeDetailDownload(viewModel)

        // Download first, should take back to Downloads menu
        click(viewModel)
        assertTrue(viewModel.menuState.value is MenuState.Download)

        viewModel.moveSelection(1) // Move to 'Add New'

        navigateToEpisodeDetailDownloaded(viewModel)

        assertTrue(viewModel.menuState.value is MenuState.EpisodeDetail)

        val state = viewModel.menuState.value as MenuState.EpisodeDetail
        assertTrue(state.actionRows.firstOrNull { it is ActionRow.AlreadyDownloaded } != null)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `cancel download returns to non-downloading state`() = runTest {
        val viewModel = createNewViewModel(hangingDownloader)

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
        val viewModel = createNewViewModel(hangingDownloader)

        navigateToEpisodeDetailDownload(viewModel)

        viewModel.confirmSelection() // start
        advanceUntilIdle()

        viewModel.confirmSelection() // cancel
        advanceUntilIdle()

        assertTrue(fakePersistedSlotRepository.loadSlots().isEmpty())
    }

    @Test
    fun `progress updates downloading row`() = runTest {
        val progressFakeDownloader = ProgressFakeDownloader()
        val viewModel = createNewViewModel(progressFakeDownloader)

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
       val viewModel = createNewViewModel(failingDownloader)

        navigateToEpisodeDetailDownload(viewModel)

        viewModel.confirmSelection()
        advanceUntilIdle()

        assertTrue(fakePersistedSlotRepository.loadSlots().isEmpty())
    }

    @Test
    fun `delete action removes slot`() = runTest {

        val viewModel = createNewViewModel()

        navigateToEpisodeDetailDownload(viewModel)

        // Download first
        click(viewModel)

        // Open downloaded slot
        click(viewModel)

        // Delete
        click(viewModel)

        val state = viewModel.menuState.value as MenuState.Download
        assertEquals(0, state.slots.size)
    }

    @Test
    fun `delete calls storage deleteFile`() = runTest {

        val viewModel = createNewViewModel()

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

        val viewModel = createNewViewModel()

        advanceUntilIdle()

        viewModel.confirmSelection() // Home -> Downloads
        advanceUntilIdle()

        val state = viewModel.menuState.value as MenuState.Download

        assertEquals(1, state.slots.size)
    }

    //--------------
    // Helper functions to set up specific menu states
    //--------------
    private fun createNewViewModel(
        downloader: Downloader = successFakeDownloader): MenuViewModel = MenuViewModel(
            podcastRepository = fakeRepository,
            slotRepository = fakePersistedSlotRepository,
            downloader = downloader,
            storage = fakeFileStorage,
            player = FakePodcastPlayer()
        )

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
        click(viewModel)
    }

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

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun TestScope.click(viewModel: MenuViewModel) {
        viewModel.confirmSelection()
        advanceUntilIdle()
        println("click menuState result: ${viewModel.menuState.value}")
    }

}


