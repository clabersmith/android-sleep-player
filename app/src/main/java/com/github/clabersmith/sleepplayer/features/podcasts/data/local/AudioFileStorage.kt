package com.github.clabersmith.sleepplayer.features.podcasts.data.local

import android.content.Context
import android.os.Environment
import java.io.File

class AudioFileStorage(
    private val context: Context
) {

    private val audioDirectory: File by lazy {
        context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
            ?: throw IllegalStateException("External files directory unavailable")
    }

    fun createFile(fileName: String): File {
        if (!audioDirectory.exists()) {
            audioDirectory.mkdirs()
        }

        return File(audioDirectory, fileName)
    }

    fun fileExists(filePath: String?): Boolean {
        if (filePath.isNullOrBlank()) return false
        return File(filePath).exists()
    }

    fun deleteFile(filePath: String?) {
        if (filePath.isNullOrBlank()) return
        val file = File(filePath)
        if (file.exists()) {
            file.delete()
        }
    }
}