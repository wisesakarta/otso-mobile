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
    private const val STROKE_WEIGHT = 18f

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
            strokeLineWidth = STROKE_WEIGHT,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
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
            strokeLineWidth = STROKE_WEIGHT,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
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
            strokeLineWidth = STROKE_WEIGHT,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(64f, 96f)
            lineTo(128f, 160f)
            lineTo(192f, 96f)
        }.build()

    val CaretRight: ImageVector
        get() = ImageVector.Builder(
            name = "OtsoIcons.CaretRight",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 256f,
            viewportHeight = 256f
        ).path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = STROKE_WEIGHT,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(96f, 192f)
            lineTo(160f, 128f)
            lineTo(96f, 64f)
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
            strokeLineWidth = STROKE_WEIGHT,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
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
            strokeLineWidth = STROKE_WEIGHT,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
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
            strokeLineWidth = STROKE_WEIGHT,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(79.8f, 99.7f)
            lineTo(31.8f, 99.7f)
            lineTo(31.8f, 51.7f)
            
            moveTo(65.8f, 190.2f)
            arcTo(88f, 88f, 0f, isMoreThanHalf = true, isPositiveArc = false, 65.8f, 65.8f)
            lineTo(31.8f, 99.7f)
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
            strokeLineWidth = STROKE_WEIGHT,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
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
            strokeLineWidth = STROKE_WEIGHT,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
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
            strokeLineWidth = STROKE_WEIGHT,
            strokeLineCap = StrokeCap.Round,
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

    val Highlighter: ImageVector
        get() = ImageVector.Builder(
            name = "OtsoIcons.Highlighter",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 256f,
            viewportHeight = 256f
        ).path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = STROKE_WEIGHT,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(200f, 40f)
            lineTo(224f, 64f)
            lineTo(96f, 192f)
            lineTo(40f, 192f)
            lineTo(40f, 136f)
            close()
            moveTo(72f, 136f)
            lineTo(120f, 184f)
        }.build()

    val Undo: ImageVector
        get() = ImageVector.Builder(
            name = "OtsoIcons.Undo",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 256f,
            viewportHeight = 256f
        ).path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = STROKE_WEIGHT,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(80f, 96f)
            lineTo(32f, 96f)
            lineTo(32f, 48f)
            
            moveTo(65.8f, 190.2f)
            arcTo(88f, 88f, 0f, isMoreThanHalf = true, isPositiveArc = false, 65.8f, 65.8f)
            lineTo(32f, 96f)
        }.build()

    val Redo: ImageVector
        get() = ImageVector.Builder(
            name = "OtsoIcons.Redo",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 256f,
            viewportHeight = 256f
        ).path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = STROKE_WEIGHT,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(176f, 96f)
            lineTo(224f, 96f)
            lineTo(224f, 48f)
            
            moveTo(190.2f, 190.2f)
            arcTo(88f, 88f, 0f, isMoreThanHalf = true, isPositiveArc = true, 190.2f, 65.8f)
            lineTo(224f, 96f)
        }.build()
    val TextB: ImageVector
        get() = ImageVector.Builder(
            name = "OtsoIcons.TextB",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 256f,
            viewportHeight = 256f
        ).path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 22f, // Bold is slightly thicker
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(64f, 48f)
            lineTo(128f, 48f)
            arcTo(40f, 40f, 0f, false, true, 128f, 128f)
            lineTo(64f, 128f)
            close()
            moveTo(64f, 128f)
            lineTo(144f, 128f)
            arcTo(44f, 44f, 0f, false, true, 144f, 216f)
            lineTo(64f, 216f)
            close()
        }.build()

    val TextItalic: ImageVector
        get() = ImageVector.Builder(
            name = "OtsoIcons.TextItalic",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 256f,
            viewportHeight = 256f
        ).path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = STROKE_WEIGHT,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(160f, 40f)
            lineTo(96f, 216f)
            moveTo(88f, 40f)
            lineTo(192f, 40f)
            moveTo(64f, 216f)
            lineTo(168f, 216f)
        }.build()

    val TextUnderline: ImageVector
        get() = ImageVector.Builder(
            name = "OtsoIcons.TextUnderline",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 256f,
            viewportHeight = 256f
        ).path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = STROKE_WEIGHT,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(64f, 40f)
            lineTo(64f, 128f)
            arcTo(64f, 64f, 0f, false, false, 192f, 128f)
            lineTo(192f, 40f)
            moveTo(40f, 216f)
            lineTo(216f, 216f)
        }.build()

    val TextStrikethrough: ImageVector
        get() = ImageVector.Builder(
            name = "OtsoIcons.TextStrikethrough",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 256f,
            viewportHeight = 256f
        ).path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = STROKE_WEIGHT,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(176f, 48f)
            arcTo(64f, 64f, 0f, false, false, 80f, 104f)
            moveTo(80f, 152f)
            arcTo(64f, 64f, 0f, false, false, 176f, 208f)
            moveTo(40f, 128f)
            lineTo(216f, 128f)
        }.build()

    val ListBullets: ImageVector
        get() = ImageVector.Builder(
            name = "OtsoIcons.ListBullets",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 256f,
            viewportHeight = 256f
        ).path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = STROKE_WEIGHT,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(88f, 64f)
            lineTo(224f, 64f)
            moveTo(88f, 128f)
            lineTo(224f, 128f)
            moveTo(88f, 192f)
            lineTo(224f, 192f)
            moveTo(44f, 64f)
            arcTo(4f, 4f, 0f, true, true, 40f, 64f)
            moveTo(44f, 128f)
            arcTo(4f, 4f, 0f, true, true, 40f, 128f)
            moveTo(44f, 192f)
            arcTo(4f, 4f, 0f, true, true, 40f, 192f)
        }.build()

    val ListNumbers: ImageVector
        get() = ImageVector.Builder(
            name = "OtsoIcons.ListNumbers",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 256f,
            viewportHeight = 256f
        ).path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = STROKE_WEIGHT,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(104f, 64f)
            lineTo(224f, 64f)
            moveTo(104f, 128f)
            lineTo(224f, 128f)
            moveTo(104f, 192f)
            lineTo(224f, 192f)
            moveTo(40f, 60f)
            lineTo(56f, 48f)
            lineTo(56f, 80f)
            moveTo(40f, 116f)
            arcTo(16f, 16f, 0f, true, true, 64f, 136f)
            lineTo(40f, 152f)
            lineTo(64f, 152f)
        }.build()

    val Code: ImageVector
        get() = ImageVector.Builder(
            name = "OtsoIcons.Code",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 256f,
            viewportHeight = 256f
        ).path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = STROKE_WEIGHT,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(64f, 88f)
            lineTo(16f, 128f)
            lineTo(64f, 168f)
            moveTo(192f, 88f)
            lineTo(240f, 128f)
            lineTo(192f, 168f)
            moveTo(160f, 40f)
            lineTo(96f, 216f)
        }.build()

    val Link: ImageVector
        get() = ImageVector.Builder(
            name = "OtsoIcons.Link",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 256f,
            viewportHeight = 256f
        ).path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = STROKE_WEIGHT,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(104f, 128f)
            lineTo(152f, 128f)
            moveTo(88f, 168f)
            arcTo(40f, 40f, 0f, false, true, 88f, 88f)
            lineTo(120f, 88f)
            moveTo(136f, 168f)
            lineTo(168f, 168f)
            arcTo(40f, 40f, 0f, false, false, 168f, 88f)
        }.build()
}
