package com.github.clabersmith.sleepplayer.features.sfx.data.local

import kotlinx.serialization.Serializable

@Serializable
data class PersistedSfxSlot(
    val index: Int,
    val lastDownloadedAt: Long? = null
)