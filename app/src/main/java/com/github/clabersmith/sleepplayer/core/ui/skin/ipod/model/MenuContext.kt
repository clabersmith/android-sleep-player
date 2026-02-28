package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model

import com.github.clabersmith.sleepplayer.features.podcasts.domain.model.PodcastFeed

data class MenuContext(
    val slots: List<SlotState>,
    val feeds: List<PodcastFeed>,
    val categories: List<String>,
    val maxSlotsCount: Int
)