package com.otso.app

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

object BrandAssets {
    val logoDark = R.drawable.ic_otso_dark
    val logoLight = R.drawable.ic_otso_light
    val defaultFont = FontFamily(
        Font(R.font.ia_writer_quattro_s_regular, FontWeight.Normal, FontStyle.Normal),
        Font(R.font.ia_writer_quattro_s_italic, FontWeight.Normal, FontStyle.Italic),
        Font(R.font.ia_writer_quattro_s_bold, FontWeight.Bold, FontStyle.Normal),
        Font(R.font.ia_writer_quattro_s_bold_italic, FontWeight.Bold, FontStyle.Italic),
    )
}
