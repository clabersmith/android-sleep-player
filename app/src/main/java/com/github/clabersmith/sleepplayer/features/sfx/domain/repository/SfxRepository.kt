package com.github.clabersmith.sleepplayer.features.sfx.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface SfxRepository {
    val status: StateFlow<SfxDownloadStatus>
    suspend fun startDownload()
}