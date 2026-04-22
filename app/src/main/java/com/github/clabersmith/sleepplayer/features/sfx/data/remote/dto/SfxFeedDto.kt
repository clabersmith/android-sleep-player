package com.github.clabersmith.sleepplayer.features.sfx.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SfxFeedDto(
    val items: List<SfxItemDto>
)

@Serializable
data class SfxItemDto(
    val name: String,
    val timestamp: String,
    val url: String
)