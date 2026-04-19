package com.otso.app.core

import android.content.Context
import android.net.Uri
import java.io.File

object FontManager {
    private const val FONT_DIR = "fonts"

    fun importFontFromUri(
        context: Context,
        uri: Uri,
        extension: String,
    ): File? {
        val normalizedExtension = extension.lowercase()
        val fontDir = File(context.filesDir, FONT_DIR)
        if (!fontDir.exists() && !fontDir.mkdirs()) {
            return null
        }

        deleteCustomFont(context)
        val targetFile = File(fontDir, "custom_editor_font.$normalizedExtension")
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return null
            targetFile
        } catch (_: Exception) {
            if (targetFile.exists()) {
                targetFile.delete()
            }
            null
        }
    }

    fun deleteCustomFont(context: Context) {
        try {
            val fontDir = File(context.filesDir, FONT_DIR)
            if (!fontDir.exists()) return
            fontDir.listFiles()
                ?.filter { it.isFile && it.name.startsWith("custom_editor_font.") }
                ?.forEach { it.delete() }
        } catch (_: Exception) {
        }
    }
}
