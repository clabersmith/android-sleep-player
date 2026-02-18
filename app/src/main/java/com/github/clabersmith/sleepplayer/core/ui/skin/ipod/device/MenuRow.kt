package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.device

import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.basicMarquee
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.theme.IpodMenuHighlight
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.theme.IpodMenuText
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.theme.IpodTextPrimary

@Composable
fun MenuRow(
    text: String,
    selected: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (selected) IpodMenuHighlight else Color.Transparent
            )
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = text,
            maxLines = 1,
            overflow = TextOverflow.Clip, //important for marquee to work properly
            style = IpodMenuText,
            color = if (selected) Color.White else IpodTextPrimary.copy(alpha = 0.85f),
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (selected)
                        Modifier.basicMarquee(
                            iterations = Int.MAX_VALUE,
                            initialDelayMillis = 850,
                            repeatDelayMillis = 1300,
                            velocity = 28.dp
                        )
                    else Modifier
                )
        )
    }
}