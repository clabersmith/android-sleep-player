package com.github.clabersmith.sleepplayer.testutil.data.local

import com.github.clabersmith.sleepplayer.features.podcasts.data.local.PersistedSlot
import com.github.clabersmith.sleepplayer.features.podcasts.data.local.SlotRepository

class FakePersistedSlotRepository : SlotRepository {

    private var stored: List<PersistedSlot> = emptyList()

    override suspend fun saveSlots(slots: List<PersistedSlot>) {
        stored = slots
    }

    override suspend fun loadSlots(): List<PersistedSlot> {
        return stored
    }

    override suspend fun clear() {
        stored = emptyList()
    }
}