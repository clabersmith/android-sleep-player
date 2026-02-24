package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.github.clabersmith.sleepplayer.R

val IpodFontFamily = FontFamily(
    Font(R.font.chicago, FontWeight.Normal)
)

val IpodMenuText = TextStyle(
    fontFamily = IpodFontFamily,
    fontSize = 16.sp,
    fontWeight = FontWeight.SemiBold,
    color = IpodTextPrimary
)