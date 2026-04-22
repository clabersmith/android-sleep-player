package com.github.clabersmith.sleepplayer.core.util

import java.text.SimpleDateFormat
import java.util.*

fun formatTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}
