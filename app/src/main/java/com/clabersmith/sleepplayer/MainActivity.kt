package com.clabersmith.sleepplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import com.clabersmith.sleepplayer.ui.skin.ipod.IpodScreen
import com.clabersmith.sleepplayer.ui.skin.ipod.viewmodel.IpodUiViewModel

class MainActivity : ComponentActivity() {
    val ipodUiViewModel by viewModels<IpodUiViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                IpodScreen(
                    viewModel = ipodUiViewModel
                )
            }
        }
    }
}
