package com.otso.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.otso.app.ui.theme.OtsoSquircleShape
import com.otso.app.ui.theme.otsoClickable
import com.otso.app.ui.theme.otsoColors

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
    val paletteShape = OtsoSquircleShape(radius = 12.dp, smoothing = 0.8f)

    val paletteColors = listOf(
        Color(0xFFF9EB73), // Yellow
        Color(0xFF86EFAC), // Green
        Color(0xFF93C5FD), // Blue
        Color(0xFFFCA5A5), // Red
        Color(0xFFD8B4FE), // Purple
        Color(0xFFFDBA74), // Orange
        Color.Transparent, // Clear
    )

    Popup(
        alignment = Alignment.TopCenter,
        offset = IntOffset(0, offsetY),
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = false)
    ) {
        Row(
            modifier = Modifier
                .shadow(
                    elevation = 12.dp,
                    shape = paletteShape,
                    ambientColor = Color.Black.copy(alpha = 0.12f),
                    spotColor = Color.Black.copy(alpha = 0.08f),
                )
                .background(colors.surface, paletteShape)
                .border(
                    1.dp,
                    colors.edge.copy(alpha = if (colors.isDarkMode) 0.2f else 0.1f),
                    paletteShape,
                )
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
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
                        // Clear highlight button: shows X mark
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .border(
                                    1.5.dp,
                                    colors.muted.copy(alpha = 0.3f),
                                    CircleShape,
                                ),
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
                                    1.dp,
                                    Color.Black.copy(alpha = 0.08f),
                                    CircleShape,
                                )
                        )
                    }
                }
            }
        }
    }
}
