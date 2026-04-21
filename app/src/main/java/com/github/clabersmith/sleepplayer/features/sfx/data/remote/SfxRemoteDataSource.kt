package com.github.clabersmith.sleepplayer.features.sfx.data.remote

import com.github.clabersmith.sleepplayer.features.sfx.data.remote.dto.SfxFeedDto

interface SfxRemoteDataSource {
    suspend fun fetchFeed(): SfxFeedDto
}