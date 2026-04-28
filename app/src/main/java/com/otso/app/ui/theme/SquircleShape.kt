package com.otso.app.ui.theme

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sign
import kotlin.math.sin

/**
 * SquircleShape — Lamé curve based geometry.
 *
 * Implicit form (definition):
 *   |x/a|^n + |y/b|^n = 1
 *   where n = 4 for pure squircle, a = b = radius
 *
 * Parametric form (used for rendering):
 *   x(t) = a · sgn(cos t) · |cos t|^(2/n)
 *   y(t) = b · sgn(sin t) · |sin t|^(2/n)
 *   where t ∈ [0, 2π]
 *
 * Sampled at 360 uniform intervals of t for smooth curvature.
 * No third-party shape library. Math is explicit and self-contained.
 *
 * @param cornerRadius Dp — corner radius for the squircle (a = b).
 * @param n Double — Lamé exponent. n=4 is pure squircle. n=2 is circle.
 *                   n>4 approaches square. Default 4.
 */
class SquircleShape(
    private val cornerRadius: Dp,
    private val n: Double = 4.0,
) : Shape {

    private companion object {
        const val TOTAL_SAMPLES = 360
    }

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val radiusPx = with(density) { cornerRadius.toPx() }
        val path = buildSquirclePath(size, radiusPx, n)
        return Outline.Generic(path)
    }

    private fun buildSquirclePath(
        size: Size,
        radius: Float,
        exponent: Double,
    ): Path {
        val path = Path()
        if (!exponent.isFinite() || exponent <= 0.0) {
            path.addRect(androidx.compose.ui.geometry.Rect(0f, 0f, size.width, size.height))
            return path
        }

        // Clamp radius to not exceed half of the shorter dimension
        val maxRadius = minOf(size.width, size.height) / 2f
        val r = radius.coerceAtMost(maxRadius)

        // If radius is effectively zero or not enough, fall back to rectangle
        if (r < 0.5f) {
            path.moveTo(0f, 0f)
            path.lineTo(size.width, 0f)
            path.lineTo(size.width, size.height)
            path.lineTo(0f, size.height)
            path.close()
            return path
        }

        val leftCenterX = r
        val rightCenterX = size.width - r
        val topCenterY = r
        val bottomCenterY = size.height - r

        // Uniform t sampling across [0, 2π] using the explicit parametric Lamé form.
        val twoOverN = 2.0 / exponent
        val step = (2.0 * Math.PI) / TOTAL_SAMPLES

        for (i in 0..TOTAL_SAMPLES) {
            val t = i * step
            val cosT = cos(t)
            val sinT = sin(t)
            val localX = (r * sign(cosT) * cosT.absoluteValue.pow(twoOverN)).toFloat()
            val localY = (r * sign(sinT) * sinT.absoluteValue.pow(twoOverN)).toFloat()

            val quadrant = ((t / (Math.PI / 2.0)).toInt()) % 4
            val centerX = when (quadrant) {
                0, 3 -> rightCenterX
                else -> leftCenterX
            }
            val centerY = when (quadrant) {
                0, 1 -> bottomCenterY
                else -> topCenterY
            }

            val x = centerX + localX
            val y = centerY + localY
            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        path.close()
        return path
    }
}

/**
 * Convenience constructor matching RoundedCornerShape signature.
 */
fun SquircleShape(radius: Dp) = SquircleShape(cornerRadius = radius, n = 4.0)
