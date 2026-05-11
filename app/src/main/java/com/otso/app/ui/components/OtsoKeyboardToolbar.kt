package com.otso.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import com.otso.app.ui.theme.otsoClickable
import com.otso.app.ui.theme.OtsoTypography
import com.otso.app.ui.theme.SquircleShape
import com.otso.app.ui.theme.otsoFloatingSolid
import com.otso.app.ui.theme.stackedShadow
import com.otso.app.ui.theme.otsoColors
import com.otso.app.ui.theme.StaggeredItem

@Composable
fun OtsoKeyboardToolbar(
    onKeyInsert: (String) -> Unit,
    onFindClick: () -> Unit,
    onScanClick: () -> Unit,
    onMonospaceToggle: () -> Unit,
    isMonospaceActive: Boolean = false,
    onUndo: () -> Unit = {},
    onRedo: () -> Unit = {},
    canUndo: Boolean = false,
    canRedo: Boolean = false,
    onSelectAll: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme.otsoColors
    val haptic = LocalHapticFeedback.current
    val accent = colors.accent
    val toolbarShape = SquircleShape(20.dp)
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .stackedShadow(shape = toolbarShape)
                .otsoFloatingSolid(shape = toolbarShape, colors = colors, drawBorder = false)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // SCROLLABLE MIDDLE: Utilities & Insert Keys
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(scrollState)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // DNA: Logical Grouping (Undo/Redo)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Undo
                    StaggeredItem(index = 0) {
                        ToolbarButton(
                            icon = OtsoIcons.Undo,
                            contentDescription = "Undo",
                            enabled = canUndo,
                            colors = colors,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onUndo()
                            },
                        )
                    }

                    // Redo
                    StaggeredItem(index = 1) {
                        ToolbarButton(
                            icon = OtsoIcons.Redo,
                            contentDescription = "Redo",
                            enabled = canRedo,
                            colors = colors,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onRedo()
                            },
                        )
                    }
                }

                ToolbarDivider(colors)

                // DNA: Utilities Group
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Scan
                    StaggeredItem(index = 2) {
                        ToolbarButton(
                            icon = OtsoIcons.Camera,
                            contentDescription = "Scan Document",
                            colors = colors,
                            modifier = Modifier.offset(y = (-0.2).dp),
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onScanClick()
                            },
                        )
                    }

                    // Monospace toggle
                    StaggeredItem(index = 3) {
                        ToolbarButton(
                            icon = OtsoIcons.LetterM,
                            contentDescription = if (isMonospaceActive) "Disable Monospace Font" else "Enable Monospace Font",
                            isActive = isMonospaceActive,
                            accent = accent,
                            colors = colors,
                            modifier = Modifier.offset(y = (-0.2).dp),
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onMonospaceToggle()
                            },
                        )
                    }

                    // Select All
                    StaggeredItem(index = 4) {
                        ToolbarButton(
                            icon = OtsoIcons.SelectAll,
                            contentDescription = "Select All",
                            colors = colors,
                            modifier = Modifier.offset(y = (-0.2).dp),
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSelectAll()
                            },
                        )
                    }
                }

                ToolbarDivider(colors)

                // DNA: Quick Insert Group
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val insertKeys = listOf(
                        OtsoIcons.TabKey to "\t",
                        OtsoIcons.Parentheses to "()",
                        OtsoIcons.Brackets to "[]",
                        OtsoIcons.Quotes to "\"\"",
                        OtsoIcons.Slash to "/",
                    )

                    insertKeys.forEachIndexed { index, (icon, insert) ->
                        StaggeredItem(index = index + 6) {
                            ToolbarButton(
                                icon = icon,
                                contentDescription = "Insert control",
                                colors = colors,
                                modifier = Modifier.offset(y = (-0.2).dp),
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onKeyInsert(insert)
                                },
                            )
                        }
                    }
                }
            }

            ToolbarDivider(colors)

            // FIXED END: Find — Accent pill
            StaggeredItem(index = 12) { // Delayed entry for the final action
                Box(
                    modifier = Modifier
                        .padding(end = 4.dp, start = 4.dp)
                        .height(34.dp)
                        .widthIn(min = 64.dp)
                        .otsoClickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onFindClick()
                        }
                        .semantics(mergeDescendants = true) {
                            contentDescription = "Find in document"
                        }
                        .background(accent, SquircleShape(12.dp))
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "find",
                        style = OtsoTypography.uiTechnical.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun ToolbarButton(
    icon: ImageVector? = null,
    label: String? = null,
    contentDescription: String? = null,
    isActive: Boolean = false,
    enabled: Boolean = true,
    accent: Color = Color.Unspecified,
    colors: com.otso.app.ui.theme.OtsoColorScheme,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {

    Box(
        modifier = modifier
            .size(40.dp)
            .background(
                color = if (isActive) accent.copy(alpha = 0.12f) else Color.Transparent,
                shape = SquircleShape(16.dp),
            )
            .semantics(mergeDescendants = true) {
                if (contentDescription != null) {
                    this.contentDescription = contentDescription
                }
            }
            .otsoClickable(
                enabled = enabled, 
                onClick = onClick
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null, // Handled by Box semantics
                modifier = Modifier.size(20.dp), 
                tint = when {
                    !enabled -> colors.muted.copy(alpha = 0.25f)
                    isActive -> accent
                    else -> colors.ink.copy(alpha = 0.65f)
                }
            )
        } else if (label != null) {
            Text(
                text = label,
                style = OtsoTypography.uiTechnical.copy(
                    fontSize = 12.5.sp, // Optical increase from 11sp
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold // Increased weight for balance
                ),
                color = when {
                    !enabled -> colors.muted.copy(alpha = 0.25f)
                    isActive -> accent
                    else -> colors.ink.copy(alpha = 0.65f)
                }
            )
        }
    }
}

@Composable
private fun ToolbarDivider(colors: com.otso.app.ui.theme.OtsoColorScheme) {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(16.dp)
            .background(colors.edge.copy(alpha = 0.15f))
    )
}
