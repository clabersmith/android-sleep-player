package com.github.clabersmith.sleepplayer.features.sfx.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SfxFeedDto(
    val items: List<SfxFeedItemDto>
)

@Serializable
data class SfxFeedItemDto(
    val index: Int,
    val url: String,
    val lastModified: Long
)