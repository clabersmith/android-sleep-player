package com.github.clabersmith.sleepplayer.features.sfx.data.local

interface SfxSlotRepository {
    suspend fun loadSlots(): List<PersistedSfxSlot>
    suspend fun saveSlots(slots: List<PersistedSfxSlot>)
    suspend fun updateSlot(index: Int, fileName: String, timestamp: Long)
}