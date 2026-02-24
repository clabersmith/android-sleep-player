package com.github.clabersmith.sleepplayer.features.podcasts.data.local

import kotlinx.serialization.Serializable

@Serializable
data class PersistedSlot(
    val feedIndex: Int,
    val feedName: String,
    val episodeIndex: Int,
    val episodeId: String,
    val fileName: String
)