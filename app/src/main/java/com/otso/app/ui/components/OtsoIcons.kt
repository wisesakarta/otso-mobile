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
 * Custom implementation using manual paths for maximum stability and visual perfection.
 * All icons follow Phosphor Icons v2.1.0 geometry.
 */
object OtsoIcons {
    private const val STROKE_WEIGHT = 16f
    private const val THIN_STROKE = 1.5f

    val X = buildIcon("X") {
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = STROKE_WEIGHT, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(200f, 56f)
            lineTo(56f, 200f)
            moveTo(56f, 56f)
            lineTo(200f, 200f)
        }
    }

    val CaretUp = buildIcon("CaretUp") {
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = STROKE_WEIGHT, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(48f, 160f)
            lineTo(128f, 80f)
            lineTo(208f, 160f)
        }
    }

    val CaretDown = buildIcon("CaretDown") {
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = STROKE_WEIGHT, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(48f, 96f)
            lineTo(128f, 176f)
            lineTo(208f, 96f)
        }
    }

    val Camera = buildIcon("Camera") {
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = STROKE_WEIGHT, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(208f, 208f)
            horizontalLineTo(48f)
            arcTo(16f, 16f, 0f, false, true, 32f, 192f)
            verticalLineTo(80f)
            arcTo(16f, 16f, 0f, false, true, 48f, 64f)
            horizontalLineTo(80f)
            lineTo(96f, 40f)
            horizontalLineTo(160f)
            lineTo(176f, 64f)
            horizontalLineTo(208f)
            arcTo(16f, 16f, 0f, false, true, 224f, 80f)
            verticalLineTo(192f)
            arcTo(16f, 16f, 0f, false, true, 208f, 208f)
            close()
            moveTo(128f, 168f)
            arcTo(36f, 36f, 0f, true, false, 92f, 132f)
            arcTo(36f, 36f, 0f, false, false, 128f, 168f)
            close()
        }
    }

    val Undo = buildIcon("Undo") {
        path(fill = SolidColor(Color.Black), stroke = null) {
            moveTo(224f, 128f)
            arcTo(96f, 96f, 0f, false, true, 129.29f, 224f)
            horizontalLineTo(128f)
            arcTo(95.38f, 95.38f, 0f, false, true, 62.1f, 197.8f)
            arcTo(8f, 8f, 0f, false, true, 73.1f, 186.17f)
            arcTo(80f, 80f, 0f, true, false, 71.43f, 71.39f)
            arcTo(3.07f, 3.07f, 0f, false, true, 71.17f, 71.64f)
            lineTo(44.59f, 96f)
            horizontalLineTo(72f)
            arcTo(8f, 8f, 0f, false, true, 72f, 112f)
            horizontalLineTo(24f)
            arcTo(8f, 8f, 0f, false, true, 16f, 104f)
            verticalLineTo(56f)
            arcTo(8f, 8f, 0f, false, true, 32f, 56f)
            verticalLineTo(85.8f)
            lineTo(60.25f, 60f)
            arcTo(96f, 96f, 0f, false, true, 224f, 128f)
            close()
        }
    }

    val Redo = buildIcon("Redo") {
        path(fill = SolidColor(Color.Black), stroke = null) {
            moveTo(240f, 56f)
            verticalLineTo(104f)
            arcTo(8f, 8f, 0f, false, true, 232f, 112f)
            horizontalLineTo(184f)
            arcTo(8f, 8f, 0f, false, true, 184f, 96f)
            horizontalLineTo(211.4f)
            lineTo(184.81f, 71.64f)
            lineTo(184.56f, 71.4f)
            arcTo(80f, 80f, 0f, true, false, 182.89f, 186.18f)
            arcTo(8f, 8f, 0f, false, true, 193.89f, 197.81f)
            arcTo(95.44f, 95.44f, 0f, false, true, 128f, 224f)
            horizontalLineTo(126.68f)
            arcTo(96f, 96f, 0f, true, true, 195.75f, 60f)
            lineTo(224f, 85.8f)
            verticalLineTo(56f)
            arcTo(8f, 8f, 0f, false, true, 240f, 56f)
            close()
        }
    }

    val Check = buildIcon("Check") {
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = STROKE_WEIGHT, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(40f, 128f)
            lineTo(96f, 184f)
            lineTo(216f, 64f)
        }
    }

    val ArrowCounterClockwise = buildIcon("ArrowCounterClockwise") {
        path(fill = SolidColor(Color.Black), stroke = null) {
            moveTo(224f, 128f)
            arcTo(96f, 96f, 0f, false, true, 129.29f, 224f)
            horizontalLineTo(128f)
            arcTo(95.38f, 95.38f, 0f, false, true, 62.1f, 197.8f)
            arcTo(8f, 8f, 0f, false, true, 73.1f, 186.17f)
            arcTo(80f, 80f, 0f, true, false, 71.43f, 71.39f)
            arcTo(3.07f, 3.07f, 0f, false, true, 71.17f, 71.64f)
            lineTo(44.59f, 96f)
            horizontalLineTo(72f)
            arcTo(8f, 8f, 0f, false, true, 72f, 112f)
            horizontalLineTo(24f)
            arcTo(8f, 8f, 0f, false, true, 16f, 104f)
            verticalLineTo(56f)
            arcTo(8f, 8f, 0f, false, true, 32f, 56f)
            verticalLineTo(85.8f)
            lineTo(60.25f, 60f)
            arcTo(96f, 96f, 0f, false, true, 224f, 128f)
            close()
        }
    }

    val ArrowLeft = buildIcon("ArrowLeft") {
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = STROKE_WEIGHT, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(208f, 128f)
            horizontalLineTo(48f)
            moveTo(48f, 128f)
            lineTo(112f, 64f)
            moveTo(48f, 128f)
            lineTo(112f, 192f)
        }
    }

    val ArrowRight = buildIcon("ArrowRight") {
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = STROKE_WEIGHT, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(48f, 128f)
            horizontalLineTo(208f)
            moveTo(208f, 128f)
            lineTo(144f, 64f)
            moveTo(208f, 128f)
            lineTo(144f, 192f)
        }
    }

    val Plus = buildIcon("Plus") {
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = STROKE_WEIGHT, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(40f, 128f)
            horizontalLineTo(216f)
            moveTo(128f, 40f)
            verticalLineTo(216f)
        }
    }

    val Minus = buildIcon("Minus") {
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = STROKE_WEIGHT, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(40f, 128f)
            horizontalLineTo(216f)
        }
    }

    val TextB = buildIcon("TextB") {
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = STROKE_WEIGHT, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(64f, 48f)
            horizontalLineTo(140f)
            arcTo(44f, 44f, 0f, false, true, 140f, 136f)
            horizontalLineTo(64f)
            verticalLineTo(48f)
            close()
            moveTo(64f, 136f)
            horizontalLineTo(148f)
            arcTo(44f, 44f, 0f, false, true, 148f, 224f)
            horizontalLineTo(64f)
            verticalLineTo(136f)
            close()
        }
    }

    val TextItalic = buildIcon("TextItalic") {
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = STROKE_WEIGHT, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(152f, 56f)
            lineTo(104f, 200f)
            moveTo(64f, 200f)
            horizontalLineTo(144f)
            moveTo(112f, 56f)
            horizontalLineTo(192f)
        }
    }

    val TextUnderline = buildIcon("TextUnderline") {
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = STROKE_WEIGHT, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(64f, 48f)
            verticalLineTo(128f)
            arcTo(64f, 64f, 0f, false, false, 192f, 128f)
            verticalLineTo(48f)
            moveTo(40f, 216f)
            horizontalLineTo(216f)
        }
    }

    val TextStrikethrough = buildIcon("TextStrikethrough") {
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = STROKE_WEIGHT, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(40f, 128f)
            horizontalLineTo(216f)
            moveTo(76.33f, 96f)
            arcTo(25.71f, 25.71f, 0f, false, true, 75.11f, 88f)
            arcTo(52.89f, 52.89f, 0f, false, true, 128f, 48f)
            arcTo(48f, 48f, 0f, false, true, 176f, 72f)
            moveTo(72f, 168f)
            arcTo(56f, 56f, 0f, false, false, 128f, 208f)
            arcTo(56f, 56f, 0f, false, false, 184f, 168f)
            arcTo(45.6f, 45.6f, 0f, false, false, 138.4f, 128f)
        }
    }

    val ListBullets = buildIcon("ListBullets") {
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = STROKE_WEIGHT, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(88f, 64f)
            horizontalLineTo(216f)
            moveTo(88f, 128f)
            horizontalLineTo(216f)
            moveTo(88f, 192f)
            horizontalLineTo(216f)
            moveTo(40f, 64f)
            horizontalLineTo(40.1f)
            moveTo(40f, 128f)
            horizontalLineTo(40.1f)
            moveTo(40f, 192f)
            horizontalLineTo(40.1f)
        }
    }

    val ListNumbers = buildIcon("ListNumbers") {
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = STROKE_WEIGHT, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(104f, 64f)
            horizontalLineTo(216f)
            moveTo(104f, 128f)
            horizontalLineTo(216f)
            moveTo(104f, 192f)
            horizontalLineTo(216f)
            moveTo(40f, 60f)
            lineTo(56f, 48f)
            verticalLineTo(80f)
        }
    }

    val Code = buildIcon("Code") {
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = STROKE_WEIGHT, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(64f, 88f)
            lineTo(16f, 128f)
            lineTo(64f, 168f)
            moveTo(192f, 88f)
            lineTo(240f, 128f)
            lineTo(192f, 168f)
            moveTo(160f, 40f)
            lineTo(96f, 216f)
        }
    }

    val Link = buildIcon("Link") {
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = STROKE_WEIGHT, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(96f, 160f)
            lineTo(160f, 96f)
            moveTo(112f, 76.11f)
            lineTo(142.06f, 46.11f)
            arcTo(48f, 48f, 0f, false, true, 209.94f, 113.99f)
            lineTo(179.88f, 144f)
            moveTo(76.11f, 112f)
            lineTo(46.11f, 142.06f)
            arcTo(48f, 48f, 0f, false, false, 113.99f, 209.94f)
            lineTo(144f, 179.88f)
        }
    }

    val Highlighter = buildIcon("Highlighter", viewport = 24f) {
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(8f, 21f)
            lineTo(9f, 14f)
            horizontalLineTo(15f)
            lineTo(16f, 21f)

            moveTo(11.6154f, 8.34613f)
            lineTo(10f, 14f)
            horizontalLineTo(14f)
            lineTo(12.3846f, 8.34613f)
            curveTo(12.2741f, 7.95932f, 11.7259f, 7.95932f, 11.6154f, 8.34613f)
            close()

            moveTo(22f, 12f)
            curveTo(22f, 17.5228f, 17.5228f, 22f, 12f, 22f)
            curveTo(6.47715f, 22f, 2f, 17.5228f, 2f, 12f)
            curveTo(2f, 6.47715f, 6.47715f, 2f, 12f, 2f)
            curveTo(17.5228f, 2f, 22f, 6.47715f, 22f, 12f)
            close()
        }
    }

    val PaperPlaneTilt = buildIcon("PaperPlaneTilt") {
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = STROKE_WEIGHT, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(210.3f, 35.9f)
            lineTo(23.9f, 88.4f)
            arcTo(8f, 8f, 0f, false, false, 26.1f, 103.9f)
            lineTo(96f, 128f)
            lineTo(168f, 56f)
            lineTo(128f, 160f)
            lineTo(152.1f, 229.9f)
            arcTo(8f, 8f, 0f, false, false, 167.6f, 232.1f)
            lineTo(220.1f, 45.7f)
            arcTo(8.1f, 8.1f, 0f, false, false, 210.3f, 35.9f)
            close()
        }
    }

    val TabKey = buildIcon("TabKey") {
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = STROKE_WEIGHT, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(48f, 128f)
            horizontalLineTo(208f)
            moveTo(208f, 128f)
            lineTo(144f, 64f)
            moveTo(208f, 128f)
            lineTo(144f, 192f)
            moveTo(208f, 48f)
            verticalLineTo(208f)
        }
    }

    val Tabs = buildIcon("Tabs") {
        path(fill = SolidColor(Color.Black), stroke = null) {
            moveTo(224f, 128f)
            arcTo(8f, 8f, 0f, false, true, 216f, 136f)
            horizontalLineTo(112f)
            arcTo(8f, 8f, 0f, false, true, 112f, 120f)
            horizontalLineTo(216f)
            arcTo(8f, 8f, 0f, false, true, 224f, 128f)
            close()
            moveTo(112f, 72f)
            horizontalLineTo(216f)
            arcTo(8f, 8f, 0f, false, false, 216f, 56f)
            horizontalLineTo(112f)
            arcTo(8f, 8f, 0f, false, false, 112f, 72f)
            close()
            moveTo(216f, 184f)
            horizontalLineTo(40f)
            arcTo(8f, 8f, 0f, false, false, 40f, 200f)
            horizontalLineTo(216f)
            arcTo(8f, 8f, 0f, false, false, 216f, 184f)
            close()
            moveTo(34.34f, 141.66f)
            arcTo(8f, 8f, 0f, false, false, 45.66f, 141.66f)
            lineTo(85.66f, 101.66f)
            arcTo(8f, 8f, 0f, false, false, 85.66f, 90.34f)
            lineTo(45.66f, 50.34f)
            arcTo(8f, 8f, 0f, false, false, 34.34f, 61.66f)
            lineTo(68.69f, 96f)
            lineTo(34.34f, 130.34f)
            arcTo(8f, 8f, 0f, false, false, 34.34f, 141.66f)
            close()
        }
    }

    val Parentheses = buildIcon("Parentheses") {
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = STROKE_WEIGHT, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(88f, 48f)
            arcTo(128f, 128f, 0f, false, false, 88f, 208f)
            moveTo(168f, 48f)
            arcTo(128f, 128f, 0f, false, true, 168f, 208f)
        }
    }

    val Brackets = buildIcon("Brackets") {
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = STROKE_WEIGHT, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(80f, 48f)
            horizontalLineTo(48f)
            verticalLineTo(208f)
            horizontalLineTo(80f)
            moveTo(176f, 48f)
            horizontalLineTo(208f)
            verticalLineTo(208f)
            horizontalLineTo(176f)
        }
    }

    val Slash = buildIcon("Slash") {
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = STROKE_WEIGHT, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(200f, 40f)
            lineTo(56f, 216f)
        }
    }

    val Quotes = buildIcon("Quotes") {
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = STROKE_WEIGHT, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(108f, 144f)
            horizontalLineTo(40f)
            arcTo(8f, 8f, 0f, false, true, 32f, 136f)
            verticalLineTo(72f)
            arcTo(8f, 8f, 0f, false, true, 40f, 64f)
            horizontalLineTo(100f)
            arcTo(8f, 8f, 0f, false, true, 108f, 72f)
            verticalLineTo(160f)
            arcTo(40f, 40f, 0f, false, true, 68f, 200f)
            
            moveTo(224f, 144f)
            horizontalLineTo(156f)
            arcTo(8f, 8f, 0f, false, true, 148f, 136f)
            verticalLineTo(72f)
            arcTo(8f, 8f, 0f, false, true, 156f, 64f)
            horizontalLineTo(216f)
            arcTo(8f, 8f, 0f, false, true, 224f, 72f)
            verticalLineTo(160f)
            arcTo(40f, 40f, 0f, false, true, 184f, 200f)
        }
    }

    val Circle = buildIcon("Circle") {
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = STROKE_WEIGHT, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(128f, 32f)
            arcTo(96f, 96f, 0f, true, true, 128f, 224f)
            arcTo(96f, 96f, 0f, true, true, 128f, 32f)
            close()
        }
    }

    val ArrowClockwise = buildIcon("ArrowClockwise") {
        path(fill = SolidColor(Color.Black), stroke = null) {
            moveTo(232f, 128f)
            arcTo(104f, 104f, 0f, false, true, 48.81f, 195.19f)
            arcTo(8f, 8f, 0f, false, true, 60.12f, 183.88f)
            arcTo(88f, 88f, 0f, true, false, 88f, 40f)
            arcTo(87.56f, 87.56f, 0f, false, false, 56f, 53.76f)
            verticalLineTo(40f)
            arcTo(8f, 8f, 0f, false, true, 72f, 40f)
            verticalLineTo(88f)
            arcTo(8f, 8f, 0f, false, true, 64f, 96f)
            horizontalLineTo(16f)
            arcTo(8f, 8f, 0f, false, true, 16f, 80f)
            horizontalLineTo(44.2f)
            arcTo(104f, 104f, 0f, false, true, 232f, 128f)
            close()
        }
    }

    val Folder = buildIcon("Folder") {
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = STROKE_WEIGHT, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(216.89f, 208f)
            horizontalLineTo(39.38f)
            arcTo(7.4f, 7.4f, 0f, false, true, 32f, 200.62f)
            verticalLineTo(80f)
            horizontalLineTo(216f)
            arcTo(8f, 8f, 0f, false, true, 224f, 88f)
            verticalLineTo(200.89f)
            arcTo(7.11f, 7.11f, 0f, false, true, 216.89f, 208f)
            close()
            moveTo(32f, 80f)
            verticalLineTo(56f)
            arcTo(8f, 8f, 0f, false, true, 40f, 48f)
            horizontalLineTo(92.69f)
            arcTo(8f, 8f, 0f, false, true, 98.34f, 50.34f)
            lineTo(128f, 80f)
        }
    }

    val FolderCounterClockwise = buildIcon("FolderCounterClockwise", viewport = 24f) {
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = THIN_STROKE, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(3.46875f, 18.3744f)
            lineTo(4.53321f, 16.0325f)
            moveTo(14.1134f, 18.3744f)
            lineTo(13.0489f, 16.0325f)
            moveTo(13.0489f, 16.0325f)
            lineTo(8.79105f, 6.66528f)
            lineTo(4.53321f, 16.0325f)
            horizontalLineTo(13.0489f)
        }
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = THIN_STROKE, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(15.1777f, 8.79421f)
            curveTo(15.1777f, 5.06857f, 21.0323f, 5.0686f, 21.0323f, 8.79421f)
            curveTo(21.0323f, 11.4554f, 18.3711f, 10.9231f, 18.3711f, 14.1164f)
        }
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = THIN_STROKE, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(18.3711f, 18.385f)
            lineTo(18.3817f, 18.3732f)
        }
    }

    val Asterisk = buildIcon("Asterisk") {
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = STROKE_WEIGHT, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(128f, 40f)
            verticalLineTo(216f)
            moveTo(51.79f, 84f)
            lineTo(204.21f, 172f)
            moveTo(51.79f, 172f)
            lineTo(204.21f, 84f)
        }
    }

    val Brain = buildIcon("Brain") {
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = STROKE_WEIGHT, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            // Simplified brain silhouette — two hemispheres with central divide
            moveTo(128f, 40f)
            verticalLineTo(216f)
            moveTo(128f, 56f)
            curveTo(100f, 56f, 72f, 72f, 72f, 104f)
            curveTo(72f, 128f, 88f, 140f, 88f, 160f)
            curveTo(88f, 180f, 104f, 200f, 128f, 200f)
            moveTo(128f, 56f)
            curveTo(156f, 56f, 184f, 72f, 184f, 104f)
            curveTo(184f, 128f, 168f, 140f, 168f, 160f)
            curveTo(168f, 180f, 152f, 200f, 128f, 200f)
            // Sulci (brain folds)
            moveTo(88f, 104f)
            curveTo(104f, 104f, 112f, 120f, 128f, 120f)
            moveTo(168f, 104f)
            curveTo(152f, 104f, 144f, 120f, 128f, 120f)
        }
    }

    val WarningCircle = buildIcon("WarningCircle") {
        path(fill = SolidColor(Color.Black), stroke = null) {
            moveTo(128f, 24f)
            arcTo(104f, 104f, 0f, true, true, 24f, 128f)
            arcTo(104f, 104f, 0f, false, true, 128f, 24f)
            close()
            moveTo(128f, 80f)
            arcTo(8f, 8f, 0f, false, false, 120f, 88f)
            verticalLineTo(136f)
            arcTo(8f, 8f, 0f, false, false, 136f, 136f)
            verticalLineTo(88f)
            arcTo(8f, 8f, 0f, false, false, 128f, 80f)
            close()
            moveTo(128f, 160f)
            arcTo(12f, 12f, 0f, true, false, 140f, 172f)
            arcTo(12f, 12f, 0f, false, false, 128f, 160f)
            close()
        }
    }

    val Info = buildIcon("Info") {
        path(fill = SolidColor(Color.Black), stroke = null) {
            moveTo(128f, 24f)
            arcTo(104f, 104f, 0f, true, true, 24f, 128f)
            arcTo(104f, 104f, 0f, false, true, 128f, 24f)
            close()
            moveTo(128f, 112f)
            arcTo(8f, 8f, 0f, false, false, 120f, 120f)
            verticalLineTo(160f)
            arcTo(8f, 8f, 0f, false, false, 136f, 160f)
            verticalLineTo(120f)
            arcTo(8f, 8f, 0f, false, false, 128f, 112f)
            close()
            moveTo(128f, 80f)
            arcTo(12f, 12f, 0f, true, false, 140f, 92f)
            arcTo(12f, 12f, 0f, false, false, 128f, 112f)
            close()
        }
    }

    // Two horizontal selection bars — represents "select all text"
    val SelectAll = buildIcon("SelectAll") {
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = STROKE_WEIGHT, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(48f, 96f)
            horizontalLineTo(208f)
            moveTo(48f, 128f)
            horizontalLineTo(208f)
            moveTo(48f, 160f)
            horizontalLineTo(208f)
            moveTo(32f, 72f)
            verticalLineTo(184f)
            moveTo(224f, 72f)
            verticalLineTo(184f)
        }
    }

    private fun buildIcon(
        name: String,
        viewport: Float = 256f,
        block: ImageVector.Builder.() -> ImageVector.Builder
    ): ImageVector = ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = viewport,
        viewportHeight = viewport
    ).block().build()
}

