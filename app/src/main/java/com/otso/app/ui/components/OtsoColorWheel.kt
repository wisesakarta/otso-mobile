package com.otso.app.ui.components

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.otso.app.ui.theme.JetBrainsMono
import com.otso.app.ui.theme.OtsoMotion
import com.otso.app.ui.theme.OtsoTypography
import com.otso.app.ui.theme.SquircleShape
import com.otso.app.ui.theme.otsoClickable
import com.otso.app.ui.theme.otsoColors
import com.otso.app.ui.theme.otsoFloatingSolid
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt

private enum class DragTarget {
    Ring,
    SvSquare,
}

@Composable
fun OtsoColorWheelDialog(
    initialHex: String?,
    onDismiss: () -> Unit,
    onColorSelected: (Int) -> Unit,
) {
    val colors = MaterialTheme.colorScheme.otsoColors
    val initialColor = remember(initialHex) {
        parseHexColor(initialHex) ?: colors.accent
    }
    val initialHsv = remember(initialColor) { colorToHsv(initialColor) }

    var hue by remember(initialColor) { mutableFloatStateOf(initialHsv[0]) }
    var saturation by remember(initialColor) { mutableFloatStateOf(initialHsv[1]) }
    var value by remember(initialColor) { mutableFloatStateOf(initialHsv[2]) }
    var hexInput by remember(initialColor) { mutableStateOf(colorToHexNoHash(initialColor)) }
    // Animatable: values read inside graphicsLayer = draw-phase only, zero recompositions per frame.
    val dialogScale = remember { Animatable(0.95f) }
    val dialogAlpha = remember { Animatable(0f) }

    val selectedColor = remember(hue, saturation, value) {
        hsvToColor(hue, saturation, value)
    }

    LaunchedEffect(Unit) {
        launch { dialogScale.animateTo(1f, tween(durationMillis = 220, easing = OtsoMotion.easeOut)) }
        dialogAlpha.animateTo(1f, tween(durationMillis = 180, easing = OtsoMotion.easeOut))
    }

    LaunchedEffect(selectedColor) {
        if (hexInput.length == 6) {
            hexInput = colorToHexNoHash(selectedColor)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = true,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.42f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onDismiss,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .width(336.dp)
                    .clip(SquircleShape(20.dp))
                    .graphicsLayer {
                        // Draw-phase reads from Animatable.value — zero recompositions during animation.
                        alpha = dialogAlpha.value
                        scaleX = dialogScale.value
                        scaleY = dialogScale.value
                    }
                    .otsoFloatingSolid(shape = SquircleShape(20.dp), colors = colors, elevation = 14.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {},
                    )
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = "Highlight Color",
                    style = OtsoTypography.uiTitle,
                    color = colors.ink,
                )

                OtsoColorWheel(
                    hue = hue,
                    saturation = saturation,
                    value = value,
                    onHueChange = { hue = it },
                    onSaturationValueChange = { s, v ->
                        saturation = s
                        value = v
                    },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(232.dp),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .background(selectedColor, RoundedCornerShape(6.dp))
                            .border(1.dp, colors.edge, RoundedCornerShape(6.dp)),
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .background(colors.background, RoundedCornerShape(10.dp))
                            .border(1.dp, colors.edge, RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                    ) {
                        BasicTextField(
                            value = hexInput,
                            onValueChange = { input ->
                                val sanitized = sanitizeHex(input)
                                hexInput = sanitized
                                if (sanitized.length == 6) {
                                    parseHexColor(sanitized)?.let { parsed ->
                                        val hsv = colorToHsv(parsed)
                                        hue = hsv[0]
                                        saturation = hsv[1]
                                        value = hsv[2]
                                    }
                                }
                            },
                            textStyle = OtsoTypography.uiTechnical.copy(
                                fontFamily = JetBrainsMono,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = colors.ink,
                            ),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Characters,
                                autoCorrectEnabled = false,
                                keyboardType = KeyboardType.Ascii,
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        if (hexInput.isEmpty()) {
                            Text(
                                text = "RRGGBB",
                                style = OtsoTypography.uiTechnical.copy(
                                    fontFamily = JetBrainsMono,
                                    color = colors.muted.copy(alpha = 0.6f),
                                ),
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .background(colors.background, RoundedCornerShape(10.dp))
                            .border(1.dp, colors.edge, RoundedCornerShape(10.dp))
                            .otsoClickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Cancel",
                            style = OtsoTypography.uiLabelMedium,
                            color = colors.muted,
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .background(colors.accent, RoundedCornerShape(10.dp))
                            .otsoClickable {
                                onColorSelected(selectedColor.toArgb())
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Apply",
                            style = OtsoTypography.uiLabelMedium,
                            color = colors.background,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OtsoColorWheel(
    hue: Float,
    saturation: Float,
    value: Float,
    onHueChange: (Float) -> Unit,
    onSaturationValueChange: (Float, Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    var dragTarget by remember { mutableStateOf<DragTarget?>(null) }

    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures { point ->
                    val canvasSize = Size(size.width.toFloat(), size.height.toFloat())
                    when (detectTarget(point, canvasSize)) {
                        DragTarget.Ring -> onHueChange(pointToHue(point, canvasSize))
                        DragTarget.SvSquare -> {
                            val (s, v) = pointToSv(point, canvasSize)
                            onSaturationValueChange(s, v)
                        }
                        null -> Unit
                    }
                }
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { point ->
                        val canvasSize = Size(size.width.toFloat(), size.height.toFloat())
                        dragTarget = detectTarget(point, canvasSize)
                        when (dragTarget) {
                            DragTarget.Ring -> onHueChange(pointToHue(point, canvasSize))
                            DragTarget.SvSquare -> {
                                val (s, v) = pointToSv(point, canvasSize)
                                onSaturationValueChange(s, v)
                            }
                            null -> Unit
                        }
                    },
                    onDragEnd = { dragTarget = null },
                    onDragCancel = { dragTarget = null },
                    onDrag = { change, _ ->
                        val canvasSize = Size(size.width.toFloat(), size.height.toFloat())
                        when (dragTarget) {
                            DragTarget.Ring -> onHueChange(pointToHue(change.position, canvasSize))
                            DragTarget.SvSquare -> {
                                val (s, v) = pointToSv(change.position, canvasSize)
                                onSaturationValueChange(s, v)
                            }
                            null -> Unit
                        }
                        change.consume()
                    },
                )
            },
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2f
        val ringWidth = radius * 0.22f
        val ringRadius = radius - ringWidth / 2f
        val ringInner = ringRadius - ringWidth / 2f
        val svSide = ringInner * sqrt(2f)
        val svTopLeft = Offset(center.x - svSide / 2f, center.y - svSide / 2f)
        val pureHue = hsvToColor(hue, 1f, 1f)

        val hueBrush = Brush.sweepGradient(
            listOf(
                Color(0xFFFF0000),
                Color(0xFFFFFF00),
                Color(0xFF00FF00),
                Color(0xFF00FFFF),
                Color(0xFF0000FF),
                Color(0xFFFF00FF),
                Color(0xFFFF0000),
            ),
            center = center,
        )
        drawCircle(
            brush = hueBrush,
            radius = ringRadius,
            center = center,
            style = Stroke(width = ringWidth, cap = StrokeCap.Butt),
        )
        drawCircle(
            color = Color.Black.copy(alpha = 0.22f),
            radius = ringRadius,
            center = center,
            style = Stroke(width = 1.dp.toPx()),
        )

        clipRect(
            left = svTopLeft.x,
            top = svTopLeft.y,
            right = svTopLeft.x + svSide,
            bottom = svTopLeft.y + svSide,
        ) {
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.White, pureHue),
                    startX = svTopLeft.x,
                    endX = svTopLeft.x + svSide,
                ),
                topLeft = svTopLeft,
                size = Size(svSide, svSide),
            )
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black),
                    startY = svTopLeft.y,
                    endY = svTopLeft.y + svSide,
                ),
                topLeft = svTopLeft,
                size = Size(svSide, svSide),
            )
        }
        drawRect(
            color = Color.Black.copy(alpha = 0.24f),
            topLeft = svTopLeft,
            size = Size(svSide, svSide),
            style = Stroke(width = 1.dp.toPx()),
        )

        val hueRadians = Math.toRadians(hue.toDouble())
        val hueDot = Offset(
            x = center.x + (cos(hueRadians) * ringRadius).toFloat(),
            y = center.y + (sin(hueRadians) * ringRadius).toFloat(),
        )
        // Hue indicator: show pureHue as fill so the dot reflects the selected hue
        drawCircle(
            color = pureHue,
            radius = 5.5.dp.toPx(),
            center = hueDot,
        )
        drawCircle(
            color = Color.White,
            radius = 7.dp.toPx(),
            center = hueDot,
            style = Stroke(width = 1.5.dp.toPx()),
        )

        val currentColor = hsvToColor(hue, saturation, value)
        val svDot = Offset(
            x = svTopLeft.x + saturation.coerceIn(0f, 1f) * svSide,
            y = svTopLeft.y + (1f - value.coerceIn(0f, 1f)) * svSide,
        )
        // SV indicator: show current selected color as fill
        drawCircle(
            color = currentColor,
            radius = 5.dp.toPx(),
            center = svDot,
        )
        drawCircle(
            color = Color.White,
            radius = 6.5.dp.toPx(),
            center = svDot,
            style = Stroke(width = 1.5.dp.toPx()),
        )
    }
}

private fun detectTarget(point: Offset, size: Size): DragTarget? {
    val center = Offset(size.width / 2f, size.height / 2f)
    val radius = size.minDimension / 2f
    val ringWidth = radius * 0.22f
    val ringRadius = radius - ringWidth / 2f
    val ringOuter = ringRadius + ringWidth / 2f
    val ringInner = ringRadius - ringWidth / 2f
    val distance = hypot(point.x - center.x, point.y - center.y)
    if (distance in ringInner..ringOuter) return DragTarget.Ring

    val svSide = ringInner * sqrt(2f)
    val left = center.x - svSide / 2f
    val top = center.y - svSide / 2f
    if (point.x in left..(left + svSide) && point.y in top..(top + svSide)) {
        return DragTarget.SvSquare
    }
    return null
}

private fun pointToHue(point: Offset, size: Size): Float {
    val center = Offset(size.width / 2f, size.height / 2f)
    val radians = atan2(point.y - center.y, point.x - center.x)
    var degrees = Math.toDegrees(radians.toDouble()).toFloat()
    if (degrees < 0f) degrees += 360f
    return degrees
}

private fun pointToSv(point: Offset, size: Size): Pair<Float, Float> {
    val center = Offset(size.width / 2f, size.height / 2f)
    val radius = size.minDimension / 2f
    val ringWidth = radius * 0.22f
    val ringRadius = radius - ringWidth / 2f
    val ringInner = ringRadius - ringWidth / 2f
    val svSide = ringInner * sqrt(2f)
    val left = center.x - svSide / 2f
    val top = center.y - svSide / 2f

    val sat = ((point.x - left) / svSide).coerceIn(0f, 1f)
    val value = (1f - ((point.y - top) / svSide)).coerceIn(0f, 1f)
    return sat to value
}

private fun colorToHsv(color: Color): FloatArray {
    val hsv = FloatArray(3)
    AndroidColor.colorToHSV(color.toArgb(), hsv)
    return hsv
}

private fun hsvToColor(hue: Float, saturation: Float, value: Float): Color {
    return Color(
        AndroidColor.HSVToColor(
            floatArrayOf(
                hue.coerceIn(0f, 360f),
                saturation.coerceIn(0f, 1f),
                value.coerceIn(0f, 1f),
            ),
        ),
    )
}

private fun sanitizeHex(raw: String): String {
    return raw
        .filter { it.isDigit() || it.lowercaseChar() in 'a'..'f' }
        .uppercase()
        .take(6)
}

private fun parseHexColor(hex: String?): Color? {
    val cleaned = hex?.trim()?.removePrefix("#") ?: return null
    if (cleaned.length != 6) return null
    return runCatching { Color(AndroidColor.parseColor("#$cleaned")) }.getOrNull()
}

private fun colorToHexNoHash(color: Color): String {
    val argb = color.toArgb()
    val red = (argb shr 16) and 0xFF
    val green = (argb shr 8) and 0xFF
    val blue = argb and 0xFF
    return "%02X%02X%02X".format(red, green, blue)
}
