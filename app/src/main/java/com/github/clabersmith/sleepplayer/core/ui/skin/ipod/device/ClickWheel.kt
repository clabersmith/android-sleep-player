package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.device

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.theme.IpodClickWheelColor
import com.github.clabersmith.sleepplayer.core.ui.skin.ipod.theme.IpodMenuText
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.abs
import kotlin.math.atan2

@Composable
fun ClickWheel(
    bodyColor: Color,
    textColor: Color,
    onRotate: (Int) -> Unit,
    onConfirm: () -> Unit,
    onMenuShortPress: () -> Unit,
    onMenuLongPress: () -> Unit,
    onPlayPausePress: () -> Unit,
    onScanForwardDown: () -> Unit,
    onScanForwardUp: () -> Unit,
    onScanBackDown: () -> Unit,
    onScanBackUp: () -> Unit,
    modifier: Modifier = Modifier
) {

    BoxWithConstraints(modifier = modifier) {

        val baseSize = 240.dp

        val scale = (minOf(maxWidth, maxHeight) / baseSize)
            .coerceAtMost(1.3f)


        val wheelSize = baseSize * scale

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
                .background(IpodClickWheelColor)
                .border(
                    width = 1.dp,
                    color = Color.Black.copy(alpha = 0.15f),
                    shape = CircleShape
                )
                .pointerInput(Unit) {
                    detectCircularGestures(
                        onRotateStep = { step ->
                            onRotate(step)
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {

            // ----- MENU (Back) -----
            Text(
                text = "MENU",
                color = textColor,
                style = IpodMenuText.copy(
                    fontSize = IpodMenuText.fontSize * scale
                ),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 18.dp * scale)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                val longPressTriggered = withTimeoutOrNull(2000) {
                                    awaitRelease()
                                } == null

                                if (longPressTriggered) {
                                    onMenuLongPress()
                                } else {
                                    onMenuShortPress()
                                }
                            }
                        )
                    }
            )

            // ----- Back -----
            Text(
                text = "⏮",
                color = textColor,
                style = IpodMenuText.copy(
                    fontSize = IpodMenuText.fontSize * scale * 1.2f
                ),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 20.dp * scale)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                onScanBackDown()

                                tryAwaitRelease()

                                onScanBackUp()
                            }
                        )
                    }
            )

            // ----- FORWARD -----
            Text(
                text = "⏭",
                color = textColor,
                style = IpodMenuText.copy(
                    fontSize = IpodMenuText.fontSize * scale * 1.2f
                ),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 20.dp * scale)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                onScanForwardDown()

                                tryAwaitRelease()

                                onScanForwardUp()
                            }
                        )
                    }
            )

            // ----- PLAY/PAUSE (visual only for now) -----
            Canvas(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 18.dp * scale)
                    .size(28.dp * .8f * scale)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                onPlayPausePress()
                            }
                        )
                    }
            ) {
                val w = size.width
                val h = size.height
                val color = textColor

                drawPath(
                    path = Path().apply {
                        moveTo(w * 0.18f, h * 0.22f)
                        lineTo(w * 0.56f, h * 0.50f)
                        lineTo(w * 0.18f, h * 0.78f)
                        close()
                    },
                    color = color
                )

                val barWidth = w * 0.11f
                val gap = barWidth * 1.25f
                val firstX = w * 0.64f
                val secondX = firstX + barWidth + gap

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
                animationSpec = tween(90),
                label = "centerScale"
            )

            val shadow by animateDpAsState(
                targetValue = if (isPressed) 1.4.dp else 3.dp,
                animationSpec = tween(90),
                label = "centerShadow"
            )

            val overlayAlpha by animateFloatAsState(
                targetValue = if (isPressed) 0.06f else 0f,
                animationSpec = tween(90),
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
                                    onConfirm()
                                } finally {
                                    isPressed = false
                                }
                            }
                        )
                    }
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = overlayAlpha))
                )
            }
        }
    }
}

suspend fun PointerInputScope.detectCircularGestures(
    onRotateStep: (Int) -> Unit
) {
    var previousAngle: Float? = null
    var accumulator = 0f

    val NOTCH_THRESHOLD = 0.22f
    val SENSITIVITY = 0.18f

    detectDragGestures(
        onDragEnd = {
            previousAngle = null
            accumulator = 0f
        },
        onDragCancel = {
            previousAngle = null
            accumulator = 0f
        }
    ) { change, _ ->

        val center = size.center
        val touch = change.position

        val angle = atan2(
            touch.y - center.y,
            touch.x - center.x
        )

        previousAngle?.let { prev ->

            var delta = angle - prev

            if (delta > Math.PI) delta -= (2 * Math.PI).toFloat()
            if (delta < -Math.PI) delta += (2 * Math.PI).toFloat()

            if (abs(delta) > 0.035f) {

                val dampedDelta = delta.coerceIn(-0.4f, 0.4f)
                accumulator += dampedDelta * SENSITIVITY

                while (abs(accumulator) >= NOTCH_THRESHOLD) {

                    val direction =
                        if (accumulator > 0) 1 else -1

                    onRotateStep(direction)

                    accumulator -=
                        NOTCH_THRESHOLD * direction
                }
            }
        }

        previousAngle = angle
        change.consume()
    }
}