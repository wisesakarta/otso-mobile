package com.otso.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Icon
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.otso.app.ui.theme.OtsoSpacing
import com.otso.app.ui.theme.OtsoTypography
import com.otso.app.ui.theme.otsoClickable
import com.otso.app.ui.theme.otsoColors

@Composable
fun OtsoFindBar(
    findQuery: String,
    replaceQuery: String,
    matchCount: Int,
    activeMatchIndex: Int,
    onFindQueryChange: (String) -> Unit,
    onReplaceQueryChange: (String) -> Unit,
    onFindNext: () -> Unit,
    onFindPrevious: () -> Unit,
    onReplaceCurrent: () -> Unit,
    onReplaceAll: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme.otsoColors
    val haptic = LocalHapticFeedback.current
    var isReplaceExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(if (isReplaceExpanded) 96.dp else 48.dp)
            .background(colors.surface)
            .drawBehind {
                // Top border
                drawLine(
                    color = colors.edge.copy(alpha = 0.3f),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 0.5.dp.toPx()
                )
                // Row Divider (Only if expanded)
                if (isReplaceExpanded) {
                    drawLine(
                        color = colors.edge.copy(alpha = 0.2f),
                        start = Offset(0f, size.height / 2f),
                        end = Offset(size.width, size.height / 2f),
                        strokeWidth = 0.5.dp.toPx()
                    )
                }
            }
            .padding(horizontal = OtsoSpacing.globalMargin)
    ) {
        // ROW 1: Find Control
        Row(
            modifier = Modifier
                .height(48.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (findQuery.isEmpty()) {
                    Text(
                        text = "find...",
                        style = OtsoTypography.uiLabel,
                        color = colors.muted
                    )
                }
                BasicTextField(
                    value = findQuery,
                    onValueChange = onFindQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = OtsoTypography.uiLabel.copy(color = colors.ink),
                    cursorBrush = SolidColor(colors.accent),
                    singleLine = true
                )
            }

            // Expansion Toggle
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .otsoClickable { isReplaceExpanded = !isReplaceExpanded },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (isReplaceExpanded) OtsoIcons.CaretUp else OtsoIcons.CaretDown,
                    contentDescription = "Expand",
                    modifier = Modifier.size(16.dp),
                    tint = if (isReplaceExpanded) colors.accent else colors.muted.copy(alpha = 0.5f),
                )
            }

            // Navigation Controls
            FindBarButton(icon = OtsoIcons.CaretUp, onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onFindPrevious()
            })
            FindBarButton(icon = OtsoIcons.CaretDown, onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onFindNext()
            })

            // Counter
            val activeDisplay = if (matchCount > 0) activeMatchIndex + 1 else 0
            Text(
                text = "[$activeDisplay/$matchCount]",
                style = OtsoTypography.uiTechnical,
                color = colors.muted.copy(alpha = 0.6f)
            )

            // Close
            FindBarButton(icon = OtsoIcons.X, onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClose()
            })
        }

        // ROW 2: Replace Control (Conditional)
        if (isReplaceExpanded) {
            Row(
                modifier = Modifier
                    .height(48.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (replaceQuery.isEmpty()) {
                        Text(
                            text = "replace...",
                            style = OtsoTypography.uiLabel,
                            color = colors.muted
                        )
                    }
                    BasicTextField(
                        value = replaceQuery,
                        onValueChange = onReplaceQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = OtsoTypography.uiLabel.copy(color = colors.ink),
                        cursorBrush = SolidColor(colors.accent),
                        singleLine = true
                    )
                }

                // Replace Actions
                FindBarButton(icon = OtsoIcons.ArrowCounterClockwise, onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onReplaceCurrent()
                })
                FindBarButton(
                    icon = OtsoIcons.Check,
                    color = colors.accent,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onReplaceAll()
                    }
                )
            }
        }
    }
}

@Composable
private fun FindBarButton(
    icon: ImageVector,
    onClick: () -> Unit,
    color: Color? = null
) {
    val colors = MaterialTheme.colorScheme.otsoColors
    Box(
        modifier = Modifier
            .size(40.dp)
            .otsoClickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = color ?: colors.muted
        )
    }
}
