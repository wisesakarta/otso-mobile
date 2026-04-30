package com.otso.app.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * OtsoIcons — The Phosphor Bridge.
 * Using exact Phosphor "Regular" SVG paths mapped to Compose.
 */
object OtsoIcons {
    private const val STROKE_WEIGHT = 18f

    val X: ImageVector get() = buildIcon("X", "M200,56 L56,200 M200,200 L56,56")

    val CaretUp: ImageVector get() = buildIcon("CaretUp", "M48,160 L128,80 L208,160")

    val CaretDown: ImageVector get() = buildIcon("CaretDown", "M208,96 L128,176 L48,96")

    val Camera: ImageVector get() = buildIcon("Camera", "M 128.0 96.0 a 36.0,36.0 0 1,0 0,72.0 a 36.0,36.0 0 1,0 0,-72.0 M208,208H48a16,16,0,0,1-16-16V80A16,16,0,0,1,48,64H80L96,40h64l16,24h32a16,16,0,0,1,16,16V192A16,16,0,0,1,208,208Z")

    val Undo: ImageVector get() = buildIcon("Undo", "M80,136 L32,88 L80,40 M80,200h88a56,56,0,0,0,56-56h0a56,56,0,0,0-56-56H32")

    val Redo: ImageVector get() = buildIcon("Redo", "M176,136 L224,88 L176,40 M176,200H88a56,56,0,0,1-56-56h0A56,56,0,0,1,88,88H224")

    val Check: ImageVector get() = buildIcon("Check", "M40,144 L96,200 L224,72")

    val ArrowCounterClockwise: ImageVector get() = buildIcon("ArrowCounterClockwise", "M24,56 L24,104 L72,104 M67.59,192A88,88,0,1,0,65.77,65.77L24,104")

    val ArrowLeft: ImageVector get() = buildIcon("ArrowLeft", "M216,128 L40,128 M112,56 L40,128 L112,200")

    val Plus: ImageVector get() = buildIcon("Plus", "M40,128 L216,128 M128,40 L128,216")

    val Minus: ImageVector get() = buildIcon("Minus", "M40,128 L216,128")

    val TextB: ImageVector get() = buildIcon("TextB", "M80,120h80a40,40,0,0,1,0,80H80V48h68a36,36,0,0,1,0,72")

    val TextItalic: ImageVector get() = buildIcon("TextItalic", "M152,56 L104,200 M64,200 L144,200 M112,56 L192,56")

    val TextUnderline: ImageVector get() = buildIcon("TextUnderline", "M64,224 L192,224 M184,56v80a56,56,0,0,1-112,0V56")

    val TextStrikethrough: ImageVector get() = buildIcon("TextStrikethrough", "M40,128 L216,128 M76.33,96a25.71,25.71,0,0,1-1.22-8c0-22.09,22-40,52.89-40,23,0,40.24,9.87,48,24 M72,168c0,22.09,25.07,40,56,40s56-17.91,56-40c0-23.77-21.62-33-45.6-40")

    val ListBullets: ImageVector get() = buildIcon("ListBullets", "M88,64 L216,64 M88,128 L216,128 M88,192 L216,192 M 44.0 52.0 a 12.0,12.0 0 1,0 0,24.0 a 12.0,12.0 0 1,0 0,-24.0 M 44.0 116.0 a 12.0,12.0 0 1,0 0,24.0 a 12.0,12.0 0 1,0 0,-24.0 M 44.0 180.0 a 12.0,12.0 0 1,0 0,24.0 a 12.0,12.0 0 1,0 0,-24.0")

    val ListNumbers: ImageVector get() = buildIcon("ListNumbers", "M104,128 L216,128 M104,64 L216,64 M104,192 L216,192 M56,104 L56,40 L40,48 M72,208H40l28.68-38.37a15.69,15.69,0,0,0-3.24-22.41,16.78,16.78,0,0,0-23.06,3.15,15.85,15.85,0,0,0-2.38,4.3")

    val Code: ImageVector get() = buildIcon("Code", "M160,40 L96,216 M64,88 L16,128 L64,168 M192,88 L240,128 L192,168")

    val Link: ImageVector get() = buildIcon("Link", "M141.38,64.68l11-11a46.62,46.62,0,0,1,65.94,0h0a46.62,46.62,0,0,1,0,65.94L193.94,144,183.6,154.34a46.63,46.63,0,0,1-66-.05h0A46.48,46.48,0,0,1,104,120.06 M114.62,191.32l-11,11a46.63,46.63,0,0,1-66-.05h0a46.63,46.63,0,0,1,.06-65.89L72.4,101.66a46.62,46.62,0,0,1,65.94,0h0A46.45,46.45,0,0,1,152,135.94")

    val Highlighter: ImageVector get() = buildIcon("Highlighter", "M88,128 L24,192 L96,216 L136,176 M184,160l-26.34,26.34a8,8,0,0,1-11.32,0L77.66,117.66a8,8,0,0,1,0-11.32L104,80 M248,112l-50.34,50.34a8,8,0,0,1-11.32,0L101.66,77.66a8,8,0,0,1,0-11.32L152,16")

    private fun buildIcon(iconName: String, pathData: String): ImageVector {
        return ImageVector.Builder(
            name = "OtsoIcons." + iconName,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 256f,
            viewportHeight = 256f
        ).addPath(
            pathData = addPathNodes(pathData),
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = STROKE_WEIGHT,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ).build()
    }
}
