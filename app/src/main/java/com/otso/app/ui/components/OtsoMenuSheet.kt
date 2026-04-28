package com.otso.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.otso.app.ui.theme.OtsoMotion
import com.otso.app.ui.theme.OtsoSpacing
import com.otso.app.ui.theme.OtsoTypography
import com.otso.app.ui.theme.otsoClickable
import com.otso.app.ui.theme.otsoColors
import com.otso.app.ui.theme.StaggeredItem
import com.otso.app.ui.theme.SquircleShape
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun OtsoMenuSheet(
    themeMode: String,
    fontSizeSp: Int,
    onNewTab: () -> Unit,
    onOpenFile: () -> Unit,
    onImportImage: () -> Unit,
    onSave: () -> Unit,
    onSaveAs: () -> Unit,
    onThemeModeChange: (String) -> Unit,
    onFontSizeChange: (Int) -> Unit,
    onLoadCustomFont: () -> Unit,
    onResetCustomFont: () -> Unit,
    isCustomFontLoaded: Boolean,
    customFontName: String?,
    onAboutClick: () -> Unit,
    onTranslateClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme.otsoColors
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp),
    ) {
        // Handle
        Box(
            modifier = Modifier
                .padding(vertical = 12.dp)
                .align(Alignment.CenterHorizontally)
                .width(32.dp)
                .height(4.dp)
                .background(colors.edge.copy(alpha = 0.2f), RoundedCornerShape(100.dp))
        )

        // Group 1 Ã¢â‚¬â€ Core Actions (Back to Flat List)
        StaggeredItem(index = 0) { MenuTextItem("New Tab") { onNewTab(); onDismiss() } }
        StaggeredItem(index = 1) { MenuTextItem("Open File") { onOpenFile(); onDismiss() } }
        StaggeredItem(index = 2) { MenuTextItem("Import Image (OCR)", "experimental") { onImportImage(); onDismiss() } }
        StaggeredItem(index = 3) { MenuTextItem("Translate (ML Kit)", "experimental") { onTranslateClick(); onDismiss() } }
        StaggeredItem(index = 4) { MenuTextItem("Save") { onSave(); onDismiss() } }
        StaggeredItem(index = 5) { MenuTextItem("Save As") { onSaveAs(); onDismiss() } }

        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(colors.edge.copy(alpha = 0.08f)))
        Spacer(modifier = Modifier.height(8.dp))

        // Group 2 Ã¢â‚¬â€ Settings
        StaggeredItem(index = 6) {
            SettingsRow("Theme") {
                SlidingThemeSelector(
                    selectedMode = themeMode,
                    onModeChange = { mode ->
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onThemeModeChange(mode)
                    }
                )
            }
        }

        StaggeredItem(index = 7) {
            SettingsRow("Size") {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StepIcon(OtsoIcons.Minus) { 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onFontSizeChange((fontSizeSp - 1).coerceIn(12, 24)) 
                    }
                    Text(text = "$fontSizeSp", style = OtsoTypography.uiTechnical, color = colors.ink)
                    StepIcon(OtsoIcons.Plus) { 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onFontSizeChange((fontSizeSp + 1).coerceIn(12, 24)) 
                    }
                }
            }
        }

        StaggeredItem(index = 8) {
            SettingsRow("Typeface") {
                if (isCustomFontLoaded) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = customFontName ?: "Custom",
                            style = OtsoTypography.uiTechnical,
                            color = colors.accent,
                        )
                        Icon(
                            imageVector = OtsoIcons.ArrowCounterClockwise,
                            contentDescription = "Reset",
                            modifier = Modifier.size(16.dp).otsoClickable { onResetCustomFont() },
                            tint = colors.muted
                        )
                        Box(
                            modifier = Modifier
                                .otsoClickable { onLoadCustomFont() }
                                .background(colors.edge.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = "Set Font Folder", style = OtsoTypography.uiTechnical, color = colors.ink)
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .otsoClickable { onLoadCustomFont() }
                            .background(colors.edge.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "Set Font Folder", style = OtsoTypography.uiTechnical, color = colors.ink)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(colors.edge.copy(alpha = 0.08f)))
        Spacer(modifier = Modifier.height(8.dp))

        // About
        StaggeredItem(index = 9) {
            MenuTextItem("About Otso") { onAboutClick(); onDismiss() }
        }
    }
}

@Composable
private fun MenuTextItem(
    label: String,
    badge: String? = null,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme.otsoColors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .otsoClickable(onClick = onClick)
            .padding(horizontal = OtsoSpacing.globalMargin),
        contentAlignment = Alignment.CenterStart
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
            if (badge != null) {
                Text(
                    text = badge,
                    style = OtsoTypography.uiCaption.copy(fontSize = 10.sp),
                    color = colors.muted.copy(alpha = 0.5f),
                )
            }
        }
    }
}

@Composable
private fun SettingsRow(
    label: String,
    content: @Composable () -> Unit
) {
    val colors = MaterialTheme.colorScheme.otsoColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = OtsoSpacing.globalMargin, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = OtsoTypography.uiCaption,
            color = colors.muted
        )
        content()
    }
}

@Composable
private fun SlidingThemeSelector(
    selectedMode: String,
    onModeChange: (String) -> Unit,
) {
    val colors = MaterialTheme.colorScheme.otsoColors
    val modes = listOf("system", "dark", "light")
    val coroutineScope = rememberCoroutineScope()
    var localSelectedMode by remember(selectedMode) { mutableStateOf(selectedMode) }
    val selectedIndex = modes.indexOf(localSelectedMode).coerceAtLeast(0)

    // Both offset AND width animate together — pill morphs seamlessly.
    // tween + expo-out instead of spring: fixed duration feels intentional
    // and snappy (most motion in first 30% of 200ms).
    val pillOffset by animateDpAsState(
        targetValue = when (selectedIndex) {
            0 -> 0.dp
            1 -> 62.dp
            else -> 110.dp
        },
        animationSpec = tween(durationMillis = 200, easing = OtsoMotion.easeOut),
        label = "pill_offset"
    )
    val pillWidth by animateDpAsState(
        targetValue = if (selectedIndex == 0) 58.dp else 44.dp,
        animationSpec = tween(durationMillis = 200, easing = OtsoMotion.easeOut),
        label = "pill_width"
    )

    Box(
        modifier = Modifier
            .background(colors.edge.copy(alpha = 0.08f), SquircleShape(100.dp))
            .padding(1.dp)
    ) {
        Box(
            modifier = Modifier
                .offset(x = pillOffset)
                .width(pillWidth)
                .height(28.dp)
                .background(colors.accent.copy(alpha = 0.12f), SquircleShape(100.dp))
                .border(1.dp, colors.accent.copy(alpha = 0.4f), SquircleShape(100.dp))
        )

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            modes.forEachIndexed { index, mode ->
                val isSelected = index == selectedIndex
                val labelColor by animateColorAsState(
                    targetValue = if (isSelected) colors.ink else colors.muted,
                    animationSpec = tween(durationMillis = 160, easing = OtsoMotion.easeOut),
                    label = "label_color_$index",
                )
                Box(
                    modifier = Modifier
                        .height(28.dp)
                        .width(if (index == 0) 58.dp else 44.dp)
                        .otsoClickable {
                            if (localSelectedMode != mode) {
                                localSelectedMode = mode
                                coroutineScope.launch {
                                    delay(220L)
                                    onModeChange(mode)
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mode.replaceFirstChar { it.uppercase() },
                        style = OtsoTypography.uiCaption.copy(
                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                        ),
                        color = labelColor,
                    )
                }
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
        modifier = Modifier.size(32.dp).otsoClickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.material3.Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = colors.accent,
        )
    }
}
