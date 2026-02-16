package com.github.clabersmith.sleepplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.IpodScreen
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.viewmodel.IpodUiViewModel

class MainActivity : ComponentActivity() {
    private val ipodUiViewModel: IpodUiViewModel by viewModels {
        IpodUiViewModelFactory(
            repository = container.podcastRepository
        )
    }

    private val container = AppContainer()

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
