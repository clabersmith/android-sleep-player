package com.github.clabersmith.sleepplayer.core.playback

object Volume {
    fun percentToFloat(percent: Int): Float =
        (percent / 100f).coerceIn(0f, 1f)

    fun floatToPercent(value: Float): Int =
        (value * 100).toInt().coerceIn(0, 100)
}
