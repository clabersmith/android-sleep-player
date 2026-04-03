package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model

import com.github.clabersmith.sleepplayer.core.playback.WhiteNoiseTrack
import com.github.clabersmith.sleepplayer.features.podcasts.domain.model.PodcastFeed

data class MenuContext(
    val slots: List<SlotState>,
    val feeds: List<PodcastFeed>,
    val categories: List<String>,
    val maxSlotsCount: Int,
    val playbackSettings: PlaybackSettings,
    val currentWhiteNoiseTrack: WhiteNoiseTrack? = null,
    val displaySettings: DisplaySettings = DisplaySettings(),
    val audioSettings: AudioSettings = AudioSettings()
)

data class PlaybackSettings(
    val duckVolumePercent: Int = 10,          // 0–100
    val autoFadeMinutes: Int? = 12,         // null = None
    val autoStopMinutes: Int? = 20          // null = None
)

data class DisplaySettings(
    val theme: MenuState.DisplaySettings.Theme = MenuState.DisplaySettings.Theme.White
)

data class AudioSettings(
    val clickEnabled: Boolean = true,
    val masterVolume: Int = 100 // 0–100
)