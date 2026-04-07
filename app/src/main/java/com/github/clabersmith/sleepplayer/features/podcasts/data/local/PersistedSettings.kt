package com.github.clabersmith.sleepplayer.features.podcasts.data.local

import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.AudioSettings
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.DisplaySettings
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.PlaybackSettings
import kotlinx.serialization.Serializable

@Serializable
data class PersistedSettings (
    val playbackSettings: PlaybackSettings,
    val displaySettings: DisplaySettings,
    val audioSettings: AudioSettings
)
