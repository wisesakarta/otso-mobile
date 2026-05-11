package com.otso.app

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

object BrandAssets {
    val logoDark = R.drawable.ic_kontio_dark
    val logoLight = R.drawable.ic_kontio_light

    val defaultFont = FontFamily(
        Font(R.font.excon_variable, FontWeight.Light),
        Font(R.font.excon_variable, FontWeight.Normal),
    )
}
