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
 */
object OtsoIcons {
    private const val STROKE_WEIGHT = 18f

    val X: ImageVector get() = buildIcon("X") {
        moveTo(192f, 64f); lineTo(64f, 192f)
        moveTo(64f, 64f); lineTo(192f, 192f)
    }

    val CaretUp: ImageVector get() = buildIcon("CaretUp") {
        moveTo(64f, 160f); lineTo(128f, 96f); lineTo(192f, 160f)
    }

    val CaretDown: ImageVector get() = buildIcon("CaretDown") {
        moveTo(64f, 96f); lineTo(128f, 160f); lineTo(192f, 96f)
    }

    val Camera: ImageVector get() = buildIcon("Camera") {
        moveTo(24f, 80f); lineTo(232f, 80f); lineTo(232f, 208f); lineTo(24f, 208f); close()
        moveTo(80f, 80f); lineTo(96f, 48f); lineTo(160f, 48f); lineTo(176f, 80f)
        moveTo(128f, 172f); arcTo(36f, 36f, 1f, true, false, 128f, 100f)
        arcTo(36f, 36f, 1f, true, false, 128f, 172f)
    }

    val Undo: ImageVector get() = buildIcon("Undo") {
        moveTo(80f, 96f); lineTo(32f, 96f); lineTo(32f, 48f)
        moveTo(65.8f, 190.2f); arcTo(88f, 88f, 0f, true, false, 65.8f, 65.8f); lineTo(32f, 96f)
    }

    val Redo: ImageVector get() = buildIcon("Redo") {
        moveTo(176f, 96f); lineTo(224f, 96f); lineTo(224f, 48f)
        moveTo(190.2f, 190.2f); arcTo(88f, 88f, 0f, true, true, 190.2f, 65.8f); lineTo(224f, 96f)
    }

    val Check: ImageVector get() = buildIcon("Check") {
        moveTo(216f, 72f); lineTo(104f, 184f); lineTo(40f, 120f)
    }

    val ArrowCounterClockwise: ImageVector get() = buildIcon("ArrowCounterClockwise") {
        moveTo(79.8f, 99.7f); lineTo(31.8f, 99.7f); lineTo(31.8f, 51.7f)
        moveTo(65.8f, 190.2f); arcTo(88f, 88f, 0f, true, false, 65.8f, 65.8f); lineTo(31.8f, 99.7f)
    }

    val ArrowLeft: ImageVector get() = buildIcon("ArrowLeft") {
        moveTo(200f, 128f); lineTo(56f, 128f); moveTo(120f, 64f); lineTo(56f, 128f); lineTo(120f, 192f)
    }

    val Plus: ImageVector get() = buildIcon("Plus") {
        moveTo(56f, 128f); lineTo(200f, 128f); moveTo(128f, 56f); lineTo(128f, 200f)
    }

    val Minus: ImageVector get() = buildIcon("Minus") {
        moveTo(56f, 128f); lineTo(200f, 128f)
    }

    val TextB: ImageVector get() = buildIcon("TextB") {
        moveTo(64f, 48f); lineTo(156f, 48f); arcTo(44f, 44f, 0f, false, true, 156f, 136f); lineTo(64f, 136f); close()
        moveTo(64f, 136f); lineTo(164f, 136f); arcTo(48f, 48f, 0f, false, true, 164f, 232f); lineTo(64f, 232f); close()
    }

    val TextItalic: ImageVector get() = buildIcon("TextItalic") {
        moveTo(160f, 40f); lineTo(112f, 216f); moveTo(128f, 40f); lineTo(192f, 40f); moveTo(64f, 216f); lineTo(128f, 216f)
    }

    val TextUnderline: ImageVector get() = buildIcon("TextUnderline") {
        moveTo(64f, 40f); lineTo(64f, 128f); arcTo(64f, 64f, 0f, false, false, 192f, 128f); lineTo(192f, 40f)
        moveTo(48f, 216f); lineTo(208f, 216f)
    }

    val TextStrikethrough: ImageVector get() = buildIcon("TextStrikethrough") {
        moveTo(40f, 128f); lineTo(216f, 128f)
        moveTo(176f, 56f); lineTo(176f, 88f); arcTo(48f, 48f, 0f, false, true, 128f, 136f); moveTo(80f, 168f); arcTo(48f, 48f, 0f, false, false, 128f, 216f); lineTo(128f, 232f)
    }

    val ListBullets: ImageVector get() = buildIcon("ListBullets") {
        moveTo(88f, 64f); lineTo(216f, 64f); moveTo(88f, 128f); lineTo(216f, 128f); moveTo(88f, 192f); lineTo(216f, 192f)
        moveTo(40f, 64f); arcTo(4f, 4f, 0f, true, true, 44f, 68f); arcTo(4f, 4f, 0f, false, true, 40f, 64f)
        moveTo(40f, 128f); arcTo(4f, 4f, 0f, true, true, 44f, 132f); arcTo(4f, 4f, 0f, false, true, 40f, 128f)
        moveTo(40f, 192f); arcTo(4f, 4f, 0f, true, true, 44f, 196f); arcTo(4f, 4f, 0f, false, true, 40f, 192f)
    }

    val ListNumbers: ImageVector get() = buildIcon("ListNumbers") {
        moveTo(104f, 64f); lineTo(216f, 64f); moveTo(104f, 128f); lineTo(216f, 128f); moveTo(104f, 192f); lineTo(216f, 192f)
        moveTo(40f, 56f); lineTo(56f, 40f); lineTo(56f, 88f)
        moveTo(40f, 144f); lineTo(64f, 144f); lineTo(40f, 176f); lineTo(64f, 176f)
    }

    val Code: ImageVector get() = buildIcon("Code") {
        moveTo(64f, 88f); lineTo(16f, 128f); lineTo(64f, 168f)
        moveTo(192f, 88f); lineTo(240f, 128f); lineTo(192f, 168f)
        moveTo(160f, 40f); lineTo(96f, 216f)
    }

    val Link: ImageVector get() = buildIcon("Link") {
        moveTo(104f, 152f); arcTo(48f, 48f, 0f, false, true, 104f, 56f); lineTo(136f, 56f)
        moveTo(152f, 104f); arcTo(48f, 48f, 0f, false, true, 152f, 200f); lineTo(120f, 200f)
        moveTo(96f, 128f); lineTo(160f, 128f)
    }

    val Highlighter: ImageVector get() = buildIcon("Highlighter") {
        // Simple pen/marker shape + bottom line
        moveTo(160f, 40f); lineTo(192f, 72f); lineTo(112f, 152f); lineTo(64f, 168f); lineTo(80f, 120f); close()
        moveTo(40f, 216f); lineTo(216f, 216f)
    }

    private fun buildIcon(
        iconName: String,
        pathBuilder: androidx.compose.ui.graphics.vector.PathBuilder.() -> Unit
    ): ImageVector {
        return ImageVector.Builder(
            name = "OtsoIcons." + iconName,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 256f,
            viewportHeight = 256f
        ).path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = STROKE_WEIGHT,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            pathBuilder = pathBuilder
        ).build()
    }
}
