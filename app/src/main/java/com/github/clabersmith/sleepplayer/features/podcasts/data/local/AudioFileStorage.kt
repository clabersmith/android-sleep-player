package com.github.clabersmith.sleepplayer.features.podcasts.data.local

import android.content.Context
import java.io.File

class AudioFileStorage(
    context: Context
) : FileStorage {

    private val audioDirectory =
        context.getExternalFilesDir(null)
            ?: error("External files dir unavailable")

    override fun createFile(fileName: String): File {
        return File(audioDirectory, fileName)
    }

    override fun fileExists(fileName: String?): Boolean {
        if (fileName == null) return false
        val file = File(audioDirectory, fileName)
        return file.exists()
    }

    override fun deleteFile(fileName: String?): Boolean {
        if (fileName == null) return false
        val file = File(audioDirectory, fileName)
        return if (file.exists()) {
            file.delete()
        } else {
            false
        }
    }

    override fun getFilePath(fileName: String): String {
        return File(audioDirectory, fileName).absolutePath
    }
}