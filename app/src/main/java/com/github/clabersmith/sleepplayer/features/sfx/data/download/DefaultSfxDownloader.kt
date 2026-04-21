package com.github.clabersmith.sleepplayer.features.sfx.data.download

import com.github.clabersmith.sleepplayer.core.data.download.FileDownloader
import com.github.clabersmith.sleepplayer.features.podcasts.data.local.AudioFileStorage
import java.io.File

class DefaultSfxDownloader(
    private val fileDownloader: FileDownloader,
    private val storage: AudioFileStorage
) : SfxDownloader {

    override suspend fun download(
        index: Int,
        url: String
    ): File {

        val fileName = "sfx_feed_$index.mp3"
        val file = storage.createFile(fileName)

        return fileDownloader.download(
            url = url,
            outputFile = file,
            onProgress = {}
        )
    }
}