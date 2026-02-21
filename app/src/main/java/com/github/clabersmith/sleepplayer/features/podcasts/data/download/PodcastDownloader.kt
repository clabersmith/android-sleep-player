package com.github.clabersmith.sleepplayer.features.podcasts.data.download

import com.github.clabersmith.sleepplayer.features.podcasts.data.local.AudioFileStorage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.prepareGet
import io.ktor.http.contentLength
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.copyTo
import io.ktor.utils.io.readAvailable
import java.io.File

class PodcastDownloader(
    private val client: HttpClient,
    private val storage: AudioFileStorage
) {

    suspend fun download(
        url: String,
        fileName: String,
        onProgress: (Float) -> Unit
    ): File {

        val file = storage.createFile(fileName)
        val testUrl =
            "https://dts.podtrac.com/redirect.mp3/dovetail.prxu.org/_/137/2270a71f-9327-40f1-b8a9-6dff83c79028/SWMP_1414_Datas_Mom_SnoreTrekTNGDatasMom_2.1.26_PP-1.mp3"

        client.prepareGet(testUrl).execute { response ->

            val channel = response.body<ByteReadChannel>()

            val totalBytes = response.contentLength() ?: -1L
            var downloadedBytes = 0L

            file.outputStream().use { output ->

                val buffer = ByteArray(8192)

                while (!channel.isClosedForRead) {

                    val read = channel.readAvailable(buffer)
                    if (read == -1) break

                    output.write(buffer, 0, read)
                    downloadedBytes += read

                    if (totalBytes > 0) {
                        onProgress(downloadedBytes.toFloat() / totalBytes)
                    }
                }
            }
        }

        return file
    }
}