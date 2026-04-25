package com.github.clabersmith.sleepplayer.features.sfx.domain.repository

data class SfxDownloadStatus(
    val isDownloading: Boolean = false,
    val message: String = "",
    val current: Int = 0,
    val total: Int = 4,
    val isUpToDate: Boolean = false
)