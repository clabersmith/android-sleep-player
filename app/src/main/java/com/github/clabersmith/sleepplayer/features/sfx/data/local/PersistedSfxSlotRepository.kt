package com.github.clabersmith.sleepplayer.features.sfx.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.first
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class PersistedSfxSlotRepository(
    private val dataStore: DataStore<Preferences>
) : SfxSlotRepository {

    companion object {
        val KEY = stringPreferencesKey("sfx_slots_json")
    }

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override suspend fun loadSlots(): List<PersistedSfxSlot> {
        val prefs = dataStore.data.first()
        val raw = prefs[KEY] ?: return defaultSlots()

        return runCatching {
            json.decodeFromString(
                ListSerializer(PersistedSfxSlot.serializer()),
                raw
            )
        }.getOrDefault(defaultSlots())
    }

    override suspend fun saveSlots(slots: List<PersistedSfxSlot>) {
        val encoded = json.encodeToString(
            ListSerializer(PersistedSfxSlot.serializer()),
            slots
        )

        dataStore.edit { it[KEY] = encoded }
    }

    override suspend fun updateSlot(index: Int, fileName: String, timestamp: Long) {
        val slots = loadSlots().toMutableList()
        val i = slots.indexOfFirst { it.index == index }

        if (i != -1) {
            slots[i] = slots[i].copy(
                fileName = fileName,
                lastDownloadedAt = timestamp
            )
        }
        saveSlots(slots)
    }

    private fun defaultSlots(): List<PersistedSfxSlot> =
        (0..3).map { index ->
            PersistedSfxSlot(
                index = index,
                fileName = "",        // empty until downloaded
                lastDownloadedAt = 0L
            )
        }
}