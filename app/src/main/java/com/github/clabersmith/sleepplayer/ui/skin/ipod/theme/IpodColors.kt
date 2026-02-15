package com.github.clabersmith.sleepplayer.ui.skin.ipod.theme

import androidx.compose.ui.graphics.Color

val IpodBodyColorLight = Color(0xFFF2F2F2)
val IpodBodyColorDark = Color(0xFF1C1C1C)
val IpodClickWheelColor = Color(0xFFD6D6D6)
val IpodClickWheelTextColorLight = Color(0xFFF2F2F2)
val IpodClickWheelTextColorDark = Color(0xFF1E1E1E)
val IpodTextPrimary = Color(0xFF28305E)

val IpodMenuHighlight = Color(0xFF2B4EA2)

fun Color.blend(target: Color, amount: Float): Color {
    return Color(
        red = red + (target.red - red) * amount,
        green = green + (target.green - green) * amount,
        blue = blue + (target.blue - blue) * amount,
        alpha = alpha
    )
}
