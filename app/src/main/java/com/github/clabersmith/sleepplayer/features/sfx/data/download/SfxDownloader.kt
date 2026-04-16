package com.github.clabersmith.sleepplayer.features.sfx.data.download

import java.io.File

interface SfxDownloader {
    suspend fun download(index: Int): File
}