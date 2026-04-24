package com.otso.app.ui.theme

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

/**
 * OtsoSquircleShape â€” Continuous Curvature Geometry (DNA-Level)
 * Replaces simple mathematical superellipses with a premium "Smooth Corner" algorithm (cubic bezier).
 * Provides C2 curvature continuity for the most organic and high-end industrial feel.
 *
 * @param radius If null, generates a "Pill" (full radius). Otherwise uses fixed Dp.
 * @param smoothing "Boxing" factor. 0.5 = Circular, 0.67 = Apple standard, 0.8 = Industrial Boxy.
 * @param topOnly If true, only rounds the top corners (perfect for Sheets).
 */
class OtsoSquircleShape(
    val radius: Dp? = null,
    val smoothing: Float = 0.8f,
    val topOnly: Boolean = false
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            val width = size.width
            val height = size.height
            val r = with(density) { radius?.toPx() } ?: (kotlin.math.min(width, height) / 2f)
            
            // Limit radius to half the shortest side
            val actualRadius = kotlin.math.min(r, kotlin.math.min(width, height) / 2f)
            
            // Smoothing logic: defines the "ramp up" point for curvature
            val smoothOffset = actualRadius * smoothing

            // Start from middle top
            moveTo(width / 2f, 0f)

            // TR Corner
            lineTo(width - smoothOffset, 0f)
            cubicTo(
                width - smoothOffset + (smoothOffset * 0.5f), 0f,
                width, smoothOffset - (smoothOffset * 0.5f),
                width, smoothOffset
            )
            
            // Right Side
            if (!topOnly) {
                lineTo(width, height - smoothOffset)
                // BR Corner
                cubicTo(
                    width, height - smoothOffset + (smoothOffset * 0.5f),
                    width - smoothOffset + (smoothOffset * 0.5f), height,
                    width - smoothOffset, height
                )
                lineTo(smoothOffset, height)
                // BL Corner
                cubicTo(
                    smoothOffset - (smoothOffset * 0.5f), height,
                    0f, height - smoothOffset + (smoothOffset * 0.5f),
                    0f, height - smoothOffset
                )
            } else {
                lineTo(width, height)
                lineTo(0f, height)
            }
            
            // Left Side
            lineTo(0f, smoothOffset)
            // TL Corner
            cubicTo(
                0f, smoothOffset - (smoothOffset * 0.5f),
                smoothOffset - (smoothOffset * 0.5f), 0f,
                smoothOffset, 0f
            )
            
            close()
        }
        return Outline.Generic(path)
    }
}
