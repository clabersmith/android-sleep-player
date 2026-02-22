package com.github.clabersmith.sleepplayer.features.podcasts.data.download

import java.io.File

interface Downloader {
    suspend fun download(
        url: String,
        fileName: String,
        onProgress: (Float) -> Unit
    ): File
}