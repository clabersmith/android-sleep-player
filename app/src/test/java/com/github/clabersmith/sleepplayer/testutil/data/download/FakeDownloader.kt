package com.github.clabersmith.sleepplayer.testutil.data.download

import com.github.clabersmith.sleepplayer.features.podcasts.data.download.Downloader
import kotlinx.coroutines.delay
import java.io.File

class FakeDownloaderFailing : Downloader {
    override suspend fun download(
        url: String,
        fileName: String,
        onProgress: (Float) -> Unit,
    ): File {
        throw RuntimeException("Network error")
    }
}

class FakeDownloaderHanging : Downloader {
    override suspend fun download(
        url: String,
        fileName: String,
        onProgress: (Float) -> Unit,
    ): File {
        delay(Long.MAX_VALUE)
        return File("never")
    }
}

class FakeDownloaderProgress : Downloader {
    lateinit var progressCallback: (Float) -> Unit

    override suspend fun download(
        url: String,
        fileName: String,
        onProgress: (Float) -> Unit,
    ): File {
        progressCallback = onProgress
        delay(Long.MAX_VALUE)
        return File("dummy")
    }
}

class FakeDownloaderSuccess : Downloader {
    override suspend fun download(
        url: String,
        fileName: String,
        onProgress: (Float) -> Unit,
    ): File {
        delay(10) // ensures intermediate state is visible
        return File("dummy")
    }
}
