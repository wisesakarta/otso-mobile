package com.otso.app.ui.theme

import androidx.compose.ui.graphics.Color

private const val BrightnessThreshold = 186f

fun Color.isBright(): Boolean {
    val red = this.red * 255f
    val green = this.green * 255f
    val blue = this.blue * 255f
    val luminance = (red * 0.299f) + (green * 0.587f) + (blue * 0.114f)
    return luminance >= BrightnessThreshold
}
