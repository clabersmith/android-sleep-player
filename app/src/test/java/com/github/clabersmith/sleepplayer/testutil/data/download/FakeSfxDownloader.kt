package com.github.clabersmith.sleepplayer.testutil.data.download

import com.github.clabersmith.sleepplayer.features.sfx.data.download.SfxDownloader
import java.io.File

class FakeSfxDownloader : SfxDownloader {

    val downloaded = mutableListOf<Int>()

    override suspend fun download(index: Int, url: String): File {
        downloaded.add(index)
        return File("sfx_feed_$index.mp3")
    }
}