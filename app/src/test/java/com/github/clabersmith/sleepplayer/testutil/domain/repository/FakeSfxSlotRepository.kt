package com.github.clabersmith.sleepplayer.testutil.domain.repository

import com.github.clabersmith.sleepplayer.features.sfx.data.local.PersistedSfxSlot
import com.github.clabersmith.sleepplayer.features.sfx.data.local.SfxSlotRepository

class FakeSfxSlotRepository(
    private val slots: MutableList<PersistedSfxSlot>
) : SfxSlotRepository {

    override suspend fun loadSlots(): List<PersistedSfxSlot> = slots.toList()

    override suspend fun updateSlot(index: Int, timestamp: Long) {
        val i = slots.indexOfFirst { it.index == index }
        if (i != -1) {
            slots[i] = slots[i].copy(lastDownloadedAt = timestamp)
        }
    }

    override suspend fun saveSlots(slots: List<PersistedSfxSlot>) {
        this.slots.clear()
        this.slots.addAll(slots)
    }
}