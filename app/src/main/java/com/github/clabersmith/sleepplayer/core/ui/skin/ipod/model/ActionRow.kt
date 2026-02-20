package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model

data class ActionRow(
    val label: String,
    val type: Type,
    val enabled: Boolean
) {
    enum class Type {
        DOWNLOAD,
        DELETE,
        BACK
    }
}