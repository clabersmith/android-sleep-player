package com.github.clabersmith.sleepplayer.testutil.helpers.ipod

import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.viewmodel.MenuViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle

//--------------
// Helper extension functions to set up specific menu states
//--------------
@OptIn(ExperimentalCoroutinesApi::class)
fun TestScope.navigateToFeedsMenu(
    viewModel: MenuViewModel
) {
    // Home -> Podcasts
    click(viewModel)

    //Podcasts, select Downloads
    viewModel.moveSelection(1)
    advanceUntilIdle()

    // Podcasts -> Downloads
    click(viewModel)

    //Downloads -> Categories (via 'Add New')
    click(viewModel)

    //Categories -> Feeds (via 'Relaxation' category)
    click(viewModel)
}

fun TestScope.navigateToEpisodeDetailDownload(
    viewModel: MenuViewModel
) {
    navigateToFeedsMenu(viewModel)

    //Feeds -> Episodes
    click(viewModel)

    //Episodes -> Episode Detail
    click(viewModel)
}

fun TestScope.navigateToEpisodeDetailDownloaded(
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

fun TestScope.navigateToNowPlaying(
    viewModel: MenuViewModel
) {
    //Home -> Podcasts
    click(viewModel)

    //Podcasts -> Play
    click(viewModel)

    //Play -> NowPlaying
    click(viewModel)
}

fun TestScope.navigateToWhiteNoise(
    viewModel: MenuViewModel
) {
    //Home, select White Noise
    viewModel.moveSelection(1)

    //Home -> White Noise
    click(viewModel)
}

fun TestScope.navigateToSettings(
    viewModel: MenuViewModel
) {
    //Home, select Settings
    viewModel.moveSelection(3)

    //Home -> Settings
    click(viewModel)
}

fun TestScope.navigateToDisplaySettings(
    viewModel: MenuViewModel
) {

    navigateToSettings(viewModel)

    //Settings, select Display
    viewModel.moveSelection(1)

    //Settings -> Playback
    click(viewModel)
}

fun TestScope.navigateToAudioSettings(
    viewModel: MenuViewModel
) {

    navigateToSettings(viewModel)

    //Settings, select Audio
    viewModel.moveSelection(2)

    //Settings -> Audio
    click(viewModel)
}



@OptIn(ExperimentalCoroutinesApi::class)
fun TestScope.click(viewModel: MenuViewModel) {
    viewModel.confirmSelection()
    advanceUntilIdle()
}