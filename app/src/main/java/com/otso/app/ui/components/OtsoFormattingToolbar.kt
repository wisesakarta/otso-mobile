package com.otso.app.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohamedrejeb.richeditor.model.RichTextState
import com.otso.app.ui.theme.OtsoSquircleShape
import com.otso.app.ui.theme.OtsoTypography
import com.otso.app.ui.theme.otsoColors

/**
 * OtsoFormattingToolbar — Rich Text Formatting Controls
 *
 * Displays inline formatting options (bold, italic, underline, strike,
 * lists, code, link) as a horizontally scrollable toolbar pill.
 * Highlighter is NOT here — it appears as a contextual popup near
 * selected text (OtsoHighlighterPopup).
 *
 * Design: Emil Kowalski + Jakub Krehel standards.
 * - 40dp minimum hit area per button
 * - Concentric border radii (outer 6dp, inner 4dp with 6dp padding)
 * - Scale 0.96 on press via otsoClickable (inherited from theme)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OtsoFormattingToolbar(
    richTextState: RichTextState,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    onLinkClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme.otsoColors
    val haptic = LocalHapticFeedback.current
    val accent = colors.accent
    val scrollState = rememberScrollState()
    val toolbarShape = OtsoSquircleShape(smoothing = 0.8f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(colors.surface, toolbarShape)
                .border(
                    1.dp,
                    colors.edge.copy(alpha = if (isDark) 0.3f else 0.15f),
                    toolbarShape,
                )
                .horizontalScroll(scrollState)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Bold
            FormatButton(
                icon = OtsoIcons.TextB,
                isActive = richTextState.currentSpanStyle.fontWeight == FontWeight.Bold,
                accent = accent,
                colors = colors,
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                toggleSpanStyleForSelection(
                    richTextState = richTextState,
                    style = SpanStyle(fontWeight = FontWeight.Bold),
                    isActive = richTextState.currentSpanStyle.fontWeight == FontWeight.Bold,
                )
            }

            // Italic
            FormatButton(
                icon = OtsoIcons.TextItalic,
                isActive = richTextState.currentSpanStyle.fontStyle == androidx.compose.ui.text.font.FontStyle.Italic,
                accent = accent,
                colors = colors,
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                toggleSpanStyleForSelection(
                    richTextState = richTextState,
                    style = SpanStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                    isActive = richTextState.currentSpanStyle.fontStyle == androidx.compose.ui.text.font.FontStyle.Italic,
                )
            }

            // Underline
            FormatButton(
                icon = OtsoIcons.TextUnderline,
                isActive = richTextState.currentSpanStyle.textDecoration?.contains(
                    androidx.compose.ui.text.style.TextDecoration.Underline
                ) == true,
                accent = accent,
                colors = colors
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                toggleSpanStyleForSelection(
                    richTextState = richTextState,
                    style = SpanStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline),
                    isActive = richTextState.currentSpanStyle.textDecoration?.contains(
                        androidx.compose.ui.text.style.TextDecoration.Underline
                    ) == true,
                )
            }

            // Strikethrough
            FormatButton(
                icon = OtsoIcons.TextStrikethrough,
                isActive = richTextState.currentSpanStyle.textDecoration?.contains(
                    androidx.compose.ui.text.style.TextDecoration.LineThrough
                ) == true,
                accent = accent,
                colors = colors
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                toggleSpanStyleForSelection(
                    richTextState = richTextState,
                    style = SpanStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough),
                    isActive = richTextState.currentSpanStyle.textDecoration?.contains(
                        androidx.compose.ui.text.style.TextDecoration.LineThrough
                    ) == true,
                )
            }

            Divider(colors)

            // Bullet list
            FormatButton(
                icon = OtsoIcons.ListBullets,
                isActive = richTextState.isUnorderedList,
                accent = accent,
                colors = colors
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                richTextState.toggleUnorderedList()
            }

            // Numbered list
            FormatButton(
                icon = OtsoIcons.ListNumbers,
                isActive = richTextState.isOrderedList,
                accent = accent,
                colors = colors
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                richTextState.toggleOrderedList()
            }

            Divider(colors)

            // Code
            FormatButton(
                icon = OtsoIcons.Code,
                isActive = richTextState.isCodeSpan,
                accent = accent,
                colors = colors
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                richTextState.toggleCodeSpan()
            }

            // Link
            FormatButton(
                icon = OtsoIcons.Link,
                isActive = richTextState.isLink,
                accent = accent,
                colors = colors
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                if (richTextState.isLink) richTextState.removeLink()
                else onLinkClick()
            }
        }
    }
}

private fun toggleSpanStyleForSelection(
    richTextState: RichTextState,
    style: SpanStyle,
    isActive: Boolean,
) {
    val selectionSnapshot = richTextState.selection
    if (selectionSnapshot.start == selectionSnapshot.end) {
        richTextState.toggleSpanStyle(style)
        return
    }

    val range = TextRange(selectionSnapshot.min, selectionSnapshot.max)
    if (isActive) {
        richTextState.removeSpanStyle(style, range)
    } else {
        richTextState.addSpanStyle(style, range)
    }
    richTextState.selection = range
}

@Composable
private fun FormatButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    label: String? = null,
    isActive: Boolean,
    accent: Color,
    colors: com.otso.app.ui.theme.OtsoColorScheme,
    fontWeight: FontWeight? = null,
    fontStyle: androidx.compose.ui.text.font.FontStyle? = null,
    onClick: () -> Unit,
) {
    val buttonShape = OtsoSquircleShape(smoothing = 0.8f)
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .height(26.dp).widthIn(min = 40.dp)
            .background(
                color = if (isActive) accent.copy(alpha = 0.12f)
                        else Color.Transparent,
                shape = buttonShape,
            )
            .border(
                width = if (isActive) 1.dp else 0.dp,
                color = if (isActive) accent.copy(alpha = 0.25f)
                        else Color.Transparent,
                shape = buttonShape,
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (isActive) accent else colors.ink.copy(alpha = 0.65f)
            )
        } else if (label != null) {
            Text(
                text = label,
                style = OtsoTypography.uiCaption.copy(
                    fontSize = 11.sp,
                    fontWeight = fontWeight ?: FontWeight.Normal,
                    fontStyle = fontStyle ?: androidx.compose.ui.text.font.FontStyle.Normal,
                ),
                color = if (isActive) accent else colors.ink.copy(alpha = 0.65f),
            )
        }
    }
}

@Composable
private fun Divider(colors: com.otso.app.ui.theme.OtsoColorScheme) {
    Box(
        modifier = Modifier
            .padding(horizontal = 2.dp)
            .width(1.dp)
            .height(16.dp)
            .background(colors.edge.copy(alpha = 0.15f))
    )
}
