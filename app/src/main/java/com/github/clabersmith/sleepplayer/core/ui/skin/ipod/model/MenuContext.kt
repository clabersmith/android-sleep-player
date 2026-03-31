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
)

data class PlaybackSettings(
    val duckVolumePercent: Int = 10,          // 0–100
    val autoFadeMinutes: Int? = 12,         // null = None
    val autoStopMinutes: Int? = 20          // null = None
)