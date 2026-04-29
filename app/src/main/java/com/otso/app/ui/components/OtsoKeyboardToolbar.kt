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
import com.otso.app.ui.theme.otsoClickable
import com.otso.app.ui.theme.OtsoTypography
import com.otso.app.ui.theme.SquircleShape
import com.otso.app.ui.theme.otsoFloatingSolid
import com.otso.app.ui.theme.stackedShadow
import com.otso.app.ui.theme.otsoColors

@Composable
fun OtsoKeyboardToolbar(
    onKeyInsert: (String) -> Unit,
    onFindClick: () -> Unit,
    onScanClick: () -> Unit,
    onMonospaceToggle: () -> Unit,
    isMonospaceActive: Boolean = false,
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
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Scan
                ToolbarButton(
                    icon = OtsoIcons.Camera,
                    colors = colors,
                    modifier = Modifier.offset(y = (-0.5).dp),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onScanClick()
                    },
                )

                // Monospace toggle
                ToolbarButton(
                    label = "M",
                    isActive = isMonospaceActive,
                    accent = accent,
                    colors = colors,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onMonospaceToggle()
                    },
                )

                ToolbarDivider(colors)

                // Insert keys
                val insertKeys = listOf(
                    "tab" to "\t",
                    "( )" to "()",
                    "[ ]" to "[]",
                    "\" \"" to "\"\"",
                    "/" to "/",
                )

                insertKeys.forEach { (label, insert) ->
                    ToolbarButton(
                        label = label,
                        colors = colors,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onKeyInsert(insert)
                        },
                    )
                }
            }

            ToolbarDivider(colors)

            // FIXED END: Find — Accent pill
            Box(
                modifier = Modifier
                    .padding(end = 4.dp, start = 4.dp)
                    .height(34.dp)
                    .widthIn(min = 64.dp)
                    .otsoClickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onFindClick()
                    }
                    .background(accent, SquircleShape(12.dp))
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "find",
                    style = OtsoTypography.uiTechnical.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun ToolbarButton(
    icon: ImageVector? = null,
    label: String? = null,
    isActive: Boolean = false,
    enabled: Boolean = true,
    accent: Color = Color.Unspecified,
    colors: com.otso.app.ui.theme.OtsoColorScheme,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {

    Box(
        modifier = modifier
            .size(36.dp)
            .background(
                color = if (isActive) accent.copy(alpha = 0.12f) else Color.Transparent,
                shape = SquircleShape(16.dp),
            )
            .otsoClickable(
                enabled = enabled, 
                onClick = onClick
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
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
                    fontSize = 11.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
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
            .padding(horizontal = 4.dp)
            .width(1.dp)
            .height(16.dp)
            .background(colors.edge.copy(alpha = 0.15f))
    )
}
