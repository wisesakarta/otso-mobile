package com.otso.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.ui.unit.IntOffset
import com.otso.app.ui.theme.OtsoMotion
import com.otso.app.ui.theme.OtsoSpacing
import com.otso.app.ui.theme.OtsoTypography
import com.otso.app.ui.theme.otsoClickable
import com.otso.app.ui.theme.otsoColors
import com.otso.app.ui.theme.SquircleShape
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.otso.app.ui.theme.StaggeredItem
import com.otso.app.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.text.NumberFormat


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
    createdAt: Long = 0L,
    lastModified: Long = 0L,
    wordCount: Int = 0,
    characterCount: Int = 0,
    onDismiss: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme.otsoColors
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp),
    ) {
        // Group 1 — Core Actions (Back to Flat List)
        StaggeredItem(0) { MenuTextItem("New Tab") { onNewTab(); onDismiss() } }
        StaggeredItem(1) { MenuTextItem("Open File") { onOpenFile(); onDismiss() } }
        StaggeredItem(2) { MenuTextItem("Import Image (OCR)", "experimental") { onImportImage(); onDismiss() } }
        StaggeredItem(3) { MenuTextItem("Translate (ML Kit)", "experimental") { onTranslateClick(); onDismiss() } }
        StaggeredItem(4) { MenuTextItem("Save") { onSave(); onDismiss() } }
        StaggeredItem(5) { MenuTextItem("Save As") { onSaveAs(); onDismiss() } }

        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(colors.edge.copy(alpha = 0.08f)))
        Spacer(modifier = Modifier.height(8.dp))

        // Group 2 — Settings
        StaggeredItem(6) {
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
        StaggeredItem(7) {
            SettingsRow("Size") {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StepIcon(OtsoIcons.Minus, "Decrease Font Size") {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onFontSizeChange((fontSizeSp - 1).coerceIn(12, 24))
                    }
                    AnimatedContent(
                        targetState = fontSizeSp,
                        transitionSpec = {
                            val springSpec = spring<IntOffset>(stiffness = 900f, dampingRatio = Spring.DampingRatioNoBouncy)
                            if (targetState > initialState) {
                                slideInVertically(springSpec) { it } +
                                        fadeIn(spring(stiffness = 900f)) togetherWith
                                        slideOutVertically(springSpec) { -it } + fadeOut(spring(stiffness = 700f))
                            } else {
                                slideInVertically(springSpec) { -it } +
                                        fadeIn(spring(stiffness = 900f)) togetherWith
                                        slideOutVertically(springSpec) { it } + fadeOut(spring(stiffness = 700f))
                            }
                        },
                        label = "font_size_counter",
                    ) { size ->
                        Text(text = "$size", style = OtsoTypography.uiTechnical, color = colors.ink)
                    }
                    StepIcon(OtsoIcons.Plus, "Increase Font Size") {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onFontSizeChange((fontSizeSp + 1).coerceIn(12, 24))
                    }
                }
            }
        }

        StaggeredItem(8) {
            SettingsRow("Typeface") {
                if (isCustomFontLoaded) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = customFontName ?: "Custom",
                            style = OtsoTypography.uiTechnical,
                            color = colors.ink.copy(alpha = 0.75f),
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

        // Group 3 — Document Info
        if (createdAt > 0L || lastModified > 0L) {
            StaggeredItem(9) {
                DocumentInfoRow("Created", formatAdaptiveDate(createdAt))
            }
            StaggeredItem(10) {
                DocumentInfoRow("Modified", formatAdaptiveDate(lastModified))
            }
            StaggeredItem(11) {
                DocumentInfoRow("Words", formatNumber(wordCount))
            }
            StaggeredItem(12) {
                DocumentInfoRow("Characters", formatNumber(characterCount))
            }

            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(colors.edge.copy(alpha = 0.08f)))
            Spacer(modifier = Modifier.height(8.dp))
        }

        StaggeredItem(11) { MenuTextItem("About Otso") { onAboutClick(); onDismiss() } }
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
                Box(
                    modifier = Modifier
                        .background(
                            color = colors.ink.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(4.dp),
                        )
                        .padding(horizontal = 5.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = badge,
                        style = OtsoTypography.uiCaption.copy(fontSize = 9.sp, letterSpacing = 0.3.sp),
                        color = colors.ink.copy(alpha = 0.50f),
                    )
                }
            }
        }
    }
}

@Composable
private fun DocumentInfoRow(
    label: String,
    value: String,
) {
    val colors = MaterialTheme.colorScheme.otsoColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = OtsoSpacing.globalMargin, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = OtsoTypography.uiCaption,
            color = colors.muted,
        )
        Text(
            text = value,
            style = OtsoTypography.uiCaption,
            color = colors.ink.copy(alpha = 0.55f),
        )
    }
}

private fun formatAdaptiveDate(timestamp: Long): String {
    if (timestamp <= 0L) return "—"
    val now = Calendar.getInstance()
    val then = Calendar.getInstance().apply { timeInMillis = timestamp }

    val sameDay = now.get(Calendar.YEAR) == then.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR)

    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    val isYesterday = yesterday.get(Calendar.YEAR) == then.get(Calendar.YEAR) &&
            yesterday.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR)

    val sameYear = now.get(Calendar.YEAR) == then.get(Calendar.YEAR)

    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    return when {
        sameDay -> "Today at ${timeFormat.format(Date(timestamp))}"
        isYesterday -> "Yesterday at ${timeFormat.format(Date(timestamp))}"
        sameYear -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
        else -> SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}

private fun formatNumber(count: Int): String {
    return NumberFormat.getIntegerInstance(Locale.getDefault()).format(count)
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
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 380f),
        label = "pill_offset"
    )
    val pillWidth by animateDpAsState(
        targetValue = if (selectedIndex == 0) 58.dp else 44.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 380f),
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
                    animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 350f),
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
    contentDescription: String?,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme.otsoColors
    Box(
        modifier = Modifier.size(40.dp).otsoClickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.material3.Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(20.dp),
            tint = colors.accent,
        )
    }
}
