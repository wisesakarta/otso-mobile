package com.otso.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.otso.app.ui.theme.OtsoSpacing
import com.otso.app.ui.theme.OtsoTypography
import com.otso.app.ui.theme.otsoClickable
import com.otso.app.ui.theme.otsoColors
import com.otso.app.ui.theme.OtsoSquircleShape
import com.otso.app.ui.theme.StaggeredItem

@Composable
fun OtsoMenuSheet(
    themeMode: String,
    fontSizeSp: Int,
    onNewTab: () -> Unit,
    onOpenFile: () -> Unit,
    onSave: () -> Unit,
    onSaveAs: () -> Unit,
    onThemeModeChange: (String) -> Unit,
    onFontSizeChange: (Int) -> Unit,
    onLoadCustomFont: () -> Unit,
    onResetCustomFont: () -> Unit,
    isCustomFontLoaded: Boolean,
    customFontName: String?,
    onAboutClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme.otsoColors
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.background)
            .padding(bottom = 32.dp),
    ) {
        // Group 1 — File actions:
        StaggeredItem(index = 0) { MenuItem("New Tab") { onNewTab(); onDismiss() } }
        StaggeredItem(index = 1) { MenuItem("Open File") { onOpenFile(); onDismiss() } }
        StaggeredItem(index = 2) { MenuItem("Save") { onSave(); onDismiss() } }
        StaggeredItem(index = 3) { MenuItem("Save As") { onSaveAs(); onDismiss() } }

        Spacer(modifier = Modifier.height(8.dp))
        StaggeredItem(index = 4) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(colors.edge.copy(alpha = 0.08f)),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Group 2 — Settings:
        // Theme Row with Sliding Selector
        StaggeredItem(index = 5) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = OtsoSpacing.globalMargin, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Theme",
                    style = OtsoTypography.uiCaption,
                    color = colors.muted,
                    modifier = Modifier.weight(1f),
                )
                
                SlidingThemeSelector(
                    selectedMode = themeMode,
                    onModeChange = { mode ->
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onThemeModeChange(mode)
                    }
                )
            }
        }

        // Font Row
        StaggeredItem(index = 6) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = OtsoSpacing.globalMargin, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Size", 
                    style = OtsoTypography.uiCaption,
                    color = colors.muted,
                    modifier = Modifier.weight(1f),
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    StepIcon(OtsoIcons.Minus) { 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onFontSizeChange((fontSizeSp - 1).coerceIn(12, 24)) 
                    }
                    Text(
                        text = "$fontSizeSp",
                        style = OtsoTypography.uiTechnical,
                        color = colors.ink,
                    )
                    StepIcon(OtsoIcons.Plus) { 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onFontSizeChange((fontSizeSp + 1).coerceIn(12, 24)) 
                    }
                }
            }
        }

        // Font Precision — Custom Injection Module
        StaggeredItem(index = 7) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = OtsoSpacing.globalMargin, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Typeface",
                    style = OtsoTypography.uiCaption,
                    color = colors.muted,
                    modifier = Modifier.weight(1f),
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (isCustomFontLoaded) {
                        // Active Badge
                        Box(
                            modifier = Modifier
                                .background(colors.accent.copy(alpha = 0.08f), OtsoSquircleShape(radius = 4.dp, smoothing = 0.8f))
                                .border(1.dp, colors.accent.copy(alpha = 0.2f), OtsoSquircleShape(radius = 4.dp, smoothing = 0.8f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = customFontName ?: "Custom",
                                style = OtsoTypography.uiTechnical,
                                color = colors.accent,
                            )
                        }
                        
                        // Reset Button
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .otsoClickable(onClick = onResetCustomFont),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = OtsoIcons.ArrowCounterClockwise,
                                contentDescription = "Reset Font",
                                modifier = Modifier.size(16.dp),
                                tint = colors.muted
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .otsoClickable(onClick = onLoadCustomFont)
                                .background(colors.edge.copy(alpha = 0.08f), OtsoSquircleShape(smoothing = 0.8f))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(
                                    imageVector = OtsoIcons.Plus,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = colors.ink
                                )
                                Text(
                                    text = "Inject",
                                    style = OtsoTypography.uiTechnical.copy(letterSpacing = 0.6.sp),
                                    color = colors.ink
                                )
                            }
                        }
                    }
                }
            }
        }

        StaggeredItem(index = 8) {
            MenuItem("About Otso") {
                onAboutClick()
                onDismiss()
            }
        }
    }
}

@Composable
private fun SlidingThemeSelector(
    selectedMode: String,
    onModeChange: (String) -> Unit,
) {
    val colors = MaterialTheme.colorScheme.otsoColors
    val modes = listOf("system", "dark", "light")
    val selectedIndex = modes.indexOf(selectedMode).coerceAtLeast(0)
    
    // DNA: Sliding Pill Animation (Emil Design Engineering)
    val pillOffset by animateDpAsState(
        targetValue = when(selectedIndex) {
            0 -> 0.dp
            1 -> 62.dp // Calculated based on fixed widths
            else -> 110.dp
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy),
        label = "pill_offset"
    )

    Box(
        modifier = Modifier
            .background(colors.edge.copy(alpha = 0.08f), OtsoSquircleShape(smoothing = 0.8f))
            .padding(1.dp)
    ) {
        // The Sliding Indicator (Pill Style)
        Box(
            modifier = Modifier
                .offset(x = pillOffset)
                .width(if (selectedIndex == 0) 58.dp else 44.dp)
                .height(28.dp)
                .background(colors.accent.copy(alpha = 0.12f), OtsoSquircleShape(smoothing = 0.8f))
                .border(1.dp, colors.accent.copy(alpha = 0.4f), OtsoSquircleShape(smoothing = 0.8f))
        )

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            modes.forEachIndexed { index, mode ->
                val isSelected = index == selectedIndex
                Box(
                    modifier = Modifier
                        .height(28.dp)
                        .width(if (index == 0) 58.dp else 44.dp)
                        .otsoClickable { onModeChange(mode) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mode.capitalize(),
                        style = OtsoTypography.uiCaption,
                        color = if (isSelected) colors.ink else colors.muted,
                    )
                }
            }
        }
    }
}

@Composable
private fun MenuItem(
    label: String,
    trailingText: String? = null,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme.otsoColors
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "menu_item_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .otsoClickable(onClick = onClick)
            .padding(horizontal = OtsoSpacing.globalMargin),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = OtsoTypography.uiLabel,
                color = colors.ink,
            )

            if (trailingText != null) {
                Text(
                    text = trailingText,
                    style = OtsoTypography.uiCaption,
                    color = colors.muted,
                )
            }
        }
    }
}

@Composable
private fun StepIcon(
    icon: ImageVector,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme.otsoColors
    
    Box(
        modifier = Modifier
            .size(32.dp)
            .otsoClickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = colors.accent,
        )
    }
}
