package com.otso.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.otso.app.ui.theme.OtsoTypography
import com.otso.app.ui.theme.OtsoColorScheme
import com.otso.app.ui.theme.otsoColors
import com.otso.app.ui.theme.OtsoSquircleShape

@Composable
fun OtsoKeyboardToolbar(
    onKeyInsert: (String) -> Unit,
    onFindClick: () -> Unit,
    onScanClick: () -> Unit,
    onUndoClick: () -> Unit,
    onRedoClick: () -> Unit,
    onMonoToggle: () -> Unit,
    isMonospace: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme.otsoColors
    val isDark = colors.isDarkMode

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .shadow(
                    elevation = if (isDark) 0.dp else 4.dp,
                    shape = OtsoSquircleShape(smoothing = 0.8f),
                    ambientColor = Color.Black.copy(alpha = 0.04f),
                    spotColor = Color.Black.copy(alpha = 0.04f),
                )
                .background(
                    color = colors.surface,
                    shape = OtsoSquircleShape(smoothing = 0.8f),
                )
                .border(
                    width = 1.dp,
                    color = colors.edge.copy(alpha = if (isDark) 0.3f else 0.15f),
                    shape = OtsoSquircleShape(smoothing = 0.8f),
                )
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            val haptic = LocalHapticFeedback.current
            
            // Scan Button
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        color = colors.edge.copy(alpha = 0.08f),
                        shape = OtsoSquircleShape(smoothing = 0.8f),
                    )
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onScanClick()
                    },
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.material3.Icon(
                    imageVector = OtsoIcons.Camera,
                    contentDescription = "Scan",
                    modifier = Modifier.size(18.dp),
                    tint = colors.ink.copy(alpha = 0.65f),
                )
            }

            // Monospace Toggle
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        color = if (isMonospace) colors.accent.copy(alpha = 0.12f) else colors.edge.copy(alpha = 0.08f),
                        shape = OtsoSquircleShape(smoothing = 0.8f),
                    )
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onMonoToggle()
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "M",
                    style = OtsoTypography.uiLabelMedium.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                    color = if (isMonospace) colors.accent else colors.ink.copy(alpha = 0.65f),
                )
            }

            val keys = listOf("tab" to "\t", "( )" to "()", "[ ]" to "[]", "\" \"" to "\"\"", "/" to "/")

            keys.forEach { (label, insert) ->
                ToolbarKey(
                    label = label,
                    colors = colors,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onKeyInsert(insert)
                    },
                )
            }

            // Undo
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onUndoClick()
                    },
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.material3.Icon(
                    imageVector = OtsoIcons.Undo,
                    contentDescription = "Undo",
                    modifier = Modifier.size(18.dp),
                    tint = colors.ink.copy(alpha = 0.65f),
                )
            }

            // Redo
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onRedoClick()
                    },
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.material3.Icon(
                    imageVector = OtsoIcons.Redo,
                    contentDescription = "Redo",
                    modifier = Modifier.size(18.dp),
                    tint = colors.ink.copy(alpha = 0.65f),
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Find key
            Box(
                modifier = Modifier
                    .height(28.dp)
                    .widthIn(min = 48.dp)
                    .background(
                        color = colors.accent.copy(alpha = 0.12f),
                        shape = OtsoSquircleShape(smoothing = 0.8f),
                    )
                    .border(
                        width = 1.dp,
                        color = colors.accent.copy(alpha = 0.25f),
                        shape = OtsoSquircleShape(smoothing = 0.8f),
                    )
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onFindClick()
                    }
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "find",
                    style = OtsoTypography.uiCaption.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                    color = colors.accent,
                )
            }
        }
    }
}

@Composable
private fun ToolbarKey(
    label: String,
    colors: OtsoColorScheme,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .height(28.dp)
            .widthIn(min = 32.dp)
            .background(
                color = colors.edge.copy(alpha = 0.08f),
                shape = OtsoSquircleShape(smoothing = 0.8f),
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
            )
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = OtsoTypography.uiCaption.copy(fontSize = 11.sp),
            color = colors.ink.copy(alpha = 0.65f),
        )
    }
}
