package com.github.clabersmith.sleepplayer.testutil.data.remote

import com.github.clabersmith.sleepplayer.features.sfx.data.remote.SfxRemoteDataSource
import com.github.clabersmith.sleepplayer.features.sfx.data.remote.dto.SfxFeedDto

class FakeSfxRemoteDataSource(
    private val feed: SfxFeedDto
) : SfxRemoteDataSource {

    override suspend fun fetchFeed(): SfxFeedDto = feed
}