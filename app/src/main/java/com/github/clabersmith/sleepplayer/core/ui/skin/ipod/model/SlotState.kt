package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model

import com.github.clabersmith.sleepplayer.features.podcasts.domain.model.PodcastEpisode
import com.github.clabersmith.sleepplayer.features.podcasts.data.local.PersistedSlot
data class SlotState(
    val feedIndex: Int,
    val episodeIndex: Int,
    val loadedEpisode: PodcastEpisode,
    val filePath: String?
)

fun SlotState.toPersisted() =
    PersistedSlot(
        feedIndex = feedIndex,
        episodeIndex = episodeIndex,
        episodeId = loadedEpisode.id,
        filePath = filePath ?: ""
    )