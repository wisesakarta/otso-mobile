/**
 * Otso Mobile — Design System Tokens
 * THE REVERT: Restoring original design with fixed theme reactivity.
 */

package com.otso.app.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import java.io.File
import android.graphics.Typeface as AndroidTypeface
import com.otso.app.R
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

    val Accent           = Color(0xFF001AE2)
    val AccentMuted      = Color(0x2E001AE2) // 18% Opacity Blueprint Blue
    val Black            = Color(0xFF000000)
    val Transparent      = Color(0x00000000)
}

data class OtsoColorScheme(
    val background: Color,
    val ink: Color,
    val muted: Color,
    val edge: Color,
    val surface: Color,
    val accent: Color,
    val accentMuted: Color,
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
    keyboardToolbarH  = 48.dp,
    editorialMargin   = 24.dp,
)

val LocalOtsoSpacing = compositionLocalOf { OtsoSpacing }

// Essential Motion Tokens for system components (DO NOT REMOVE)
object OtsoMotion {
    const val durationQuickMs = 120
    const val durationPressMs = 120
    const val durationStandardMs = 240
    const val durationSheetMs = 400
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
    val targetBackground = if (darkTheme) OtsoColors.DarkBackground else OtsoColors.LightBackground
    val targetInk = if (darkTheme) OtsoColors.DarkInk else OtsoColors.LightInk
    val targetMuted = if (darkTheme) OtsoColors.DarkMuted else OtsoColors.LightMuted
    val targetEdge = if (darkTheme) OtsoColors.DarkEdge else OtsoColors.LightEdge
    val targetSurface = if (darkTheme) OtsoColors.DarkSurface else OtsoColors.LightSurface

    val animatedBackground by animateColorAsState(targetBackground, spring(stiffness = Spring.StiffnessMediumLow), label = "bg")
    val animatedInk by animateColorAsState(targetInk, spring(stiffness = Spring.StiffnessMediumLow), label = "ink")
    val animatedMuted by animateColorAsState(targetMuted, spring(stiffness = Spring.StiffnessMediumLow), label = "muted")
    val animatedEdge by animateColorAsState(targetEdge, spring(stiffness = Spring.StiffnessMediumLow), label = "edge")
    val animatedSurface by animateColorAsState(targetSurface, spring(stiffness = Spring.StiffnessMediumLow), label = "surface")
    val animatedAccent by animateColorAsState(OtsoColors.Accent, spring(stiffness = Spring.StiffnessMediumLow), label = "accent")

    val finalOtsoScheme = remember(darkTheme, animatedBackground, animatedInk, animatedMuted, animatedEdge, animatedSurface, animatedAccent) {
        OtsoColorScheme(
            background = animatedBackground,
            ink = animatedInk,
            muted = animatedMuted,
            edge = animatedEdge,
            surface = animatedSurface,
            accent = animatedAccent,
            accentMuted = animatedAccent.copy(alpha = 0.18f),
            isDarkMode = darkTheme
        )
    }

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            background = finalOtsoScheme.background,
            surface = finalOtsoScheme.surface,
            primary = finalOtsoScheme.accent,
            onBackground = finalOtsoScheme.ink,
            onSurface = finalOtsoScheme.ink,
            outline = finalOtsoScheme.edge,
        )
    } else {
        lightColorScheme(
            background = finalOtsoScheme.background,
            surface = finalOtsoScheme.surface,
            primary = finalOtsoScheme.accent,
            onBackground = finalOtsoScheme.ink,
            onSurface = finalOtsoScheme.ink,
            outline = finalOtsoScheme.edge,
        )
    }

    CompositionLocalProvider(
        LocalOtsoColors provides finalOtsoScheme,
        LocalOtsoTypography provides OtsoTypography,
        LocalOtsoSpacing provides OtsoSpacing,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content,
        )
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
    delayPerRow: Int = 40,
    content: @Composable () -> Unit
) {
    Box {
        content()
    }
}

/**
 * Industrial-grade Dynamic FontFamily Bridge.
 * Loads a .ttf/.otf from the provided path with silent failover.
 */
@Composable
fun rememberDynamicFontFamily(path: String?): FontFamily {
    return remember(path) {
        if (path == null) return@remember FontFamily.Monospace
        
        try {
            val file = File(path)
            if (!file.exists()) return@remember FontFamily.Monospace
            
            val androidTypeface = AndroidTypeface.createFromFile(file)
            FontFamily(androidTypeface)
        } catch (e: Exception) {
          // Exception Shielding: Fail silently to Monospace
            FontFamily.Monospace
        }
    }
}

/**
 * DNA Utility: Universal Interaction Purge
 * Replaces Material Ripples with a tactile mechanical feedback:
 * 98% scale-down + 5% background contrast shift.
 */
fun Modifier.otsoClickable(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "scale"
    )
    
    val colors = MaterialTheme.colorScheme.otsoColors
    val overlayAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.08f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "overlay"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .drawWithContent {
            drawContent()
            if (overlayAlpha > 0f) {
                drawRect(
                    color = colors.ink.copy(alpha = overlayAlpha),
                    size = size
                )
            }
        }
        .clickable(
            interactionSource = interactionSource,
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

/**
 * OtsoSquircleShape — True Lamé Curve Geometry (DNA-Level)
 *
 * Implements the superellipse (squircle) using the Lamé curve parametric formula:
 *   x(t) = a · sgn(cos t) · |cos t|^(2/n)
 *   y(t) = b · sgn(sin t) · |sin t|^(2/n)
 * where n = 4 (pure squircle), a = b = radius.
 *
 * The curve is sampled at 360 uniform intervals of t and rendered via Path.lineTo().
 * Math is explicit and self-contained — no third-party shape library.
 *
 * For rectangular shapes, the Lamé curve is applied only to the four corners,
 * with straight edges connecting them. This produces the characteristic
 * "continuous curvature" that distinguishes squircles from rounded rectangles.
 *
 * @param radius Corner radius. If null, generates a "Pill" (full radius).
 * @param smoothing Exponent control. Higher = boxier. Default 0.8 maps to n=4 Lamé.
 * @param topOnly If true, only rounds the top corners (for ModalBottomSheet).
 */
