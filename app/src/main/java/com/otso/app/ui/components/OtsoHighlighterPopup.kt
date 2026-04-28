package com.otso.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.otso.app.ui.theme.OtsoMotion
import com.otso.app.ui.theme.SquircleShape
import com.otso.app.ui.theme.otsoClickable
import com.otso.app.ui.theme.otsoColors
import com.otso.app.ui.theme.otsoFloatingSolid
import kotlinx.coroutines.launch

/**
 * OtsoHighlighterPopup -- Contextual Highlight Color Picker
 *
 * Appears near selected text when user has a text selection.
 * Floats above the selection area so the user can quickly
 * pick a highlight color without leaving the editing context.
 *
 * Design: Origin-aware popup (Emil Kowalski), scale from 0.95
 * not 0 (never animate from nothing). 40dp hit areas.
 */
@Composable
fun OtsoHighlighterPopup(
    visible: Boolean,
    offsetY: Int,
    onColorSelect: (Color) -> Unit,
    onDismiss: () -> Unit,
) {
    if (!visible) return

    val haptic = LocalHapticFeedback.current
    val colors = MaterialTheme.colorScheme.otsoColors
    val paletteShape = SquircleShape(12.dp)

    // Animatable: values read inside graphicsLayer {} = draw-phase only, zero recompositions per frame.
    val paletteScale = remember { Animatable(0.95f) }
    val paletteAlpha = remember { Animatable(0f) }

    val paletteColors = listOf(
        Color(0xFFF9EB73), // Yellow
        Color(0xFF86EFAC), // Green
        Color(0xFF93C5FD), // Blue
        Color(0xFFFCA5A5), // Red
        Color(0xFFD8B4FE), // Purple
        Color(0xFFFDBA74), // Orange
        Color.Transparent, // Clear
    )

    LaunchedEffect(Unit) {
        launch { paletteScale.animateTo(1f, tween(durationMillis = 170, easing = OtsoMotion.easeOut)) }
        paletteAlpha.animateTo(1f, tween(durationMillis = 130, easing = OtsoMotion.easeOut))
    }

    Popup(
        alignment = Alignment.TopCenter,
        offset = IntOffset(0, offsetY),
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = false)
    ) {
        Row(
            modifier = Modifier
                .graphicsLayer {
                    // Draw-phase reads: Animatable.value inside graphicsLayer lambda
                    // is observed by the draw system, not the composition system —
                    // zero recompositions per animation frame.
                    alpha = paletteAlpha.value
                    scaleX = paletteScale.value
                    scaleY = paletteScale.value
                }
                .otsoFloatingSolid(shape = paletteShape, colors = colors, elevation = 14.dp)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            paletteColors.forEach { color ->
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .otsoClickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onColorSelect(color)
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    if (color == Color.Transparent) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .border(1.5.dp, colors.muted.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = OtsoIcons.X,
                                contentDescription = "Clear",
                                modifier = Modifier.size(12.dp),
                                tint = colors.muted.copy(alpha = 0.5f),
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(color, CircleShape)
                                .border(
                                    0.5.dp,
                                    if (colors.isDarkMode) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f),
                                    CircleShape,
                                )
                        )
                    }
                }
            }
        }
    }
}
