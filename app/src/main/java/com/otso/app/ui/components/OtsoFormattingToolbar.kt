package com.otso.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.otso.app.model.SpanStyleType
import com.otso.app.ui.theme.OtsoColorScheme
import com.otso.app.ui.theme.OtsoMotion
import com.otso.app.ui.theme.OtsoTypography
import com.otso.app.ui.theme.SquircleShape
import com.otso.app.ui.theme.otsoClickable
import com.otso.app.ui.theme.otsoColors
import com.otso.app.ui.theme.otsoFloatingSolid
import com.otso.app.viewmodel.RichTextState

private val DefaultHighlightPalette = listOf(
    Color(0xFFF9EB73),
    Color(0xFFFDBA74),
    Color(0xFFFCA5A5),
    Color(0xFFD8B4FE),
    Color(0xFF93C5FD),
    Color(0xFF86EFAC),
)

@Composable
fun OtsoFormattingToolbar(
    richTextState: RichTextState,
    onLinkClick: () -> Unit,
    customHighlightPalette: List<Int>,
    activeHighlightHex: String?,
    onHighlightColorChange: (String) -> Unit,
    onOpenColorPicker: (String?) -> Unit,
    onCustomHighlightRemove: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme.otsoColors
    val haptic = LocalHapticFeedback.current
    val toolbarShape = SquircleShape(20.dp)
    var isColorPickerVisible by remember { mutableStateOf(false) }
    val customPalette = customHighlightPalette.distinct()
    val isBoldActive = richTextState.hasStyle(SpanStyleType.Bold)
    val isItalicActive = richTextState.hasStyle(SpanStyleType.Italic)
    val isStrikethroughActive = richTextState.hasStyle(SpanStyleType.Strikethrough)
    val isUnderlineActive = richTextState.hasStyle(SpanStyleType.Underline)
    val isCodeActive = richTextState.hasStyle(SpanStyleType.Code)
    val activeHighlightColorInt = remember(activeHighlightHex) {
        parseHexColor(activeHighlightHex)?.toArgb()
    }

    fun apply(action: () -> Unit) {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        action()
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .otsoFloatingSolid(shape = toolbarShape, colors = colors)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isColorPickerVisible) Arrangement.Start else Arrangement.Center,
        ) {
            AnimatedContent(
                targetState = isColorPickerVisible,
                transitionSpec = {
                    (fadeIn(tween(durationMillis = 160, easing = OtsoMotion.easeOut)) +
                            scaleIn(
                                initialScale = 0.98f,
                                animationSpec = tween(durationMillis = 180, easing = OtsoMotion.easeOut),
                            )) togetherWith
                            (fadeOut(tween(durationMillis = 100, easing = OtsoMotion.easeInOut)) +
                                    scaleOut(
                                        targetScale = 0.98f,
                                        animationSpec = tween(durationMillis = 120, easing = OtsoMotion.easeInOut),
                                    ))
                },
                label = "toolbar_mode_switch",
            ) { pickerVisible ->
                if (pickerVisible) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .otsoClickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    isColorPickerVisible = false
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = OtsoIcons.ArrowLeft,
                                contentDescription = "Close color picker",
                                modifier = Modifier.size(18.dp),
                                tint = colors.ink.copy(alpha = 0.65f),
                            )
                        }

                        FormattingDivider(colors)

                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            DefaultHighlightPalette.forEach { color ->
                                ColorSwatch(
                                    color = color,
                                    isDarkMode = colors.isDarkMode,
                                    isSelected = activeHighlightColorInt?.let { isSameRgb(it, color.toArgb()) } == true,
                                    onClick = {
                                        val hex = color.toHexString()
                                        apply { richTextState.addHighlight(hex) }
                                        onHighlightColorChange(hex)
                                        isColorPickerVisible = false
                                    },
                                )
                            }

                            customPalette.forEach { colorInt ->
                                val color = colorInt.toComposeColor() ?: return@forEach
                                ColorSwatch(
                                    color = color,
                                    isDarkMode = colors.isDarkMode,
                                    isSelected = activeHighlightColorInt?.let { isSameRgb(it, color.toArgb()) } == true,
                                    onClick = {
                                        val hex = color.toHexString()
                                        apply { richTextState.addHighlight(hex) }
                                        onHighlightColorChange(hex)
                                        isColorPickerVisible = false
                                    },
                                    onLongClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onCustomHighlightRemove(colorInt)
                                    },
                                )
                            }

                            CustomColorSwatch(
                                colors = colors,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onOpenColorPicker(activeHighlightHex)
                                },
                            )

                            ClearSwatch(
                                colors = colors,
                                onClick = {
                                    apply { richTextState.clearHighlight() }
                                    isColorPickerVisible = false
                                },
                            )
                        }
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        FormattingButton(
                            icon = OtsoIcons.TextB,
                            contentDescription = "Bold",
                            isActive = isBoldActive,
                            colors = colors,
                            onClick = { apply { richTextState.toggleStyle(SpanStyleType.Bold) } },
                        )
                        FormattingButton(
                            icon = OtsoIcons.TextItalic,
                            contentDescription = "Italic",
                            isActive = isItalicActive,
                            colors = colors,
                            onClick = { apply { richTextState.toggleStyle(SpanStyleType.Italic) } },
                        )
                        FormattingButton(
                            icon = OtsoIcons.TextStrikethrough,
                            contentDescription = "Strikethrough",
                            isActive = isStrikethroughActive,
                            colors = colors,
                            onClick = { apply { richTextState.toggleStyle(SpanStyleType.Strikethrough) } },
                        )
                        FormattingButton(
                            icon = OtsoIcons.TextUnderline,
                            contentDescription = "Underline",
                            isActive = isUnderlineActive,
                            colors = colors,
                            onClick = { apply { richTextState.toggleStyle(SpanStyleType.Underline) } },
                        )
                        FormattingButton(
                            icon = OtsoIcons.Code,
                            contentDescription = "Code",
                            isActive = isCodeActive,
                            colors = colors,
                            onClick = { apply { richTextState.toggleStyle(SpanStyleType.Code) } },
                        )
                        FormattingDivider(colors)
                        FormattingButton(
                            icon = OtsoIcons.Highlighter,
                            contentDescription = "Highlight",
                            colorSwatch = activeHighlightHex?.let { parseHexColor(it) } ?: colors.accent,
                            colors = colors,
                            onClick = { isColorPickerVisible = true },
                        )
                        FormattingDivider(colors)
                        FormattingButton(
                            icon = OtsoIcons.Link,
                            contentDescription = "Link",
                            colors = colors,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onLinkClick()
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FormattingButton(
    icon: ImageVector,
    contentDescription: String,
    colorSwatch: Color? = null,
    isActive: Boolean = false,
    colors: OtsoColorScheme,
    onClick: () -> Unit,
) {
    val iconTint by animateColorAsState(
        targetValue = if (isActive) colors.accent else colors.ink.copy(alpha = 0.65f),
        animationSpec = tween(durationMillis = 150, easing = OtsoMotion.easeOut),
        label = "format_icon_tint",
    )
    val activeBg by animateColorAsState(
        targetValue = if (isActive) colors.accent.copy(alpha = 0.12f) else Color.Transparent,
        animationSpec = tween(durationMillis = 150, easing = OtsoMotion.easeOut),
        label = "format_active_bg",
    )

    Box(
        modifier = Modifier
            .size(36.dp)
            .background(activeBg, SquircleShape(14.dp))
            .otsoClickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(if (colorSwatch != null) 16.dp else 18.dp),
                tint = iconTint,
            )
            if (colorSwatch != null) {
                Box(
                    modifier = Modifier
                        .width(14.dp)
                        .height(2.5.dp)
                        .background(colorSwatch, RoundedCornerShape(50)),
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun ColorSwatch(
    color: Color,
    isDarkMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
) {
    val borderColor = if (isDarkMode) {
        Color.White.copy(alpha = 0.15f)
    } else {
        Color.Black.copy(alpha = 0.10f)
    }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val swatchScale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.96f
            isSelected -> 1.06f
            else -> 1f
        },
        animationSpec = tween(durationMillis = 140, easing = OtsoMotion.easeOut),
        label = "swatch_scale",
    )
    val strokeWidth by animateDpAsState(
        targetValue = if (isSelected) 2.dp else 0.5.dp,
        animationSpec = tween(durationMillis = 150, easing = OtsoMotion.easeOut),
        label = "swatch_stroke",
    )
    val selectedRingColor by animateColorAsState(
        targetValue = if (isSelected) color.copy(alpha = 0.32f) else Color.Transparent,
        animationSpec = tween(durationMillis = 150, easing = OtsoMotion.easeOut),
        label = "swatch_ring_color",
    )
    val swatchModifier = Modifier.combinedClickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = onClick,
        onLongClick = { onLongClick?.invoke() },
    )

    Box(
        modifier = Modifier
            .size(32.dp)
            .graphicsLayer {
                scaleX = swatchScale
                scaleY = swatchScale
            }
            .then(swatchModifier),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(color, CircleShape)
                .border(strokeWidth, borderColor, CircleShape),
        )
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .border(1.dp, selectedRingColor, CircleShape),
            )
        }
    }
}

@Composable
private fun CustomColorSwatch(
    colors: OtsoColorScheme,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .otsoClickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .border(1.2.dp, colors.edge, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "◉",
                style = OtsoTypography.uiTechnical,
                color = Color.Red,
            )
        }
    }
}

@Composable
private fun ClearSwatch(
    colors: OtsoColorScheme,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .otsoClickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .border(1.5.dp, colors.muted.copy(alpha = 0.35f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = OtsoIcons.X,
                contentDescription = "Clear highlight",
                modifier = Modifier.size(11.dp),
                tint = colors.muted.copy(alpha = 0.55f),
            )
        }
    }
}

@Composable
private fun FormattingDivider(colors: OtsoColorScheme) {
    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .width(1.dp)
            .height(16.dp)
            .background(colors.edge.copy(alpha = 0.15f)),
    )
}

private fun Color.toHexString(): String {
    val colorInt = toArgb()
    return String.format("#%06X", (0xFFFFFF and colorInt))
}

private fun parseHexColor(hex: String?): Color? {
    val normalized = hex?.trim()?.removePrefix("#") ?: return null
    if (normalized.length != 6) return null
    return runCatching {
        Color(android.graphics.Color.parseColor("#$normalized"))
    }.getOrNull()
}

private fun Int.toComposeColor(): Color? {
    return runCatching {
        Color(this)
    }.getOrNull()
}

private fun isSameRgb(left: Int, right: Int): Boolean {
    return (left and 0xFFFFFF) == (right and 0xFFFFFF)
}
