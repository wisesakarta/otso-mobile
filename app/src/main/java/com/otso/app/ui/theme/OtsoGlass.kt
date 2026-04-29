package com.otso.app.ui.theme

import android.graphics.BlurMaskFilter
import android.graphics.Matrix as AndroidMatrix
import android.graphics.Paint as AndroidPaint
import android.graphics.Path as AndroidPath
import android.graphics.RectF as AndroidRectF
import android.graphics.RenderEffect
import android.graphics.RenderNode
import android.graphics.Shader
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private const val GLASS_SHINE_DURATION_MS = 6200
private const val GLASS_SHINE_START = -1f
private const val GLASS_SHINE_END = 2f
private const val GLASS_SHINE_VECTOR = 0.8f
private const val GLASS_TOP_SHEEN_END_FACTOR = 0.40f
private const val GLASS_BOTTOM_SHADE_START_FACTOR = 0.56f
private const val GLASS_EDGE_GLOW_RADIUS_FACTOR = 0.55f
private const val GLASS_TOP_EDGE_COVERAGE_FACTOR = 0.58f
private const val GLASS_LEFT_EDGE_COVERAGE_FACTOR = 0.58f
private const val GLASS_BOTTOM_EDGE_START_FACTOR = 0.42f
private const val GLASS_RIGHT_EDGE_START_FACTOR = 0.42f
private const val GLASS_INNER_EDGE_WIDTH_PX = 1f
private const val GLASS_STRUCTURAL_EDGE_WIDTH_PX = 1f
private const val GLASS_STRUCTURAL_EDGE_INSET_PX = 1f

private val GLASS_BACKDROP_BLUR_RADIUS: Dp = 22.dp
private val GLASS_ELEVATION_DARK: Dp = 14.dp
private val GLASS_ELEVATION_LIGHT: Dp = 22.dp
private val SOLID_AMBIENT_ELEVATION: Dp = 4.dp
private const val SOLID_LIGHT_AMBIENT_ALPHA = 0.12f
private const val SOLID_LIGHT_SPOT_ALPHA = 0.28f
private const val SOLID_DARK_AMBIENT_ALPHA = 0.20f
private const val SOLID_DARK_SPOT_ALPHA = 0.40f

fun Modifier.stackedShadow(shape: Shape, shadowColor: Color = Color.Black): Modifier = composed {
    val layoutDirection = LocalLayoutDirection.current
    drawBehind {
        if (size.width <= 0f || size.height <= 0f) return@drawBehind
        val outline = shape.createOutline(size, layoutDirection, this)

        fun buildPath(spreadPx: Float): AndroidPath = when (outline) {
            is Outline.Rectangle -> {
                val r = outline.rect
                AndroidPath().apply {
                    addRect(
                        r.left - spreadPx, r.top - spreadPx,
                        r.right + spreadPx, r.bottom + spreadPx,
                        AndroidPath.Direction.CW,
                    )
                }
            }
            is Outline.Rounded -> {
                val rr = outline.roundRect
                AndroidPath().apply {
                    addRoundRect(
                        AndroidRectF(
                            rr.left - spreadPx, rr.top - spreadPx,
                            rr.right + spreadPx, rr.bottom + spreadPx,
                        ),
                        floatArrayOf(
                            (rr.topLeftCornerRadius.x + spreadPx).coerceAtLeast(0f),
                            (rr.topLeftCornerRadius.y + spreadPx).coerceAtLeast(0f),
                            (rr.topRightCornerRadius.x + spreadPx).coerceAtLeast(0f),
                            (rr.topRightCornerRadius.y + spreadPx).coerceAtLeast(0f),
                            (rr.bottomRightCornerRadius.x + spreadPx).coerceAtLeast(0f),
                            (rr.bottomRightCornerRadius.y + spreadPx).coerceAtLeast(0f),
                            (rr.bottomLeftCornerRadius.x + spreadPx).coerceAtLeast(0f),
                            (rr.bottomLeftCornerRadius.y + spreadPx).coerceAtLeast(0f),
                        ),
                        AndroidPath.Direction.CW,
                    )
                }
            }
            is Outline.Generic -> AndroidPath(outline.path.asAndroidPath()).apply {
                if (spreadPx != 0f) {
                    val newW = (size.width + spreadPx * 2f).coerceAtLeast(0.01f)
                    val newH = (size.height + spreadPx * 2f).coerceAtLeast(0.01f)
                    transform(AndroidMatrix().apply {
                        setScale(newW / size.width, newH / size.height, size.width / 2f, size.height / 2f)
                    })
                }
            }
        }

        drawIntoCanvas { canvas ->
            val native = canvas.nativeCanvas
            val s1 = 1.dp.toPx()
            val b2 = 2.dp.toPx(); val o2 = 1.dp.toPx()
            val b3 = 4.dp.toPx(); val o3 = 2.dp.toPx()

            // Layer 1: 1dp spread, no blur — sharp outline
            native.drawPath(
                buildPath(s1),
                AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
                    color = shadowColor.copy(alpha = 0.06f).toArgb()
                },
            )
            // Layer 2: 1dp y-offset, 2dp blur, -1dp contract
            native.save()
            native.translate(0f, o2)
            native.drawPath(
                buildPath(-s1),
                AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
                    color = shadowColor.copy(alpha = 0.04f).toArgb()
                    maskFilter = BlurMaskFilter(b2, BlurMaskFilter.Blur.NORMAL)
                },
            )
            native.restore()
            // Layer 3: 2dp y-offset, 4dp blur, 0 spread
            native.save()
            native.translate(0f, o3)
            native.drawPath(
                buildPath(0f),
                AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
                    color = shadowColor.copy(alpha = 0.025f).toArgb()
                    maskFilter = BlurMaskFilter(b3, BlurMaskFilter.Blur.NORMAL)
                },
            )
            native.restore()
        }
    }
}

fun Modifier.otsoFloatingSolid(
    shape: Shape,
    colors: OtsoColorScheme,
    elevation: Dp = 12.dp,
    drawBorder: Boolean = true,
): Modifier = composed {
    val density = LocalDensity.current
    val ambientElevationPx = with(density) { SOLID_AMBIENT_ELEVATION.toPx() }
    val spotElevationPx = with(density) { elevation.toPx() }

    val ambientShadow: Color
    val spotShadow: Color
    val tonalSurface: Color

    if (colors.isDarkMode) {
        // Dark mode: validated — keep existing shadow and depth logic.
        ambientShadow = colors.shadowColor.copy(alpha = SOLID_DARK_AMBIENT_ALPHA)
        spotShadow    = colors.shadowColor.copy(alpha = SOLID_DARK_SPOT_ALPHA)
        tonalSurface  = lerp(colors.surface, colors.edge, 0.05f)
    } else {
        // Light mode: navy-tinted atmospheric shadow for visual harmony.
        // Color(0x182A3A5A) = navy-gray @ ~9% — soft, directional, non-black.
        ambientShadow = Color(0x182A3A5A)
        spotShadow    = Color(0x182A3A5A)
        // 1% blue-violet tonal shift: composites surface over the tint target.
        tonalSurface  = colors.surface.compositeOver(Color(0xFFF5F7FF))
    }

    this
        .graphicsLayer {
            this.shape = shape
            clip = false
            shadowElevation = ambientElevationPx
            ambientShadowColor = ambientShadow
            spotShadowColor = Color.Transparent
        }
        .graphicsLayer {
            this.shape = shape
            clip = false
            shadowElevation = spotElevationPx
            ambientShadowColor = Color.Transparent
            spotShadowColor = spotShadow
        }
        .background(
            color = tonalSurface,
            shape = shape,
        )
        .then(
            if (!colors.isDarkMode && drawBorder) Modifier.border(0.5.dp, Color(0x08000000), shape)
            else Modifier
        )
}

/**
 * Premium floating glass treatment for compact chrome surfaces.
 *
 * - API 31+: backdrop layer uses RenderEffect blur.
 * - Older API fallback: translucent frosted tint without blur.
 * - 1px inner edge treatment adds tactile thickness.
 */
fun Modifier.otsoFloatingGlass(
    shape: Shape,
    colors: OtsoColorScheme,
): Modifier = composed {
    val layoutDirection = LocalLayoutDirection.current
    val density = LocalDensity.current
    val blurRadiusPx = with(density) { GLASS_BACKDROP_BLUR_RADIUS.toPx() }
    val palette = rememberGlassPalette(isDarkMode = colors.isDarkMode)

    val infiniteTransition = rememberInfiniteTransition(label = "glass_shine")
    val shineOffset by infiniteTransition.animateFloat(
        initialValue = GLASS_SHINE_START,
        targetValue = GLASS_SHINE_END,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = GLASS_SHINE_DURATION_MS, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "glass_shine_offset",
    )

    this
        .shadow(
            elevation = if (colors.isDarkMode) GLASS_ELEVATION_DARK else GLASS_ELEVATION_LIGHT,
            shape = shape,
            clip = false,
            ambientColor = palette.shadowAmbient,
            spotColor = palette.shadowSpot,
        )
        .clip(shape)
        .drawWithCache {
            val outline = shape.createOutline(size = size, layoutDirection = layoutDirection, density = this)
            val topSheen = Brush.verticalGradient(
                colors = listOf(palette.topSheen, Color.Transparent),
                startY = 0f,
                endY = size.height * GLASS_TOP_SHEEN_END_FACTOR,
            )
            val animatedShine = Brush.linearGradient(
                colorStops = arrayOf(
                    0.0f to Color.Transparent,
                    0.5f to palette.animatedSheen,
                    1.0f to Color.Transparent,
                ),
                start = Offset(x = size.width * shineOffset, y = 0f),
                end = Offset(x = size.width * (shineOffset + GLASS_SHINE_VECTOR), y = size.height),
            )
            val bottomShade = Brush.verticalGradient(
                colors = listOf(Color.Transparent, palette.bottomShade),
                startY = size.height * GLASS_BOTTOM_SHADE_START_FACTOR,
                endY = size.height,
            )
            val edgeGlow = Brush.radialGradient(
                colors = listOf(palette.edgeGlow, Color.Transparent),
                center = Offset(x = size.width / 2f, y = 0f),
                radius = size.width * GLASS_EDGE_GLOW_RADIUS_FACTOR,
            )
            val innerEdgeStroke = Stroke(width = GLASS_INNER_EDGE_WIDTH_PX)
            val structuralEdgeStroke = Stroke(width = GLASS_STRUCTURAL_EDGE_WIDTH_PX)

            onDrawWithContent {
                drawBackdropLayer(
                    outline = outline,
                    tintColor = palette.backdropTintColor,
                    blurRadiusPx = blurRadiusPx,
                )
                drawOutlineWithBrush(outline = outline, brush = palette.fillBrush)
                drawOutlineWithBrush(outline = outline, brush = topSheen)
                drawOutlineWithBrush(outline = outline, brush = animatedShine)
                drawOutlineWithBrush(outline = outline, brush = bottomShade)
                drawOutlineWithBrush(outline = outline, brush = edgeGlow)
                drawContent()
                drawInnerEdge(
                    outline = outline,
                    style = innerEdgeStroke,
                    topLeftColor = palette.innerEdgeLight,
                    bottomRightColor = palette.innerEdgeDark,
                )
                drawInsetStructuralEdge(
                    outline = outline,
                    style = structuralEdgeStroke,
                    color = palette.outerStructuralEdge,
                    insetPx = GLASS_STRUCTURAL_EDGE_INSET_PX,
                )
            }
        }
}

private data class GlassPalette(
    val fillBrush: Brush,
    val backdropTintColor: Color,
    val outerStructuralEdge: Color,
    val innerEdgeLight: Color,
    val innerEdgeDark: Color,
    val topSheen: Color,
    val animatedSheen: Color,
    val bottomShade: Color,
    val edgeGlow: Color,
    val shadowAmbient: Color,
    val shadowSpot: Color,
)

private fun rememberGlassPalette(isDarkMode: Boolean): GlassPalette {
    return if (isDarkMode) {
        GlassPalette(
            fillBrush = Brush.verticalGradient(
                colorStops = arrayOf(
                    0.0f to Color(0xFF2A303A).copy(alpha = 0.40f),
                    0.52f to Color(0xFF1A202A).copy(alpha = 0.34f),
                    1.0f to Color(0xFF10151D).copy(alpha = 0.28f),
                ),
            ),
            backdropTintColor = Color(0xFF1A2330).copy(alpha = 0.22f),
            outerStructuralEdge = Color.White.copy(alpha = 0.10f),
            innerEdgeLight = Color.White.copy(alpha = 0.22f),
            innerEdgeDark = Color.Black.copy(alpha = 0.34f),
            topSheen = Color.White.copy(alpha = 0.16f),
            animatedSheen = Color.White.copy(alpha = 0.08f),
            bottomShade = Color.Black.copy(alpha = 0.16f),
            edgeGlow = Color.White.copy(alpha = 0.08f),
            shadowAmbient = Color.Black.copy(alpha = 0.58f),
            shadowSpot = Color.Black.copy(alpha = 0.42f),
        )
    } else {
        GlassPalette(
            fillBrush = Brush.verticalGradient(
                colorStops = arrayOf(
                    0.0f to Color.White.copy(alpha = 0.50f),
                    0.52f to Color(0xFFF7FAFF).copy(alpha = 0.46f),
                    1.0f to Color(0xFFEAF0F8).copy(alpha = 0.40f),
                ),
            ),
            backdropTintColor = Color(0xFFE7EEF8).copy(alpha = 0.36f),
            outerStructuralEdge = Color.Black.copy(alpha = 0.10f),
            innerEdgeLight = Color.White.copy(alpha = 0.72f),
            innerEdgeDark = Color.Black.copy(alpha = 0.14f),
            topSheen = Color.White.copy(alpha = 0.45f),
            animatedSheen = Color.White.copy(alpha = 0.12f),
            bottomShade = Color.Black.copy(alpha = 0.06f),
            edgeGlow = Color.White.copy(alpha = 0.16f),
            shadowAmbient = Color.Black.copy(alpha = 0.14f),
            shadowSpot = Color.Black.copy(alpha = 0.09f),
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawInsetStructuralEdge(
    outline: Outline,
    style: DrawStyle,
    color: Color,
    insetPx: Float,
) {
    clipRect(
        left = insetPx,
        top = insetPx,
        right = size.width - insetPx,
        bottom = size.height - insetPx,
    ) {
        drawOutlineWithColor(
            outline = outline,
            color = color,
            style = style,
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBackdropLayer(
    outline: Outline,
    tintColor: Color,
    blurRadiusPx: Float,
) {
    if (size.width <= 0f || size.height <= 0f) return

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val widthPx = size.width.toInt().coerceAtLeast(1)
        val heightPx = size.height.toInt().coerceAtLeast(1)
        val renderNode = RenderNode("glass_backdrop_blur").apply {
            setPosition(0, 0, widthPx, heightPx)
            setRenderEffect(
                RenderEffect.createBlurEffect(
                    blurRadiusPx,
                    blurRadiusPx,
                    Shader.TileMode.DECAL,
                ),
            )
        }
        val recordingCanvas = renderNode.beginRecording()
        val paint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            color = tintColor.toArgb()
        }

        when (outline) {
            is Outline.Rectangle -> {
                recordingCanvas.drawRect(0f, 0f, size.width, size.height, paint)
            }
            is Outline.Rounded -> {
                val rr = outline.roundRect
                val roundedPath = AndroidPath().apply {
                    addRoundRect(
                        rr.left,
                        rr.top,
                        rr.right,
                        rr.bottom,
                        floatArrayOf(
                            rr.topLeftCornerRadius.x,
                            rr.topLeftCornerRadius.y,
                            rr.topRightCornerRadius.x,
                            rr.topRightCornerRadius.y,
                            rr.bottomRightCornerRadius.x,
                            rr.bottomRightCornerRadius.y,
                            rr.bottomLeftCornerRadius.x,
                            rr.bottomLeftCornerRadius.y,
                        ),
                        AndroidPath.Direction.CW,
                    )
                }
                recordingCanvas.drawPath(roundedPath, paint)
            }
            is Outline.Generic -> {
                recordingCanvas.drawPath(outline.path.asAndroidPath(), paint)
            }
        }
        renderNode.endRecording()
        drawIntoCanvas { canvas ->
            canvas.nativeCanvas.drawRenderNode(renderNode)
        }
    } else {
        drawOutlineWithColor(outline = outline, color = tintColor)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawInnerEdge(
    outline: Outline,
    style: DrawStyle,
    topLeftColor: Color,
    bottomRightColor: Color,
) {
    clipRect(
        left = 0f,
        top = 0f,
        right = size.width,
        bottom = size.height * GLASS_TOP_EDGE_COVERAGE_FACTOR,
    ) {
        drawOutlineWithColor(outline = outline, color = topLeftColor, style = style)
    }
    clipRect(
        left = 0f,
        top = 0f,
        right = size.width * GLASS_LEFT_EDGE_COVERAGE_FACTOR,
        bottom = size.height,
    ) {
        drawOutlineWithColor(outline = outline, color = topLeftColor, style = style)
    }
    clipRect(
        left = 0f,
        top = size.height * GLASS_BOTTOM_EDGE_START_FACTOR,
        right = size.width,
        bottom = size.height,
    ) {
        drawOutlineWithColor(outline = outline, color = bottomRightColor, style = style)
    }
    clipRect(
        left = size.width * GLASS_RIGHT_EDGE_START_FACTOR,
        top = 0f,
        right = size.width,
        bottom = size.height,
    ) {
        drawOutlineWithColor(outline = outline, color = bottomRightColor, style = style)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawOutlineWithBrush(
    outline: Outline,
    brush: Brush,
    style: DrawStyle = Fill,
) {
    when (outline) {
        is Outline.Rectangle -> drawRect(brush = brush, style = style)
        is Outline.Rounded -> {
            val roundedPath = Path().apply { addRoundRect(outline.roundRect) }
            drawPath(path = roundedPath, brush = brush, style = style)
        }
        is Outline.Generic -> drawPath(path = outline.path, brush = brush, style = style)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawOutlineWithColor(
    outline: Outline,
    color: Color,
    style: DrawStyle = Fill,
) {
    when (outline) {
        is Outline.Rectangle -> drawRect(color = color, style = style)
        is Outline.Rounded -> {
            val roundedPath = Path().apply { addRoundRect(outline.roundRect) }
            drawPath(path = roundedPath, color = color, style = style)
        }
        is Outline.Generic -> drawPath(path = outline.path, color = color, style = style)
    }
}
