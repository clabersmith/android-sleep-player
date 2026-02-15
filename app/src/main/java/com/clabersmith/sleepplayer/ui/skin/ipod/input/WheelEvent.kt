package com.clabersmith.sleepplayer.ui.skin.ipod.input

sealed class WheelEvent {
    data class Rotate(val delta: Float) : WheelEvent()
    object GestureEnd : WheelEvent()
    object CenterClick : WheelEvent()
    object MenuClick : WheelEvent()
    object PlayPauseClick : WheelEvent()
    object PrevClick : WheelEvent()
    object NextClick : WheelEvent()
}