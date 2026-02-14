package com.clabersmith.sleepplayer.ui.skin.ipod.theme

import androidx.compose.ui.graphics.Color

val IpodLcdBackground = Color(0xFFBFE8F6)
val IpodLcdBackgroundDark = Color(0xFFA7D6E8)
val IpodTextPrimary = Color(0xFF28305E)

val IpodMenuHighlight = Color(0xFF2B4EA2)
val IpodMenuDownloadProgress = Color(0xFF5B7ED6) // bright blue (matches highlight)


fun Color.blend(target: Color, amount: Float): Color {
    return Color(
        red = red + (target.red - red) * amount,
        green = green + (target.green - green) * amount,
        blue = blue + (target.blue - blue) * amount,
        alpha = alpha
    )
}