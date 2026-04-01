package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object IpodThemes {

    val White = IpodTheme(
        bodyColor = IpodBodyColorWhite,
        clickWheelColor = IpodClickWheelColorWhite,
        clickWheelTextColor = IpodClickWheelTextColorWhite,
        shellGradient = Brush.verticalGradient(
            listOf(
                Color(0xFFFFFFFF),
                Color(0xFFFFFFFF)
            )
        )
    )

    val Black = IpodTheme(
        bodyColor = IpodBodyColorBlack,
        clickWheelColor = IpodClickWheelColorBlack,
        clickWheelTextColor = IpodClickWheelTextColorBlack,
        shellGradient = Brush.verticalGradient(
            listOf(
                Color(0xFF2A2A2A),
                Color(0xFF1E1E1E)
            )
        )
    )

    val Silver = IpodTheme(
        bodyColor = IpodBodyColorSilver,
        clickWheelColor = IpodClickWheelColorSilver,
        clickWheelTextColor = IpodClickWheelTextColorSilver,
        shellGradient = Brush.verticalGradient(
            listOf(
                Color(0xFFF8F8F8),
                Color(0xFFDADADA),
                Color(0xFFBFBFBF)
            )
        )
    )

    val Blue = IpodTheme(
        bodyColor = IpodBodyColorBlue,
        clickWheelColor = IpodClickWheelColorBlue,
        clickWheelTextColor = IpodClickWheelTextColorBlue,
        shellGradient = Brush.verticalGradient(
            listOf(
                Color(0xFFB3E5FC),
                Color(0xFF81D4FA)
            )
        )
    )

    val Green = IpodTheme(
        bodyColor = IpodBodyColorGreen,
        clickWheelColor = IpodClickWheelColorGreen,
        clickWheelTextColor = IpodClickWheelTextColorGreen,
        shellGradient = Brush.verticalGradient(
            listOf(
                Color(0xFFA5D6A7),
                Color(0xFF81C784)
            )
        )
    )

    val Pink = IpodTheme(
        bodyColor = IpodBodyColorPink,
        clickWheelColor = IpodClickWheelColorPink,
        clickWheelTextColor = IpodClickWheelTextColorPink,
        shellGradient = Brush.verticalGradient(
            listOf(
                Color(0xFFF48FB1),
                Color(0xFFF48FB1)
            )
        )
    )
}
