package com.github.clabersmith.sleepplayer.features.podcasts.data.download

import com.github.clabersmith.sleepplayer.core.data.download.FileDownloader
import com.github.clabersmith.sleepplayer.features.podcasts.data.local.AudioFileStorage
import java.io.File


class DefaultPodcastDownloader(
    private val fileDownloader: FileDownloader,
    private val storage: AudioFileStorage
) : PodcastDownloader {

    override suspend fun download(
        url: String,
        fileName: String,
        onProgress: (Float) -> Unit
    ): File {

        val file = storage.createFile(fileName)

        return fileDownloader.download(
            url = url,
            outputFile = file,
            onProgress = onProgress
        )
    }
}