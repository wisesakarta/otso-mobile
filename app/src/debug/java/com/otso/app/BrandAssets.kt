package com.otso.app

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

object BrandAssets {
    val logoDark = R.drawable.ic_kontio_dark
    val logoLight = R.drawable.ic_kontio_light

    // Excon Variable — single TTF file covers all weights via wght axis.
    // Android font renderer maps each FontWeight entry to the correct variation automatically.
    val defaultFont = FontFamily(
        Font(R.font.excon_variable, FontWeight.Thin),
        Font(R.font.excon_variable, FontWeight.ExtraLight),
        Font(R.font.excon_variable, FontWeight.Light),
        Font(R.font.excon_variable, FontWeight.Normal),
        Font(R.font.excon_variable, FontWeight.Medium),
        Font(R.font.excon_variable, FontWeight.SemiBold),
        Font(R.font.excon_variable, FontWeight.Bold),
        Font(R.font.excon_variable, FontWeight.ExtraBold),
        Font(R.font.excon_variable, FontWeight.Black),
    )
}
