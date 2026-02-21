package com.github.clabersmith.sleepplayer.features.podcasts.data.local

import kotlinx.serialization.Serializable

@Serializable
data class PersistedSlot(
    val feedIndex: Int,
    val episodeIndex: Int,
    val episodeId: String,
    val filePath: String
)