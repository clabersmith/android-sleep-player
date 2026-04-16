package com.github.clabersmith.sleepplayer.testutil.data.download

import com.github.clabersmith.sleepplayer.features.podcasts.data.download.PodcastDownloader
import kotlinx.coroutines.delay
import java.io.File

class FakeDownloaderFailing : PodcastDownloader {

    override suspend fun download(
        url: String,
        fileName: String,
        onProgress: (Float) -> Unit,
    ): File {
        throw RuntimeException("Network error")
    }
}

class FakeDownloaderHanging : PodcastDownloader {

    override suspend fun download(
        url: String,
        fileName: String,
        onProgress: (Float) -> Unit,
    ): File {
        delay(Long.MAX_VALUE)
        return File(fileName)
    }
}
class FakeDownloaderProgress : PodcastDownloader {

    lateinit var progressCallback: (Float) -> Unit
    var lastUrl: String? = null
    var lastFileName: String? = null

    override suspend fun download(
        url: String,
        fileName: String,
        onProgress: (Float) -> Unit,
    ): File {

        lastUrl = url
        lastFileName = fileName
        progressCallback = onProgress

        delay(Long.MAX_VALUE)

        return File(fileName)
    }
}

class FakeDownloaderSuccess : PodcastDownloader {

    var lastUrl: String? = null
    var lastFileName: String? = null

    override suspend fun download(
        url: String,
        fileName: String,
        onProgress: (Float) -> Unit,
    ): File {

        lastUrl = url
        lastFileName = fileName

        onProgress(1f)

        delay(10)

        return File(fileName)
    }
}
