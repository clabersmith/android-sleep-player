package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.device

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.clabersmith.sleepplayer.R
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.MenuItem
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.theme.IpodMenuHighlight
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.theme.IpodMenuText
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.theme.IpodTextPrimary

@Composable
fun MenuRow(
    item: MenuItem,
    selected: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (selected) IpodMenuHighlight else Color.Transparent
            )
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = item.title,
            maxLines = 1,
            overflow = TextOverflow.Clip,
            style = IpodMenuText,
            fontSize = 18.sp,
            color = if (selected) Color.White else IpodTextPrimary.copy(alpha = 0.85f),
            modifier = Modifier
                .weight(1f)
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

        // Checkmark
        if (item.isChecked) {
            Text(
                text = "✓",
                style = IpodMenuText,
                color = if (selected) Color.White else IpodTextPrimary,
                modifier = Modifier.padding(end = 8.dp)
            )
        }

        if (item.showChevron) {
            Icon(
                painter = painterResource(R.drawable.ic_ipod_chevron),
                contentDescription = null,
                tint = if (selected) Color.White else IpodTextPrimary,
                modifier = Modifier
                    .padding(start = 6.dp, end = 4.dp)
            )
        }
    }
}