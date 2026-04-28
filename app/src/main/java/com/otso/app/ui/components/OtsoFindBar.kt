package com.otso.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.otso.app.ui.theme.SquircleShape
import com.otso.app.ui.theme.otsoFloatingSolid
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntSize

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
    val toolbarShape = SquircleShape(20.dp)

    val rotation by animateFloatAsState(
        targetValue = if (isReplaceExpanded) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "rotation"
    )
    val replaceExpandSpec = remember {
        spring<IntSize>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow,
        )
    }
    val replaceShrinkSpec = remember {
        spring<IntSize>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium,
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .otsoFloatingSolid(shape = toolbarShape, colors = colors)
            .padding(horizontal = 8.dp)
    ) {
        // ROW 1: Find Control
        Row(
            modifier = Modifier
                .height(44.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                if (findQuery.isEmpty()) {
                    Text(
                        text = "find...",
                        style = OtsoTypography.uiLabel,
                        color = colors.ink.copy(alpha = 0.4f)
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
                    .size(36.dp)
                    .otsoClickable { 
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        isReplaceExpanded = !isReplaceExpanded 
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = OtsoIcons.CaretDown,
                    contentDescription = "Expand",
                    modifier = Modifier
                        .size(16.dp)
                        .graphicsLayer { rotationZ = rotation },
                    tint = if (isReplaceExpanded) colors.accent else colors.ink.copy(alpha = 0.4f),
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
                style = OtsoTypography.uiTechnical.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                ),
                color = colors.ink.copy(alpha = 0.45f),
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            // Close
            FindBarButton(icon = OtsoIcons.X, onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClose()
            })
        }

        // ROW 2: Replace Control
        AnimatedVisibility(
            visible = isReplaceExpanded,
            enter = expandVertically(
                expandFrom = Alignment.Top,
                animationSpec = replaceExpandSpec,
            ) + fadeIn(
                animationSpec = tween(durationMillis = 130),
            ),
            exit = shrinkVertically(
                shrinkTowards = Alignment.Top,
                animationSpec = replaceShrinkSpec,
            ) + fadeOut(
                animationSpec = tween(durationMillis = 100),
            ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .padding(bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                ) {
                    if (replaceQuery.isEmpty()) {
                        Text(
                            text = "replace...",
                            style = OtsoTypography.uiLabel,
                            color = colors.ink.copy(alpha = 0.4f)
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
                FindBarButton(
                    icon = OtsoIcons.ArrowCounterClockwise,
                    modifier = Modifier.offset(y = (-0.5).dp),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onReplaceCurrent()
                    }
                )

                Box(
                    modifier = Modifier
                        .height(28.dp)
                        .padding(horizontal = 4.dp)
                        .otsoClickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onReplaceAll()
                        }
                        .background(colors.accent.copy(alpha = 0.12f), SquircleShape(20.dp))
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "all",
                        style = OtsoTypography.uiTechnical.copy(fontWeight = FontWeight.Medium),
                        color = colors.accent
                    )
                }
            }
        }
    }
}

@Composable
private fun FindBarButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color? = null
) {
    val colors = MaterialTheme.colorScheme.otsoColors
    Box(
        modifier = modifier
            .size(36.dp)
            .otsoClickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = color ?: colors.ink.copy(alpha = 0.65f)
        )
    }
}
