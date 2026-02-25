package com.github.clabersmith.sleepplayer.testutil.data.local

import com.github.clabersmith.sleepplayer.features.podcasts.data.local.FileStorage
import java.io.File

class FakeFileStorage : FileStorage {

    var deletedFileName: String? = null

    override fun createFile(fileName: String): File {
        return File("dummy")
    }

    override fun fileExists(fileName: String?): Boolean = true

    override fun deleteFile(fileName: String?): Boolean {
        deletedFileName = fileName
        return true
    }

    override fun getFilePath(fileName: String): String {
        return "dummy/${fileName}"
    }
}