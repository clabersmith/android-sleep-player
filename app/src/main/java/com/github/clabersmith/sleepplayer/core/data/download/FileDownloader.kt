package com.github.clabersmith.sleepplayer.core.data.download

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.prepareGet
import io.ktor.http.contentLength
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.ensureActive
import java.io.File
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.coroutineContext


class FileDownloader(
    private val client: HttpClient
) {

    companion object {
        val BUFFER_SIZE = 128 * 1024
    }

    suspend fun download(
        url: String,
        outputFile: File,
        onProgress: (Float) -> Unit
    ): File {

        val file = outputFile

        try {
            client.prepareGet(url).execute { response ->

                val channel = response.body<ByteReadChannel>()

                val totalBytes = response.contentLength() ?: -1L
                var downloadedBytes = 0L

                file.outputStream().buffered(BUFFER_SIZE).use { output ->

                    val buffer = ByteArray(BUFFER_SIZE)
                    var lastProgress = 0f

                    while (!channel.isClosedForRead) {

                        coroutineContext.ensureActive()

                        val read = channel.readAvailable(buffer)
                        if (read == -1) break

                        output.write(buffer, 0, read)
                        downloadedBytes += read

                        if (totalBytes > 0) {
                            val progress = downloadedBytes.toFloat() / totalBytes

                            if (progress - lastProgress >= 0.01f || progress == 1f) {
                                lastProgress = progress
                                onProgress(progress)
                            }
                        }
                    }
                }
            }

            return file

        } catch (e: CancellationException) {
            file.delete()
            throw e
        } catch (e: Exception) {
            file.delete()
            throw e
        }
    }
}