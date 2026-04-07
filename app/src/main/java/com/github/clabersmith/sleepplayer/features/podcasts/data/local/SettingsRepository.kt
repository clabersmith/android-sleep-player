package com.github.clabersmith.sleepplayer.features.podcasts.data.local

interface SettingsRepository {
    suspend fun saveSettings(settings: PersistedSettings)
    suspend fun loadSettings(): PersistedSettings?
    suspend fun clear()
}