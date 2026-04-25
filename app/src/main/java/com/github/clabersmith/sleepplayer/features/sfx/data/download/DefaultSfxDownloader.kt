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
        fileName: String,
        url: String
    ): File {

        val sanitizedFileName = sanitizeFileName(fileName)
        val file = storage.createFile(sanitizedFileName)

        return fileDownloader.download(
            url = url,
            outputFile = file,
            onProgress = {}
        )
    }
}

private fun sanitizeFileName(name: String): String {
    return name
        .replace("[^A-Za-z0-9._-]".toRegex(), "_")
        .take(100)
}