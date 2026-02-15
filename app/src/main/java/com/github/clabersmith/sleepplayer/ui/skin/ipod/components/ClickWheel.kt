package com.github.clabersmith.sleepplayer.ui.skin.ipod.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.dp
import com.github.clabersmith.sleepplayer.ui.skin.ipod.input.WheelEvent
import com.github.clabersmith.sleepplayer.ui.skin.ipod.theme.IpodClickWheelColor
import com.github.clabersmith.sleepplayer.ui.skin.ipod.theme.IpodMenuText
import kotlin.math.abs
import kotlin.math.atan2

@Composable
fun ClickWheel(
    bodyColor: Color,
    textColor: Color,
    onEvent: (WheelEvent) -> Unit,
    modifier: Modifier = Modifier
) {

    BoxWithConstraints(modifier = modifier) {

        // ----- Baseline design -----
        val baseSize = 240.dp

        // Scale to available space like IpodLcdScreen
        val scale = (minOf(maxWidth, maxHeight) / baseSize)
            .coerceAtMost(1.2f)

        val wheelSize = baseSize * scale
        val rimOuter = 240.dp * scale
        val rimInner = 236.dp * scale

        val innerRing1 = 83.dp * scale
        val innerRing2 = 82.dp * scale

        val centerSize = 80.dp * scale

        Box(
            modifier = Modifier
                .size(wheelSize)
                .graphicsLayer {
                    shadowElevation = 6.dp.toPx()
                    shape = CircleShape
                    clip = true
                }
                .clip(CircleShape)
                .background(IpodClickWheelColor) // wheel surface
                .border(
                    width = 1.dp,
                    color = Color.Black.copy(alpha = 0.15f),
                    shape = CircleShape
                )
                .pointerInput(Unit) {
                    detectCircularGestures(
                        onRotate = { delta ->
                            onEvent(WheelEvent.Rotate(delta))
                        },
                        onGestureEnd = {
                            onEvent(WheelEvent.GestureEnd)
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {

            // ----- Subtle inner rim shadow -----
            Box(
                modifier = Modifier
                    .size(rimOuter)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = Color.Black.copy(alpha = 0.06f),
                        shape = CircleShape
                    )
            )

            // ----- Subtle inner rim highlight -----
            Box(
                modifier = Modifier
                    .size(rimInner)
                    .clip(CircleShape)
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.20f),
                        shape = CircleShape
                    )
            )

            // ----- Inner rings -----
            Box(
                modifier = Modifier
                    .size(innerRing1)
                    .clip(CircleShape)
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.35f),
                        shape = CircleShape
                    )
            )

            Box(
                modifier = Modifier
                    .size(innerRing2)
                    .clip(CircleShape)
                    .border(
                        width = 1.dp,
                        color = Color.Black.copy(alpha = 0.15f),
                        shape = CircleShape
                    )
            )

            // ----- MENU label -----
            Text(
                text = "MENU",
                color = textColor,
                style = IpodMenuText.copy(
                    fontSize = IpodMenuText.fontSize * scale
                ),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 18.dp * scale)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )  { onEvent(WheelEvent.MenuClick) }
            )

            // ----- PREVIOUS (LEFT) -----
            Text(
                text = "⏮",
                color = textColor,
                style = IpodMenuText.copy(
                    fontSize = IpodMenuText.fontSize * scale * 1.2f
                ),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 20.dp * scale)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )  {
                        onEvent(WheelEvent.PrevClick)
                    }
            )

            // ----- NEXT (RIGHT) -----
            Text(
                text = "⏭",
                color = textColor,
                style = IpodMenuText.copy(
                    fontSize = IpodMenuText.fontSize * scale * 1.2f
                ),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 20.dp * scale)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )  {
                        onEvent(WheelEvent.NextClick)
                    }
            )

            // ----- PLAY / PAUSE (BOTTOM) -----
            Canvas(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 18.dp * scale)
                    .size(28.dp * .8f *  scale)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        onEvent(WheelEvent.PlayPauseClick)
                    }
            ) {
                val w = size.width
                val h = size.height
                val color = textColor

                // triangle (same as before)
                drawPath(
                    path = Path().apply {
                        moveTo(w * 0.18f, h * 0.22f)
                        lineTo(w * 0.56f, h * 0.50f)
                        lineTo(w * 0.18f, h * 0.78f)
                        close()
                    },
                    color = color
                )

                // pause bars with ~1:1.25 bar:gap ratio
                val barWidth = w * 0.11f
                val gap = barWidth * 1.25f

                val firstX = w * 0.64f
                val secondX = firstX + barWidth + gap   // → enforces 1:1.3 spacing

                drawRect(
                    color = color,
                    topLeft = Offset(firstX, h * 0.22f),
                    size = Size(barWidth, h * 0.56f)
                )

                drawRect(
                    color = color,
                    topLeft = Offset(secondX, h * 0.22f),
                    size = Size(barWidth, h * 0.56f)
                )
            }


            // ----- CENTER BUTTON -----
            var isPressed by remember { mutableStateOf(false) }

            val buttonScale by animateFloatAsState(
                targetValue = if (isPressed) 0.975f else 1f,
                animationSpec = tween(durationMillis = 90),
                label = "centerScale"
            )

            val shadow by animateDpAsState(
                targetValue = if (isPressed) 1.4.dp else 3.dp,
                animationSpec = tween(durationMillis = 90),
                label = "centerShadow"
            )

            val overlayAlpha by animateFloatAsState(
                targetValue = if (isPressed) 0.06f else 0f,
                animationSpec = tween(durationMillis = 90),
                label = "centerOverlay"
            )

            Box(
                modifier = Modifier
                    .size(centerSize)
                    .graphicsLayer {
                        scaleX = buttonScale
                        scaleY = buttonScale
                        shadowElevation = shadow.toPx()
                        shape = CircleShape
                        clip = true
                    }
                    .background(bodyColor)
                    .border(
                        width = 1.dp,
                        color = Color.Black.copy(alpha = 0.15f),
                        shape = CircleShape
                    )
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isPressed = true
                                try {
                                    awaitRelease()
                                    onEvent(WheelEvent.CenterClick)
                                } finally {
                                    isPressed = false
                                }
                            }
                        )
                    }
            ) {
                // Subtle dark overlay when pressed
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Color.Black.copy(alpha = overlayAlpha)
                        )
                )
            }
        }
    }
}
suspend fun PointerInputScope.detectCircularGestures(
    onRotate: (Float) -> Unit,
    onGestureEnd: () -> Unit
) {
    var previousAngle: Float? = null
    var accumulator = 0f

    val NOTCH_THRESHOLD = 0.22f   // ~12.6° per step
    val SENSITIVITY = 0.18f       // heavier feel

    detectDragGestures(
        onDragEnd = {
            previousAngle = null
            accumulator = 0f
            onGestureEnd()
        },
        onDragCancel = {
            previousAngle = null
            accumulator = 0f
            onGestureEnd()
        }
    ) { change, _ ->

        val center = size.center
        val touch = change.position

        val angle = atan2(
            center.y - touch.y,
            touch.x - center.x
        )

        previousAngle?.let { prev ->

            var delta = angle - prev

            if (delta > Math.PI) delta -= (2 * Math.PI).toFloat()
            if (delta < -Math.PI) delta += (2 * Math.PI).toFloat()

            if (abs(delta) > 0.035f) {

                val dampedDelta = delta.coerceIn(-0.4f, 0.4f)
                accumulator += dampedDelta * SENSITIVITY

                // Only fire when enough movement collected
                while (abs(accumulator) >= NOTCH_THRESHOLD) {

                    val step =
                        if (accumulator > 0) NOTCH_THRESHOLD
                        else -NOTCH_THRESHOLD

                    onRotate(step)

                    accumulator -= step
                }
            }
        }

        previousAngle = angle
        change.consume()
    }
}