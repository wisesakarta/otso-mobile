package com.otso.app.core

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.otso.app.model.TabDocument
import com.otso.app.viewmodel.EditorUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class SessionData(
    val tabs: List<TabDocument>,
    val activeIndex: Int,
    val isDarkMode: Boolean,
)

class SessionIO(
    private val context: Context,
) {
    private val gson = Gson()
    private val sessionFile = File(context.filesDir, "session.json")

    suspend fun saveSession(state: EditorUiState) {
        withContext(Dispatchers.IO) {
            val payload = SessionData(
                tabs = state.tabs,
                activeIndex = state.activeIndex,
                isDarkMode = state.isDarkMode,
            )
            sessionFile.writeText(gson.toJson(payload))
        }
    }

    suspend fun loadSession(): SessionData? {
        return withContext(Dispatchers.IO) {
            if (!sessionFile.exists()) return@withContext null
            runCatching {
                val type = object : TypeToken<SessionData>() {}.type
                val data = gson.fromJson<SessionData>(sessionFile.readText(), type)
                // Guard against Gson returning LinkedTreeMap elements when generic type
                // info is corrupted by R8 — discard any session that can't be used safely.
                if (data?.tabs?.any { it !is TabDocument } == true) null else data
            }.getOrNull()
        }
    }
}
