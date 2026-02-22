package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model

sealed class ActionRow {

    data object Download : ActionRow()

    data class Downloading(
        val progress: Float = 0f
    ) : ActionRow()

    data object Cancel : ActionRow()

    data object Delete : ActionRow()

    data object AlreadyDownloaded : ActionRow()
}