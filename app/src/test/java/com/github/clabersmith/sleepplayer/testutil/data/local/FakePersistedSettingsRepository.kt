package com.github.clabersmith.sleepplayer.testutil.data.local

import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.AudioSettings
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.DisplaySettings
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.PlaybackSettings
import com.github.clabersmith.sleepplayer.features.podcasts.data.local.PersistedSettings
import com.github.clabersmith.sleepplayer.features.podcasts.data.local.SettingsRepository

class FakePersistedSettingsRepository: SettingsRepository {
    var stored: PersistedSettings = init()

    override suspend fun saveSettings(settings : PersistedSettings) {
        stored = settings
    }

    override suspend fun loadSettings(): PersistedSettings {
        return stored
    }

    override suspend fun clear() {
        stored = init()
    }

    private fun init() = PersistedSettings(
        playbackSettings = PlaybackSettings(),
        displaySettings =  DisplaySettings(),
        audioSettings =  AudioSettings()
    )
}