package com.github.clabersmith.sleepplayer.features.podcasts.data.local

interface SlotRepository {
    suspend fun saveSlots(slots: List<PersistedSlot>)
    suspend fun loadSlots(): List<PersistedSlot>
    suspend fun clear()
}