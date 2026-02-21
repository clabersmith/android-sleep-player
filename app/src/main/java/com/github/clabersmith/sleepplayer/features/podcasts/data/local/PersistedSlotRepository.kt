package com.github.clabersmith.sleepplayer.features.podcasts.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class PersistedSlotRepository(
    private val dataStore: DataStore<Preferences>
) : SlotRepository {

    companion object {
        val SLOTS_KEY = stringPreferencesKey("slots_json")
    }

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override suspend fun saveSlots(slots: List<PersistedSlot>) {
        val slotsJson = json.encodeToString(
            ListSerializer(PersistedSlot.serializer()),
            slots
        )

        dataStore.edit { prefs ->
            prefs[SLOTS_KEY] = slotsJson
        }
    }

    override suspend fun loadSlots(): List<PersistedSlot> {
        val prefs = dataStore.data.first()
        val slotsJson = prefs[SLOTS_KEY] ?: return emptyList()

        return runCatching {
            json.decodeFromString(
                ListSerializer(PersistedSlot.serializer()),
                slotsJson
            )
        }.getOrDefault(emptyList())
    }

    override suspend fun clear() {
        dataStore.edit { it.remove(SLOTS_KEY) }
    }
}