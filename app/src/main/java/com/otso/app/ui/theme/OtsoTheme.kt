/**
 * Otso Mobile — Design System Tokens
 * THE REVERT: Restoring original design with fixed theme reactivity.
 */

package com.otso.app.ui.theme

import androidx.compose.animation.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import java.io.File
import android.graphics.Typeface as AndroidTypeface
import com.otso.app.R
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.math.pow

// ─────────────────────────────────────────────
// Color Tokens
// ─────────────────────────────────────────────

object OtsoColors {
    val DarkBackground   = Color(0xFF000000)
    val DarkInk          = Color(0xFFD6D6D6)
    val DarkMuted        = Color(0xFFABABAB)
    val DarkEdge         = Color(0xFF333333)
    val DarkSurface      = Color(0xFF1C1C1C)

    // Original Light mode palette
    val LightBackground  = Color(0xFFF5F5F3)  // warm off-white
    val LightInk         = Color(0xFF1A1A1A)
    val LightMuted       = Color(0xFF4D4D4D)
    val LightEdge        = Color(0xFFE0E2E2)
    val LightSurface     = Color(0xFFF2F4F4)
    val LightShadow      = Color(0x1A2A3A5A) // soft navy-tinted gray

    val Accent           = Color(0xFF001AE2)
    val AccentMuted      = Color(0x2E001AE2) // 18% Opacity Blueprint Blue
    val SelectionBackground = Color(0x73001AE2)
    val Black            = Color(0xFF000000)
    val Transparent      = Color(0x00000000)
    val DarkShadow       = Color(0xFF000000) // deep midnight-black
}

@androidx.compose.runtime.Immutable
data class OtsoColorScheme(
    val background: Color,
    val ink: Color,
    val muted: Color,
    val edge: Color,
    val surface: Color,
    val accent: Color,
    val accentMuted: Color,
    val shadowColor: Color,
    val isDarkMode: Boolean, 
)

val LocalOtsoColors = compositionLocalOf {
    OtsoColorScheme(
        background = OtsoColors.LightBackground,
        ink = OtsoColors.LightInk,
        muted = OtsoColors.LightMuted,
        edge = OtsoColors.LightEdge,
        surface = OtsoColors.LightSurface,
        accent = OtsoColors.Accent,
        accentMuted = OtsoColors.AccentMuted,
        shadowColor = OtsoColors.LightShadow,
        isDarkMode = false,
    )
}

val androidx.compose.material3.ColorScheme.otsoColors: OtsoColorScheme
    @Composable get() = LocalOtsoColors.current

// ─────────────────────────────────────────────
// Typography
// ─────────────────────────────────────────────

val GeneralSans = FontFamily(
    Font(R.font.general_sans_regular,  FontWeight.Normal,   FontStyle.Normal),
    Font(R.font.general_sans_medium,   FontWeight.Medium,   FontStyle.Normal),
    Font(R.font.general_sans_semibold, FontWeight.SemiBold, FontStyle.Normal),
    Font(R.font.general_sans_bold,     FontWeight.Bold,     FontStyle.Normal),
    Font(R.font.general_sans_light,    FontWeight.Light,    FontStyle.Normal),
)

val JetBrainsMono = FontFamily(
    Font(R.font.jetbrains_mono_regular, FontWeight.Normal, FontStyle.Normal),
)

data class OtsoTypographyTokens(
    val editorBody: TextStyle,
    val editorLarge: TextStyle,
    val uiLabel: TextStyle,
    val uiLabelMedium: TextStyle,
    val uiCaption: TextStyle,
    val uiTitle: TextStyle,
    val uiTitleLarge: TextStyle,
    val uiBodyLarge: TextStyle,
    val uiDisplayLarge: TextStyle,
    val uiTechnical: TextStyle,
)

val OtsoTypography = OtsoTypographyTokens(
    editorBody = TextStyle(
        fontFamily = GeneralSans,
        fontWeight = FontWeight.Normal,
        fontSize   = 15.sp,
        lineHeight = (15 * 1.7f).sp,
    ),
    editorLarge = TextStyle(
        fontFamily = GeneralSans,
        fontWeight = FontWeight.Normal,
        fontSize   = 18.sp,
        lineHeight = (18 * 1.7f).sp,
    ),
    uiLabel = TextStyle(
        fontFamily = GeneralSans,
        fontWeight = FontWeight.Normal,
        fontSize   = 13.sp,
        lineHeight = 18.sp,
    ),
    uiLabelMedium = TextStyle(
        fontFamily = GeneralSans,
        fontWeight = FontWeight.Medium,
        fontSize   = 13.sp,
        lineHeight = 18.sp,
    ),
    uiCaption = TextStyle(
        fontFamily = GeneralSans,
        fontWeight = FontWeight.Normal,
        fontSize   = 11.sp,
        lineHeight = 16.sp,
        fontFeatureSettings = "tnum",
        letterSpacing = 0.15.sp,
    ),
    uiTitle = TextStyle(
        fontFamily = GeneralSans,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 16.sp,
        lineHeight = 22.sp,
    ),
    uiTitleLarge = TextStyle(
        fontFamily = GeneralSans,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 22.sp,
        lineHeight = 28.sp,
    ),
    uiBodyLarge = TextStyle(
        fontFamily = GeneralSans,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 18.sp,
        lineHeight = 24.sp,
    ),
    uiDisplayLarge = TextStyle(
        fontFamily = GeneralSans,
        fontWeight = FontWeight.Bold,
        fontSize   = 64.sp,
        lineHeight = 72.sp,
        letterSpacing = (-2).sp,
    ),
    uiTechnical = TextStyle(
        fontFamily = GeneralSans,
        fontWeight = FontWeight.Normal,
        fontSize   = 11.sp,
        lineHeight = 16.sp,
        fontFeatureSettings = "tnum", // Tabular Numbers: prevents layout shift when numbers change
    ),
)

val LocalOtsoTypography = compositionLocalOf { OtsoTypography }

val androidx.compose.material3.ColorScheme.otsoTypography: OtsoTypographyTokens
    @Composable get() = LocalOtsoTypography.current

// ─────────────────────────────────────────────
// Spacing
// ─────────────────────────────────────────────

data class OtsoSpacingTokens(
    val globalMargin: Dp,
    val editorInset: Dp,
    val tabPaddingH: Dp,
    val tabPaddingV: Dp,
    val chromeBandH: Dp,
    val commandBarH: Dp,
    val commandBarV: Dp,
    val keyboardToolbarH: Dp,
    val editorialMargin: Dp,
)

val OtsoSpacing = OtsoSpacingTokens(
    globalMargin      = 20.dp,
    editorInset       = 2.dp,
    tabPaddingH       = 20.dp,
    tabPaddingV       = 8.dp,
    chromeBandH       = 48.dp,
    commandBarH       = 10.dp,
    commandBarV       = 8.dp,
    keyboardToolbarH  = 56.dp,
    editorialMargin   = 24.dp,
)

val LocalOtsoSpacing = compositionLocalOf { OtsoSpacing }

// Essential Motion Tokens for system components (DO NOT REMOVE)
object OtsoMotion {
    const val durationQuickMs = 120
    const val durationPressMs = 120
    const val durationStandardMs = 240
    const val durationSheetMs = 400
    const val durationStaggerFadeMs = 140
    const val staggerOffsetPx = 14
    val easeOut = androidx.compose.animation.core.CubicBezierEasing(0.23f, 1f, 0.32f, 1f)
    val easeInOut = androidx.compose.animation.core.CubicBezierEasing(0.77f, 0f, 0.175f, 1f)
    val easeDrawer = androidx.compose.animation.core.CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
}

val androidx.compose.material3.ColorScheme.otsoSpacing: OtsoSpacingTokens
    @Composable get() = LocalOtsoSpacing.current

// ─────────────────────────────────────────────
// Theme Composable
// ─────────────────────────────────────────────

@Composable
fun OtsoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // appliedDarkTheme is intentionally DELAYED relative to darkTheme.
    // The overlay fires instantly (draw-phase only), then after ~50ms the color
    // swap happens while the screen is covered — hiding the one-time mass
    // recomposition of LocalOtsoColors consumers behind the opaque flash.
    var appliedDarkTheme by remember { mutableStateOf(darkTheme) }

    val finalOtsoScheme = remember(appliedDarkTheme) {
        OtsoColorScheme(
            background = if (appliedDarkTheme) OtsoColors.DarkBackground else OtsoColors.LightBackground,
            ink        = if (appliedDarkTheme) OtsoColors.DarkInk        else OtsoColors.LightInk,
            muted      = if (appliedDarkTheme) OtsoColors.DarkMuted      else OtsoColors.LightMuted,
            edge       = if (appliedDarkTheme) OtsoColors.DarkEdge       else OtsoColors.LightEdge,
            surface    = if (appliedDarkTheme) OtsoColors.DarkSurface    else OtsoColors.LightSurface,
            accent     = OtsoColors.Accent,
            accentMuted= OtsoColors.AccentMuted,
            shadowColor= if (appliedDarkTheme) OtsoColors.DarkShadow     else OtsoColors.LightShadow,
            isDarkMode = appliedDarkTheme,
        )
    }
    val colorScheme = remember(appliedDarkTheme) {
        if (appliedDarkTheme) {
            darkColorScheme(
                background  = finalOtsoScheme.background,
                surface     = finalOtsoScheme.surface,
                primary     = finalOtsoScheme.accent,
                onBackground= finalOtsoScheme.ink,
                onSurface   = finalOtsoScheme.ink,
                outline     = finalOtsoScheme.edge,
            )
        } else {
            lightColorScheme(
                background  = finalOtsoScheme.background,
                surface     = finalOtsoScheme.surface,
                primary     = finalOtsoScheme.accent,
                onBackground= finalOtsoScheme.ink,
                onSurface   = finalOtsoScheme.ink,
                outline     = finalOtsoScheme.edge,
            )
        }
    }

    // Overlay: single hardware layer, reads alpha only in draw phase.
    // OtsoMotion.easeOut (expo-out curve 0.23,1,0.32,1) — same curve used
    // for all system-level transitions in this app.
    val overlayColor = remember { mutableStateOf(Color.Transparent) }
    val overlayAlpha = remember { Animatable(0f) }
    var isFirstRun by remember { mutableStateOf(true) }

    LaunchedEffect(darkTheme) {
        if (isFirstRun) {
            isFirstRun = false
            appliedDarkTheme = darkTheme
            return@LaunchedEffect
        }

        // Overlay = DESTINATION theme color: new theme "washes over" the screen.
        overlayColor.value = if (darkTheme) OtsoColors.DarkBackground else OtsoColors.LightBackground

        // Cover screen before the heavy recomposition fires.
        overlayAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 90, easing = OtsoMotion.easeOut),
        )

        // Trigger mass recomposition while fully occluded.
        appliedDarkTheme = darkTheme

        // Wait for recomposition to complete — 4 frames (~67ms) gives ART
        // enough headroom even in debug/verify mode (200-500ms recompose).
        // The overlay hides all of this; no frame drop is visible.
        repeat(4) { withFrameNanos { } }

        // Reveal: simple fade-out only — no content layer transform.
        // Removing contentScale eliminates compound CPU pressure during fade.
        overlayAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 220, easing = OtsoMotion.easeOut),
        )
    }

    CompositionLocalProvider(
        LocalOtsoColors    provides finalOtsoScheme,
        LocalOtsoTypography provides OtsoTypography,
        LocalOtsoSpacing   provides OtsoSpacing,
    ) {
        MaterialTheme(colorScheme = colorScheme) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxSize()) {
                    content()
                }
                // Overlay: alpha read in draw phase only — zero recompositions during fade.
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .graphicsLayer { alpha = overlayAlpha.value }
                        .background(overlayColor.value),
                )
            }
        }
    }
}

/**
 * DNA Utility: Staggered Entry Animation
 * Provides a cascading slide-up + fade entrance for list/menu items.
 *
 * @param index The position in the sequence (used for delay calculation).
 * @param delayPerRow The delay in ms between each staggered row.
 */
@Composable
fun StaggeredItem(
    index: Int,
    delayPerRow: Int = 28,
    content: @Composable () -> Unit
) {
    val visible = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * delayPerRow.toLong())
        visible.value = true
    }

    androidx.compose.animation.AnimatedVisibility(
        visible = visible.value,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = OtsoMotion.durationStaggerFadeMs,
                easing = OtsoMotion.easeOut,
            ),
        ) +
                slideInVertically(
                    initialOffsetY = { OtsoMotion.staggerOffsetPx },
                    animationSpec = spring<IntOffset>(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow,
                    ),
                ),
        exit = fadeOut(
            animationSpec = tween(
                durationMillis = OtsoMotion.durationQuickMs,
                easing = OtsoMotion.easeInOut,
            ),
        )
    ) {
        content()
    }
}

/**
 * Industrial-grade Dynamic FontFamily Bridge.
 * Loads a .ttf/.otf from the provided path with silent failover.
 */
@Composable
fun rememberDynamicFontFamily(
    path: String?,
    foundryFamily: FontFamily? = null,
): FontFamily {
    return remember(path, foundryFamily) {
        if (foundryFamily != null) return@remember foundryFamily
        if (path == null) return@remember GeneralSans
        
        try {
            val file = File(path)
            if (!file.exists()) return@remember GeneralSans
            
            val androidTypeface = AndroidTypeface.createFromFile(file)
            FontFamily(androidTypeface)
        } catch (e: Exception) {
          // Exception shielding: fail silently to default editor family.
            GeneralSans
        }
    }
}

/**
 * DNA Utility: Universal Interaction Purge
 * Replaces Material Ripples with a tactile mechanical feedback:
 * 96% scale-down (configurable) + 8% background contrast shift.
 *
 * @param interactionSource Optional external source for coordinated animations.
 * @param scaleTarget The target scale factor on press.
 */
fun Modifier.otsoClickable(
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    scaleTarget: Float = 0.97f,
    onClick: () -> Unit
): Modifier = composed {
    val internalInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    val isPressed by internalInteractionSource.collectIsPressedAsState()

    // Animatable: values read inside graphicsLayer/drawWithContent = draw-phase only.
    // animateFloatAsState with `by` delegate would recompose the entire containing
    // composable on every animation frame — Animatable avoids that entirely.
    val scale = remember { Animatable(1f) }
    val pressAlpha = remember { Animatable(0f) }
    val colors = MaterialTheme.colorScheme.otsoColors

    LaunchedEffect(isPressed) {
        if (isPressed) {
            // Press: crisp, fast — no overshoot. Emil: micro-interaction ~100ms.
            launch { scale.animateTo(scaleTarget, spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 600f)) }
            pressAlpha.animateTo(0.08f, spring(stiffness = Spring.StiffnessMedium))
        } else {
            // Release: slight bounce-back — physical, satisfying.
            launch { scale.animateTo(1f, spring(dampingRatio = 0.6f, stiffness = 500f)) }
            pressAlpha.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
        }
    }

    this
        .graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        }
        .drawWithContent {
            drawContent()
            if (pressAlpha.value > 0f) {
                drawRect(
                    color = colors.ink.copy(alpha = pressAlpha.value),
                    size = size
                )
            }
        }
        .clickable(
            interactionSource = internalInteractionSource,
            indication = null, // THE PURGE
            enabled = enabled,
            onClick = onClick
        )
}

/**
 * DNA Utility: Technical Grain Materiality
 * Adds a subtle noise overlay to mimic technical paper/frosted metal textures.
 */
fun Modifier.technicalGrain(alpha: Float = 0.03f): Modifier = composed {
    val noiseBitmap = remember {
        val size = 64
        val bitmap = ImageBitmap(size, size, ImageBitmapConfig.Argb8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            color = Color.Black
            this.alpha = alpha
        }
        
        for (x in 0 until size) {
            for (y in 0 until size) {
                if (Random.nextFloat() > 0.5f) {
                    canvas.drawCircle(Offset(x.toFloat(), y.toFloat()), 0.5f, paint)
                }
            }
        }
        bitmap
    }

    drawWithContent {
        drawContent()
        val paint = Paint()
        drawIntoCanvas { canvas ->
            for (x in 0..size.width.toInt() step 64) {
                for (y in 0..size.height.toInt() step 64) {
                    canvas.drawImage(noiseBitmap, Offset(x.toFloat(), y.toFloat()), paint)
                }
            }
        }
    }
}
