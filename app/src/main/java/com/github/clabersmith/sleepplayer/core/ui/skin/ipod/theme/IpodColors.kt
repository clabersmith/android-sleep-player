package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.theme

import android.icu.lang.UCharacter
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Ipod device colors body
val IpodBodyColorWhite = Color(0xFFF2F2F2)
val IpodBodyColorBlack = Color(0xFF000000)
val IpodBodyColorSilver = Color(0xFFD6D6D6)
val IpodBodyColorBlue = Color(0xFF81D4FA)
val IpodBodyColorGreen = Color(0xFF81C784)
val IpodBodyColorPink = Color(0xFFF48FB1)

//Ipod device colors click wheel
val IpodClickWheelColorWhite = Color(0xFFC9C8C8)
val IpodClickWheelColorBlack = Color(0x7C666565)
val IpodClickWheelColorSilver = Color.White
val IpodClickWheelColorBlue = Color.White
val IpodClickWheelColorGreen = Color.White
val IpodClickWheelColorPink = Color.White

//Ipod click wheel text colors
val IpodClickWheelTextColorWhite = Color(0xFFF2F2F2)
val IpodClickWheelTextColorBlack = Color.White
val IpodClickWheelTextColorSilver = Color(0xFFD0CFCF)
val IpodClickWheelTextColorBlue = Color(0xFF81D4FA)
val IpodClickWheelTextColorGreen = Color(0xFF81C784)
val IpodClickWheelTextColorPink = Color(0xFFF48FB1)

// Ipod menu screen colors
val IpodTextPrimary = Color(0xFF353F7E)

//val IpodMenuHighlight = Color(0xFF2B4EA2)
val IpodMenuHighlight = Color(0xFF353F7E)
val IpodMenuDownloadProgress = Color(0xFF5B7ED6) // lighter than highlight

// --- Shell Gradients ---
val IpodShellGradientWhite = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFFFFFF),
        Color(0xFFF2F2F2)
    )
)

val IpodShellGradientBlack = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF2A2A2A),
        Color(0xFF1E1E1E)
    )
)

val IpodShellGradientSilver = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFF8F8F8),
        Color(0xFFDADADA),
        Color(0xFFBFBFBF)
    )
)
