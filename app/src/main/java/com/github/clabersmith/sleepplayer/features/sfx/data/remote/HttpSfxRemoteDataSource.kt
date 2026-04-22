package com.github.clabersmith.sleepplayer.features.sfx.data.remote

import com.github.clabersmith.sleepplayer.features.sfx.data.remote.dto.SfxFeedDto
import com.github.clabersmith.sleepplayer.features.sfx.data.remote.dto.SfxItemDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class HttpSfxRemoteDataSource(
    private val client: HttpClient,
    private val url: String
) : SfxRemoteDataSource {

    override suspend fun fetchFeed(): SfxFeedDto {
        val items: List<SfxItemDto> = client.get(url).body()

        return SfxFeedDto(items = items)
    }
}