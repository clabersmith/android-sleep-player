package com.github.clabersmith.sleepplayer.testutil.domain.repository

import com.github.clabersmith.sleepplayer.features.sfx.data.local.PersistedSfxSlot
import com.github.clabersmith.sleepplayer.features.sfx.data.local.SfxSlotRepository

class FakeSfxSlotRepository(
    private val slots: MutableList<PersistedSfxSlot> =
        (0 until 4).map { index ->
            PersistedSfxSlot(
                index = index,
                fileName = "",
                lastDownloadedAt = 0L
            )
        }.toMutableList()
) : SfxSlotRepository {

    override suspend fun loadSlots(): List<PersistedSfxSlot> =
        slots.toList()

    override suspend fun saveSlots(slots: List<PersistedSfxSlot>) {
        this.slots.clear()
        this.slots.addAll(slots)
    }

    override suspend fun updateSlot(
        index: Int,
        fileName: String,
        timestamp: Long
    ) {
        val i = slots.indexOfFirst { it.index == index }

        if (i != -1) {
            slots[i] = slots[i].copy(
                fileName = fileName,
                lastDownloadedAt = timestamp
            )
        } else {
            // Optional: add if missing (helps avoid silent failures in tests)
            slots.add(
                PersistedSfxSlot(
                    index = index,
                    fileName = fileName,
                    lastDownloadedAt = timestamp
                )
            )
        }
    }

    // -----------------------------------
    // Test helpers (optional but useful)
    // -----------------------------------

    fun setFileName(index: Int, fileName: String) {
        val i = slots.indexOfFirst { it.index == index }
        if (i != -1) {
            slots[i] = slots[i].copy(
                fileName = fileName,
                lastDownloadedAt = System.currentTimeMillis()
            )
        }
    }

    fun clear() {
        slots.replaceAll {
            it.copy(fileName = "", lastDownloadedAt = 0L)
        }
    }
}