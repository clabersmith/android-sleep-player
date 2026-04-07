package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model

import com.github.clabersmith.sleepplayer.core.playback.WhiteNoiseTrack
import com.github.clabersmith.sleepplayer.features.podcasts.domain.model.PodcastFeed
import kotlinx.serialization.Serializable

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

@Serializable
data class PlaybackSettings(
    val duckVolumePercent: Int = 10,          // 0–100
    val autoFadeMinutes: Int? = null,         // null = None
    val autoStopMinutes: Int? = null          // null = None
)

@Serializable
data class DisplaySettings(
    val theme: MenuState.DisplaySettings.Theme = MenuState.DisplaySettings.Theme.White
)

@Serializable
data class AudioSettings(
    val clickEnabled: Boolean = true,
    val masterVolume: Int = 100 // 0–100
)