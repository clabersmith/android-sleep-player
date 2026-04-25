package com.github.clabersmith.sleepplayer.features.sfx.domain.repository

import com.github.clabersmith.sleepplayer.features.sfx.data.local.PersistedSfxSlot
import kotlinx.coroutines.flow.StateFlow

interface SfxRepository {
    val status: StateFlow<SfxDownloadStatus>
    suspend fun startDownload()
    suspend fun getFileNameForIndex(index: Int): String?
    suspend fun getSlots(): List<PersistedSfxSlot>
}