package com.github.clabersmith.sleepplayer.features.podcasts.data.local

import java.io.File

interface FileStorage {
    fun createFile(fileName: String): File
    fun fileExists(fileName: String?): Boolean
    fun deleteFile(fileName: String?): Boolean
    fun getFilePath(fileName: String): String
}