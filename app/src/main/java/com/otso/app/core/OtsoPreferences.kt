package com.otso.app.core

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private val Context.dataStore by preferencesDataStore(name = "otso_prefs")

object OtsoPreferences {
    private val THEME_MODE = stringPreferencesKey("theme_mode")
    private val EDITOR_FONT_SIZE = intPreferencesKey("editor_font_size")
    private val CUSTOM_FONT_PATH = stringPreferencesKey("custom_font_path")
    private val CUSTOM_FONT_NAME = stringPreferencesKey("custom_font_name")
    private val FOLDER_FOUNDRY_URI = stringPreferencesKey("folder_foundry_uri")
    private val CUSTOM_HIGHLIGHT_PALETTE = stringPreferencesKey("custom_highlight_palette")

    fun themeModeFlow(context: Context): Flow<String> {
        return context.dataStore.data.map { prefs ->
            prefs[THEME_MODE] ?: "system"
        }
    }

    fun editorFontSizeFlow(context: Context): Flow<Int> {
        return context.dataStore.data.map { prefs ->
            prefs[EDITOR_FONT_SIZE] ?: 15
        }
    }

    fun customFontPathFlow(context: Context): Flow<String?> {
        return context.dataStore.data.map { prefs ->
            prefs[CUSTOM_FONT_PATH]
        }
    }

    fun customFontNameFlow(context: Context): Flow<String?> {
        return context.dataStore.data.map { prefs ->
            prefs[CUSTOM_FONT_NAME]
        }
    }

    fun folderFoundryUriFlow(context: Context): Flow<String?> {
        return context.dataStore.data.map { prefs ->
            prefs[FOLDER_FOUNDRY_URI]
        }
    }

    fun customHighlightPaletteFlow(context: Context): Flow<List<Int>> {
        return context.dataStore.data.map { prefs ->
            decodeColorPalette(prefs[CUSTOM_HIGHLIGHT_PALETTE])
        }
    }

    suspend fun setThemeMode(
        context: Context,
        mode: String,
    ) {
        withContext(Dispatchers.IO) {
            context.dataStore.edit { prefs ->
                prefs[THEME_MODE] = mode
            }
        }
    }

    suspend fun setEditorFontSize(
        context: Context,
        sizeSp: Int,
    ) {
        withContext(Dispatchers.IO) {
            context.dataStore.edit { prefs ->
                prefs[EDITOR_FONT_SIZE] = sizeSp
            }
        }
    }

    suspend fun setCustomFontPath(
        context: Context,
        path: String?,
    ) {
        withContext(Dispatchers.IO) {
            context.dataStore.edit { prefs ->
                if (path == null) {
                    prefs.remove(CUSTOM_FONT_PATH)
                } else {
                    prefs[CUSTOM_FONT_PATH] = path
                }
            }
        }
    }

    suspend fun setCustomFontName(
        context: Context,
        name: String?,
    ) {
        withContext(Dispatchers.IO) {
            context.dataStore.edit { prefs ->
                if (name == null) {
                    prefs.remove(CUSTOM_FONT_NAME)
                } else {
                    prefs[CUSTOM_FONT_NAME] = name
                }
            }
        }
    }

    suspend fun setFolderFoundryUri(
        context: Context,
        uri: String?,
    ) {
        withContext(Dispatchers.IO) {
            context.dataStore.edit { prefs ->
                if (uri == null) {
                    prefs.remove(FOLDER_FOUNDRY_URI)
                } else {
                    prefs[FOLDER_FOUNDRY_URI] = uri
                }
            }
        }
    }

    suspend fun setCustomHighlightPalette(
        context: Context,
        palette: List<Int>,
    ) {
        withContext(Dispatchers.IO) {
            context.dataStore.edit { prefs ->
                if (palette.isEmpty()) {
                    prefs.remove(CUSTOM_HIGHLIGHT_PALETTE)
                } else {
                    prefs[CUSTOM_HIGHLIGHT_PALETTE] = encodeColorPalette(palette)
                }
            }
        }
    }

    private fun encodeColorPalette(palette: List<Int>): String {
        return palette.joinToString(separator = ",") { color ->
            val raw = color.toLong() and 0xFFFFFFFFL
            java.lang.Long.toHexString(raw)
        }
    }

    private fun decodeColorPalette(encoded: String?): List<Int> {
        if (encoded.isNullOrBlank()) return emptyList()
        return encoded.split(',')
            .mapNotNull { token ->
                token.trim().takeIf { it.isNotBlank() }?.let { hex ->
                    runCatching {
                        java.lang.Long.parseLong(hex, 16).toInt()
                    }.getOrNull()
                }
            }
    }
}
