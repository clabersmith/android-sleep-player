package com.github.clabersmith.sleepplayer.ui.skin.ipod.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.ViewModel
import com.github.clabersmith.sleepplayer.ui.skin.ipod.input.WheelEvent

class  IpodUiViewModel : ViewModel() {

    private val _selectedIndex = mutableIntStateOf(0)
    val selectedIndex: State<Int> = _selectedIndex

    private val menuSize = 4 // derive from list later
    fun onWheelEvent(event: WheelEvent) {
        when (event) {

            is WheelEvent.Rotate -> {
                val current = _selectedIndex.value

                val next =
                    if (event.delta > 0) current + 1
                    else current - 1

                _selectedIndex.value =
                    (next + menuSize) % menuSize
            }

            else -> Unit
        }
    }
}