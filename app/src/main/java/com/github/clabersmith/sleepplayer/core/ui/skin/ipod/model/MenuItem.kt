package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model

data class MenuItem(
    val title: String,
    val value: String? = null,
    val showChevron: Boolean = false,
    val isChecked: Boolean = false,
)