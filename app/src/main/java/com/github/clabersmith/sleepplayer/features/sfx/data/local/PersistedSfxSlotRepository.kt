package com.github.clabersmith.sleepplayer.features.sfx.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

class PersistedSfxSlotRepository(
    private val dataStore: DataStore<Preferences>
) {

    companion object {
        val SFX_SLOTS_KEY = stringPreferencesKey("sfx_slots_json")
    }

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    suspend fun saveSlots(slots: List<PersistedSfxSlot>) {
        val slotsJson = json.encodeToString(
            ListSerializer(PersistedSfxSlot.serializer()),
            slots
        )

        dataStore.edit { prefs ->
            prefs[SFX_SLOTS_KEY] = slotsJson
        }
    }

    suspend fun loadSlots(): List<PersistedSfxSlot> {
        val prefs = dataStore.data.first()
        val slotsJson = prefs[SFX_SLOTS_KEY]

        return if (slotsJson == null) {
            defaultSlots()
        } else {
            runCatching {
                json.decodeFromString(
                    ListSerializer(PersistedSfxSlot.serializer()),
                    slotsJson
                )
            }.getOrElse {
                defaultSlots()
            }
        }
    }

    suspend fun clear() {
        dataStore.edit { it.remove(SFX_SLOTS_KEY) }
    }

    private fun defaultSlots(): List<PersistedSfxSlot> {
        return (1..4).map { index ->
            PersistedSfxSlot(
                index = index,
                lastDownloadedAt = null
            )
        }
    }
}