package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.viewmodel

import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.ActionRow
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.AudioSettings
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.DisplaySettings
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuState
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.PlaybackSettings
import com.github.clabersmith.sleepplayer.features.podcasts.data.download.PodcastDownloader
import com.github.clabersmith.sleepplayer.features.podcasts.data.local.PersistedSettings
import com.github.clabersmith.sleepplayer.features.podcasts.data.local.PersistedSlot
import com.github.clabersmith.sleepplayer.features.podcasts.data.local.SettingsRepository
import com.github.clabersmith.sleepplayer.features.podcasts.data.local.SlotRepository
import com.github.clabersmith.sleepplayer.features.podcasts.domain.repository.PodcastRepository
import com.github.clabersmith.sleepplayer.features.sfx.domain.repository.SfxRepository
import com.github.clabersmith.sleepplayer.testutil.MainDispatcherRule
import com.github.clabersmith.sleepplayer.testutil.data.local.FakePersistedSlotRepository
import com.github.clabersmith.sleepplayer.testutil.domain.repository.FakePodcastRepository
import com.github.clabersmith.sleepplayer.testutil.data.download.FakeDownloaderFailing
import com.github.clabersmith.sleepplayer.testutil.data.download.FakeDownloaderHanging
import com.github.clabersmith.sleepplayer.testutil.data.download.FakeDownloaderProgress
import com.github.clabersmith.sleepplayer.testutil.data.download.FakeDownloaderSuccess
import com.github.clabersmith.sleepplayer.testutil.data.local.FakeFileStorage
import com.github.clabersmith.sleepplayer.testutil.data.local.FakePersistedSettingsRepository
import com.github.clabersmith.sleepplayer.testutil.domain.repository.FakeSfxRepository
import com.github.clabersmith.sleepplayer.testutil.playback.FakePodcastPlayer
import com.github.clabersmith.sleepplayer.testutil.helpers.ipod.click
import com.github.clabersmith.sleepplayer.testutil.helpers.ipod.navigateToAudioSettings
import com.github.clabersmith.sleepplayer.testutil.helpers.ipod.navigateToDisplaySettings
import com.github.clabersmith.sleepplayer.testutil.helpers.ipod.navigateToEpisodeDetailDownload
import com.github.clabersmith.sleepplayer.testutil.helpers.ipod.navigateToEpisodeDetailDownloaded
import com.github.clabersmith.sleepplayer.testutil.helpers.ipod.navigateToFeedsMenu
import com.github.clabersmith.sleepplayer.testutil.helpers.ipod.navigateToNowPlaying
import com.github.clabersmith.sleepplayer.testutil.helpers.ipod.navigateToSfxMenu
import com.github.clabersmith.sleepplayer.testutil.helpers.ipod.navigateToSfxPlay
import com.github.clabersmith.sleepplayer.testutil.helpers.ipod.navigateToWhiteNoise
import com.github.clabersmith.sleepplayer.testutil.playback.FakePlaybackClock
import com.github.clabersmith.sleepplayer.testutil.playback.FakeSfxPlayer
import com.github.clabersmith.sleepplayer.testutil.playback.FakeWhiteNoisePlayer
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
    private lateinit var fakePersistedSettingsRepository : SettingsRepository
    private lateinit var fakeSfxRepository : SfxRepository
    private lateinit var fakeDownloaderSuccess : PodcastDownloader
    private lateinit var fakeFileStorage : FakeFileStorage
    private lateinit var fakePodcastPlayer : FakePodcastPlayer

    private lateinit var fakeWhiteNoisePlayer: FakeWhiteNoisePlayer
    private lateinit var fakeSfxPlayer: FakeSfxPlayer

    private lateinit var fakePlaybackClock: FakePlaybackClock

    @Before
    fun setup() {
        //setup new text fixture for each test to ensure clean state
        fakePodcastRepository = FakePodcastRepository()
        fakePersistedSlotRepository = FakePersistedSlotRepository()
        fakePersistedSettingsRepository = FakePersistedSettingsRepository()
        fakeSfxRepository = FakeSfxRepository()
        fakeDownloaderSuccess = FakeDownloaderSuccess()
        fakeFileStorage = FakeFileStorage()
        fakePodcastPlayer = FakePodcastPlayer()
        fakeWhiteNoisePlayer = FakeWhiteNoisePlayer()
        fakeSfxPlayer = FakeSfxPlayer()
        fakePlaybackClock = FakePlaybackClock()
    }

    @Test
    fun `loads feeds on init`() = runTest {
        val viewModel = createNewViewModel()
        advanceUntilIdle()  // Wait for load/init to complete

        navigateToFeedsMenu(viewModel)

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
        val viewModel = createNewViewModel()  // Home menu has 4 items, so index 0-3
        advanceUntilIdle()

        viewModel.moveSelection(-1)

        val state = viewModel.menuState.value
        assertEquals(3, state.selectedIndex)
    }

    @Test
    fun `forward wrap goes to zero`() = runTest {
        val viewModel = createNewViewModel() // Home menu has 4 items, so index 0-3
        advanceUntilIdle()

        viewModel.moveSelection(4)

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

        viewModel.confirmSelection() // Home -> Podcasts

        viewModel.moveSelection(1) //Podcasts move to Downloads

        viewModel.confirmSelection() // Podcasts -> Downloads

        val state = viewModel.menuState.value as MenuState.Download

        assertEquals(1, state.context.slots.size)
    }

    //--------------
    // Tests for playback controls in Now Playing menu
    //--------------

    @Test
    fun `play pause toggles playback`() = runTest {
        val viewModel = createNewViewModel()
        persistFakeSlot()

        advanceUntilIdle()

        //plays a podcast
        navigateToNowPlaying(viewModel)

        advanceUntilIdle()

        //pauses the playing podcast
        viewModel.onPlayPauseShortPressed()
        advanceUntilIdle()
        assertTrue(fakePodcastPlayer.pauseCalled)

        //resumes playback
        viewModel.onPlayPauseShortPressed()
        advanceUntilIdle()
        assertTrue(fakePodcastPlayer.playCalled)

    }

    @Test
    fun `isPlaying updates nowPlayingUiState`() = runTest {
        val viewModel = createNewViewModel()
        persistFakeSlot()

        advanceUntilIdle()

        navigateToNowPlaying(viewModel)

        advanceUntilIdle()

        val state = viewModel.nowPlayingUiState.value

        assertTrue(state.isPlaying)

        viewModel.onPlayPauseShortPressed()

        advanceUntilIdle()

        val updated = viewModel.nowPlayingUiState.value
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
    fun `menu short press from podcasts returns to home`() = runTest {
        val viewModel = createNewViewModel()
        advanceUntilIdle()

        viewModel.confirmSelection() // Home -> Podcasts

        viewModel.onMenuShortPress()

        assertTrue(viewModel.menuState.value is MenuState.Home)
    }

    @Test
    fun `repeated menu short press from episode detail returns to home`() = runTest {
        val viewModel = createNewViewModel()
        advanceUntilIdle()

        navigateToEpisodeDetailDownload(viewModel)

        assertTrue(viewModel.menuState.value is MenuState.EpisodeDetail)

        //EpisodeDetail -> Episodes -> Feeds -> Categories -> Downloads -> Podcasts -> Home
        repeat(6) {
            viewModel.onMenuShortPress()
            advanceUntilIdle()
        }

        assertTrue(viewModel.menuState.value is MenuState.Home)
    }

    @Test
    fun `single menu long press from episode detail returns to home`() = runTest {
        val viewModel = createNewViewModel()
        advanceUntilIdle()

        navigateToEpisodeDetailDownload(viewModel)

        assertTrue(viewModel.menuState.value is MenuState.EpisodeDetail)

        //EpisodeDetail -> Home
        viewModel.onMenuLongPress()
        advanceUntilIdle()

        assertTrue(viewModel.menuState.value is MenuState.Home)
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

    //--------------
    // Tests for settings
    //--------------

    @Test
    fun `changing settings persists them`() = runTest {
        val viewModel = createNewViewModel()

        advanceUntilIdle()

        navigateToDisplaySettings(viewModel)

        //move selection to Green Theme and select

        viewModel.moveSelection(4)
        viewModel.confirmSelection()

        advanceUntilIdle()

        assertEquals(MenuState.DisplaySettings.Theme.Green,
            fakePersistedSettingsRepository.loadSettings()?.displaySettings?.theme)
    }

    @Test
    fun `loads persisted settings on init`() = runTest {
        fakePersistedSettingsRepository.saveSettings(
            PersistedSettings(
                playbackSettings = PlaybackSettings(autoStopMinutes = 5),
                displaySettings = DisplaySettings(theme = MenuState.DisplaySettings.Theme.Black),
                audioSettings = AudioSettings(clickEnabled = false,
                    defaultPodcastVolume = 30, defaultWhiteNoiseVolume = 50)
            )
        )

        val viewModel = createNewViewModel()
        advanceUntilIdle()

        assertEquals(5,
            viewModel.menuState.value.context.playbackSettings.autoStopMinutes)
        assertEquals(MenuState.DisplaySettings.Theme.Black,
            viewModel.menuState.value.context.displaySettings.theme)
        assertEquals(30,
            viewModel.menuState.value.context.audioSettings.defaultPodcastVolume)
        assertEquals(50,
            viewModel.menuState.value.context.audioSettings.defaultWhiteNoiseVolume)
    }

    @Test
    fun `changing theme updates display settings`() = runTest {
        val viewModel = createNewViewModel()
        advanceUntilIdle()

        navigateToDisplaySettings(viewModel)

        //move selection to Silver Theme and select
        viewModel.moveSelection(2)
        viewModel.confirmSelection()

        assertEquals(MenuState.DisplaySettings.Theme.Silver,
            viewModel.menuState.value.context.displaySettings.theme)
    }

    @Test
    fun `toggle click sound updates setting`() = runTest {
        // Set initial settings with click enabled
        fakePersistedSettingsRepository.saveSettings(
            PersistedSettings(
                playbackSettings = PlaybackSettings(),
                displaySettings = DisplaySettings(),
                audioSettings = AudioSettings(clickEnabled = true)
            )
        )

        val viewModel = createNewViewModel()

        advanceUntilIdle()

        navigateToAudioSettings(viewModel)

        // Toggle click sound off
        viewModel.confirmSelection()

        assertFalse(viewModel.menuState.value.context.audioSettings.clickEnabled)

    }


    //--------------
    // Tests for white noise playback
    //--------------

    @Test
    fun `selecting white noise starts playback`() = runTest {
        val viewModel = createNewViewModel()
        advanceUntilIdle()

        navigateToWhiteNoise(viewModel)

        click(viewModel) // select a track

        assertTrue(fakeWhiteNoisePlayer.playCalled)
    }

    @Test
    fun `starting podcast ducks white noise`() = runTest {
        val viewModel = createNewViewModel()
        persistFakeSlot()

        advanceUntilIdle()

        // Start white noise first
        navigateToWhiteNoise(viewModel)
        click(viewModel)

        //hold menu down to go back to home
        viewModel.onMenuLongPress()
        advanceUntilIdle()

        //Then play podcast
        navigateToNowPlaying(viewModel)
        advanceUntilIdle()

        //when podcast starts, white noise should duck (volume reduced)
        assertTrue(
            fakeWhiteNoisePlayer.setVolumeCalled &&
                    fakeWhiteNoisePlayer.volumeSet > -1.0f &&
                    fakeWhiteNoisePlayer.volumeSet < 1.0f
        )
    }

    @Test
    fun `pausing podcast restores white noise volume`() = runTest {
        val viewModel = createNewViewModel()
        persistFakeSlot()

        advanceUntilIdle()

        //start white noise
        navigateToWhiteNoise(viewModel)
        click(viewModel)

        //hold menu down to go back to home
        viewModel.onMenuLongPress()
        advanceUntilIdle()

        //start a podcast
        navigateToNowPlaying(viewModel)
        advanceUntilIdle()

        viewModel.onPlayPauseShortPressed() // pause
        advanceUntilIdle()

        assertTrue(fakeWhiteNoisePlayer.setVolumeCalled  &&
                fakeWhiteNoisePlayer.volumeSet == 1.0f) // White noise should be unducked (volume restored) when podcast is paused
    }

    @Test
    fun `auto stop stops playback after configured time`() = runTest {
        val viewModel = createNewViewModel()

        persistFakeSlot()

        fakePersistedSettingsRepository.saveSettings(
            PersistedSettings(
                playbackSettings = PlaybackSettings(autoStopMinutes = 1),
                displaySettings = DisplaySettings(),
                audioSettings = AudioSettings()
            )
        )


        advanceUntilIdle()

        navigateToNowPlaying(viewModel)
        advanceUntilIdle()

        fakePlaybackClock.advance(60_000 + 1_000) // instant, deterministic,
            // matches setting in fakePersistedSettingsRepository, includes slight buffer to ensure we pass the threshold
        advanceUntilIdle()

        assertTrue(fakePodcastPlayer.stopCalled)
    }

    @Test
    fun `auto fade triggers after configured time`() = runTest {
        val viewModel = createNewViewModel()

        // Arrange: persisted slot so playback works
        persistFakeSlot()

        // Set auto fade to 1 minute
        fakePersistedSettingsRepository.saveSettings(
            PersistedSettings(
                playbackSettings = PlaybackSettings(autoFadeMinutes = 1),
                displaySettings = DisplaySettings(),
                audioSettings = AudioSettings()
            )
        )

        advanceUntilIdle()

        // Start playback (this sets startedAtMs)
        navigateToNowPlaying(viewModel)
        advanceUntilIdle()

        // advance clock to trigger auto-fade
        fakePlaybackClock.advance(60_000)
        advanceUntilIdle()

        // test podcast fade started (volume changed)
        assertTrue( fakePodcastPlayer.volumeHistory.size > 0 &&
                fakePodcastPlayer.volumeSet < 100)
    }

    @Test
    fun `auto fade does not trigger if playback stops before timer`() = runTest {
        val viewModel = createNewViewModel()

        persistFakeSlot()

        // Set auto fade to 1 minute
        fakePersistedSettingsRepository.saveSettings(
            PersistedSettings(
                playbackSettings = PlaybackSettings(autoFadeMinutes = 1),
                displaySettings = DisplaySettings(),
                audioSettings = AudioSettings()
            )
        )

        advanceUntilIdle()

        // Start playback
        navigateToNowPlaying(viewModel)
        advanceUntilIdle()

        // Stop playback early (before 1 minute)
        viewModel.stopPlaybackCompletely()
        advanceUntilIdle()

        // Advance time past the original trigger
        fakePlaybackClock.advance(60_000)
        advanceUntilIdle()

        // Assert: no fade occurred (volume should not have been lowered)
        assertTrue( fakePodcastPlayer.volumeHistory.none { it < 1.0f })
    }

    @Test
    fun `playing sfx starts sfx player`() = runTest {
        val viewModel = createNewViewModel()

        advanceUntilIdle()

        // Start white noise first (required for SFX menu)
        navigateToWhiteNoise(viewModel)
        click(viewModel)

        assertTrue(fakeWhiteNoisePlayer.playCalled)

        viewModel.onMenuLongPress()
        advanceUntilIdle()

        //Start SFX playback
        navigateToSfxPlay(viewModel)
        click(viewModel)

        assertTrue(fakeSfxPlayer.playCalled)
    }

    @Test
    fun `playing second sfx replaces previous`() = runTest {
        val viewModel = createNewViewModel()

        advanceUntilIdle()

        navigateToWhiteNoise(viewModel)
        click(viewModel)
        advanceUntilIdle()


        viewModel.onMenuLongPress()
        advanceUntilIdle()

        navigateToSfxPlay(viewModel)

        click(viewModel) // first
        advanceUntilIdle()

        viewModel.moveSelection(1)

        click(viewModel) // second
        advanceUntilIdle()

        assertEquals(1, fakeSfxPlayer.getCurrentIndex())
    }

    @Test
    fun `selecting active sfx stops playback`() = runTest {
        val viewModel = createNewViewModel()

        advanceUntilIdle()

        navigateToWhiteNoise(viewModel)
        click(viewModel)

        viewModel.onMenuLongPress()
        advanceUntilIdle()

        navigateToSfxPlay(viewModel)

        click(viewModel)
        advanceUntilIdle()

        click(viewModel) // same index again

        assertTrue(fakeSfxPlayer.stopCalled)
    }

    @Test
    fun `playing sfx stops podcast playback`() = runTest {
        val viewModel = createNewViewModel()

        persistFakeSlot()
        advanceUntilIdle()

        //start playback of podcast
        navigateToNowPlaying(viewModel)
        advanceUntilIdle()

        //go to home
        viewModel.onMenuLongPress()
        advanceUntilIdle()

        //start playback of white noise
        navigateToWhiteNoise(viewModel)
        click(viewModel)
        advanceUntilIdle()

        //go to home
        viewModel.onMenuLongPress()
        advanceUntilIdle()

        //go to sfx and play
        navigateToSfxPlay(viewModel)
        click(viewModel)
        advanceUntilIdle()

        assertTrue(fakePodcastPlayer.stopCalled)
    }

    @Test
    fun `cannot enter sfx play without white noise active`() = runTest {
        val viewModel = createNewViewModel()

        advanceUntilIdle()

        navigateToSfxMenu(viewModel)

        viewModel.moveSelection(1)

        click(viewModel)

        assertTrue(viewModel.menuState.value is MenuState.Sfx)
    }

    @Test
    fun `changing sfx volume updates settings`() = runTest {
        val viewModel = createNewViewModel()

        advanceUntilIdle()

        navigateToAudioSettings(viewModel)

        viewModel.moveSelection(3)

        repeat(60) {
            viewModel.onScanForwardDown()
            viewModel.onScanForwardUp()
        }

        advanceUntilIdle()

        assertEquals(
            100,
            viewModel.menuState.value.context
                .audioSettings.defaultSfxVolume
        )
    }

    @Test
    fun `changing white noise volume updates active playback`() = runTest {
        fakePersistedSettingsRepository.saveSettings(
            PersistedSettings(
                playbackSettings = PlaybackSettings(),
                displaySettings = DisplaySettings(),
                audioSettings = AudioSettings(
                    defaultWhiteNoiseVolume = 50
                )
            )
        )

        val viewModel = createNewViewModel()
        advanceUntilIdle()

        navigateToWhiteNoise(viewModel)
        click(viewModel)
        advanceUntilIdle()

        //go to home
        viewModel.onMenuLongPress()
        advanceUntilIdle()

        navigateToAudioSettings(viewModel)

        viewModel.moveSelection(2)
        viewModel.onScanForwardDown()
        viewModel.onScanForwardUp()
        advanceUntilIdle()

        assertTrue(fakeWhiteNoisePlayer.setVolumeCalled)
        assertEquals(0.51f, fakeWhiteNoisePlayer.volumeSet)
    }

        @Test
        fun `changing sfx volume updates active sfx playback`() = runTest {
            fakePersistedSettingsRepository.saveSettings(
                PersistedSettings(
                    playbackSettings = PlaybackSettings(),
                    displaySettings = DisplaySettings(),
                    audioSettings = AudioSettings(
                        defaultSfxVolume = 50
                    )
                )
            )

            val viewModel = createNewViewModel()
            advanceUntilIdle()

            //go to white noise from home and start it (required to enter sfx menu)
            navigateToWhiteNoise(viewModel)
            click(viewModel)
            advanceUntilIdle()

            //back to home
            viewModel.onMenuLongPress()
            advanceUntilIdle()

            //got to sfx play and start it
            navigateToSfxPlay(viewModel)
            click(viewModel)
            advanceUntilIdle()

            //back to home
            viewModel.onMenuLongPress()
            advanceUntilIdle()

            // got sfx audio settings and change sfx volume
            navigateToAudioSettings(viewModel)
            viewModel.moveSelection(3)
            viewModel.onScanForwardDown()
            viewModel.onScanForwardUp()
            advanceUntilIdle()

            assertTrue(fakeSfxPlayer.setVolumeCalled)

            assertTrue(fakeSfxPlayer.volumeSet > 0.5f)
        }
    private suspend fun createNewViewModel(
        downloader: PodcastDownloader = fakeDownloaderSuccess
    ): MenuViewModel = MenuViewModel(
        podcastRepository = fakePodcastRepository,
        slotRepository = fakePersistedSlotRepository,
        settingsRepository = fakePersistedSettingsRepository,
        sfxRepository = fakeSfxRepository,
        downloader = downloader,
        storage = fakeFileStorage,
        podcastPlayer = fakePodcastPlayer,
        whiteNoisePlayer = fakeWhiteNoisePlayer,
        sfxPlayer = fakeSfxPlayer,
        playbackClock = fakePlaybackClock
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


