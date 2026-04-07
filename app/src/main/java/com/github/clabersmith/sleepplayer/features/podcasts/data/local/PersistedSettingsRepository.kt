package com.github.clabersmith.sleepplayer.features.podcasts.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.github.clabersmith.sleepplayer.features.podcasts.data.local.PersistedSlotRepository.Companion.SLOTS_KEY
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PersistedSettingsRepository(    private val dataStore: DataStore<Preferences>) : SettingsRepository {
    companion object { private val SETTINGS_KEY = stringPreferencesKey("settings_json") }

    override suspend fun saveSettings(settings: PersistedSettings) {
        val json = Json.encodeToString(settings)
        dataStore.edit { prefs ->
            prefs[SETTINGS_KEY] = json
        }
    }

    override suspend fun loadSettings(): PersistedSettings? {
        val json = dataStore.data.first()[SETTINGS_KEY] ?: return null

        return runCatching {
            return Json.decodeFromString(json)
        }.getOrNull()
    }

    override suspend fun clear() {
        dataStore.edit { it.remove(SLOTS_KEY) }
    }
}