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
}
