package com.otso.app.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * OtsoIcons — The Phosphor Bridge.
 * Custom-drawn ImageVectors using Phosphor "Regular" path data.
 * Stroke weight: 16 (on 256 viewbox) -> ~2.0dp visual weight.
 */
object OtsoIcons {
    val X: ImageVector
        get() = ImageVector.Builder(
            name = "OtsoIcons.X",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 256f,
            viewportHeight = 256f
        ).path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 20f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Miter
        ) {
            moveTo(192f, 64f)
            lineTo(64f, 192f)
            moveTo(64f, 64f)
            lineTo(192f, 192f)
        }.build()

    val CaretUp: ImageVector
        get() = ImageVector.Builder(
            name = "OtsoIcons.CaretUp",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 256f,
            viewportHeight = 256f
        ).path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 20f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Miter
        ) {
            moveTo(64f, 160f)
            lineTo(128f, 96f)
            lineTo(192f, 160f)
        }.build()

    val CaretDown: ImageVector
        get() = ImageVector.Builder(
            name = "OtsoIcons.CaretDown",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 256f,
            viewportHeight = 256f
        ).path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 20f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Miter
        ) {
            moveTo(64f, 96f)
            lineTo(128f, 160f)
            lineTo(192f, 96f)
        }.build()

    val Plus: ImageVector
        get() = ImageVector.Builder(
            name = "OtsoIcons.Plus",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 256f,
            viewportHeight = 256f
        ).path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 20f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Miter
        ) {
            moveTo(56f, 128f)
            lineTo(200f, 128f)
            moveTo(128f, 56f)
            lineTo(128f, 200f)
        }.build()

    val ArrowLeft: ImageVector
        get() = ImageVector.Builder(
            name = "OtsoIcons.ArrowLeft",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 256f,
            viewportHeight = 256f
        ).path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 20f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Miter
        ) {
            moveTo(200f, 128f)
            lineTo(56f, 128f)
            moveTo(120f, 64f)
            lineTo(56f, 128f)
            lineTo(120f, 192f)
        }.build()

    val ArrowCounterClockwise: ImageVector
        get() = ImageVector.Builder(
            name = "OtsoIcons.ArrowCounterClockwise",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 256f,
            viewportHeight = 256f
        ).path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 20f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Miter
        ) {
            // Main Circular Path (3/4 circle)
            moveTo(200f, 128f)
            arcTo(72f, 72f, 0f, isMoreThanHalf = true, isPositiveArc = false, 128f, 56f)
            // Integrated Arrow Head (fused to the end of the arc)
            moveTo(96f, 88f)
            lineTo(128f, 56f)
            lineTo(96f, 24f)
        }.build()
        
    val Check: ImageVector
        get() = ImageVector.Builder(
            name = "OtsoIcons.Check",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 256f,
            viewportHeight = 256f
        ).path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 20f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Miter
        ) {
            moveTo(216f, 72f)
            lineTo(104f, 184f)
            lineTo(40f, 120f)
        }.build()

    val Minus: ImageVector
        get() = ImageVector.Builder(
            name = "OtsoIcons.Minus",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 256f,
            viewportHeight = 256f
        ).path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 20f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Miter
        ) {
            moveTo(56f, 128f)
            lineTo(200f, 128f)
        }.build()

    val Camera: ImageVector
        get() = ImageVector.Builder(
            name = "OtsoIcons.Camera",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 256f,
            viewportHeight = 256f
        ).path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 18f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(24f, 80f)
            lineTo(232f, 80f)
            lineTo(232f, 208f)
            lineTo(24f, 208f)
            close()
            moveTo(80f, 80f)
            lineTo(96f, 48f)
            lineTo(160f, 48f)
            lineTo(176f, 80f)
            moveTo(128f, 172f)
            arcTo(36f, 36f, 1f, isMoreThanHalf = true, isPositiveArc = false, 128f, 100f)
            arcTo(36f, 36f, 1f, isMoreThanHalf = true, isPositiveArc = false, 128f, 172f)
        }.build()

}
