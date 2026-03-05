package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model

import com.github.clabersmith.sleepplayer.features.podcasts.data.local.PersistedSlot
import com.github.clabersmith.sleepplayer.features.podcasts.domain.model.PodcastEpisode

data class SlotState(
    val feedIndex: Int,
    val feedName: String,
    val episodeIndex: Int,
    val loadedEpisode: PodcastEpisode,
    val fileName: String
)

fun SlotState.toPersisted() =
    PersistedSlot(
        feedIndex = feedIndex,
        feedName = feedName,
        episodeIndex = episodeIndex,
        episodeId = loadedEpisode.id,
        fileName = fileName
    )