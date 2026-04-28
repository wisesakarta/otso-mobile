package com.otso.app.logic

import android.app.Application
import android.content.Intent
import android.graphics.Matrix
import android.graphics.Typeface
import android.net.Uri
import android.util.Log
import android.util.LruCache
import com.otso.app.core.FontManager
import com.otso.app.core.OtsoPreferences
import com.otso.app.viewmodel.EditorUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object TypefaceCache {
    private const val VmShareForCache = 0.10f
    private const val MinimumCacheSizeKb = 256
    private const val EstimatedTypefaceSizeKb = 256

    private val renaissanceMatrixValues = floatArrayOf(
        0.96f, -0.25f, 0f,
        0f, 1f, 0f,
        0f, 0f, 1f,
    )
    private val renaissanceSignature = renaissanceMatrixValues.joinToString(separator = ",")

    private val maxCacheSizeKb: Int by lazy {
        val vmMaxKb = (Runtime.getRuntime().maxMemory() / 1024L).coerceAtLeast(1L)
        (vmMaxKb * VmShareForCache).toInt().coerceAtLeast(MinimumCacheSizeKb)
    }

    private val cache = object : LruCache<String, Typeface>(maxCacheSizeKb) {
        override fun sizeOf(key: String, value: Typeface): Int = EstimatedTypefaceSizeKb
    }

    fun boldTypeface(): Typeface = getOrCreate("bold", Typeface.DEFAULT_BOLD)

    fun italicTypeface(): Typeface = getOrCreate(
        "italic",
        Typeface.create(Typeface.DEFAULT, Typeface.ITALIC),
    )

    fun applyRenaissanceMatrix(base: Typeface): Typeface {
        // Typeface does not expose affine glyph transforms; we preserve style identity here.
        // The matrix is consumed by renderer-level geometric transform to avoid synthetic distortion.
        Matrix().apply { setValues(renaissanceMatrixValues) }
        return Typeface.create(base, base.style)
    }

    private fun getOrCreate(cacheKey: String, base: Typeface): Typeface {
        val key = "$cacheKey|${base.style}|$renaissanceSignature"
        cache.get(key)?.let { return it }
        return applyRenaissanceMatrix(base).also { transformed ->
            cache.put(key, transformed)
        }
    }
}

class FontFoundryEngine(
    private val application: Application,
    private val _uiState: MutableStateFlow<EditorUiState>,
    private val scope: CoroutineScope,
) {
    private companion object {
        const val FOUNDRY_TAG = "OtsoFoundry"
    }

    fun initializeFontFlow() {
        scope.launch {
            OtsoPreferences.folderFoundryUriFlow(application).collect { uriString ->
                if (uriString.isNullOrBlank()) {
                    _uiState.update {
                        it.copy(
                            foundryFolderUri = null,
                            font = it.font.copy(
                                activeFoundryFamily = null,
                                activeFoundryVariantCount = 0,
                            ),
                            activeFoundryFamilyName = null,
                            isFontInitialized = true,
                        )
                    }
                    return@collect
                }

                _uiState.update { it.copy(foundryFolderUri = uriString) }
                val parsedUri = runCatching { Uri.parse(uriString) }.getOrNull()
                if (parsedUri == null) {
                    _uiState.update {
                        it.copy(
                            font = it.font.copy(
                                activeFoundryFamily = null,
                                activeFoundryVariantCount = 0,
                            ),
                            activeFoundryFamilyName = null,
                            fileAccessError = "Stored font folder URI is invalid.",
                        )
                    }
                    return@collect
                }

                val foundries = withContext(Dispatchers.IO) {
                    FontManager.scanFolderForFamilies(parsedUri)
                }
                val firstFamily = foundries.firstOrNull()
                Log.d(
                    FOUNDRY_TAG,
                    "Resolved foundry families=${foundries.size}, active='${firstFamily?.name}', variants=${firstFamily?.variantCount ?: 0}",
                )
                _uiState.update { state ->
                    state.copy(
                        font = state.font.copy(
                            activeFoundryFamily = firstFamily?.composeFamily,
                            activeFoundryVariantCount = firstFamily?.variantCount ?: 0,
                        ),
                        activeFoundryFamilyName = firstFamily?.name,
                        customFontName = firstFamily?.name ?: state.customFontName,
                        fileAccessError = if (firstFamily == null) {
                            "No supported font families found in selected folder."
                        } else {
                            null
                        },
                        isFontInitialized = true,
                    )
                }
            }
        }
    }

    fun setFoundryFolder(uri: Uri) {
        scope.launch {
            persistFoundryUriPermission(uri)
            OtsoPreferences.setFolderFoundryUri(application, uri.toString())
            OtsoPreferences.setCustomFontPath(application, null)
            OtsoPreferences.setCustomFontName(application, null)
        }
    }

    fun resetCustomFont() {
        scope.launch {
            releaseFoundryUriPermission(_uiState.value.foundryFolderUri)
            OtsoPreferences.setFolderFoundryUri(application, null)
            OtsoPreferences.setCustomFontPath(application, null)
            OtsoPreferences.setCustomFontName(application, null)
            _uiState.update {
                it.copy(
                    foundryFolderUri = null,
                    font = it.font.copy(
                        activeFoundryFamily = null,
                        activeFoundryVariantCount = 0,
                    ),
                    activeFoundryFamilyName = null,
                )
            }
        }
    }

    private fun persistFoundryUriPermission(uri: Uri) {
        val resolver = application.contentResolver
        val readWriteFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        val readOnlyFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION

        val persisted = runCatching {
            resolver.takePersistableUriPermission(uri, readWriteFlags)
        }.isSuccess || runCatching {
            resolver.takePersistableUriPermission(uri, readOnlyFlags)
        }.isSuccess

        if (!persisted) {
            _uiState.update {
                it.copy(fileAccessError = "Unable to persist access to selected font folder.")
            }
        }
    }

    private fun releaseFoundryUriPermission(uriString: String?) {
        val uri = uriString?.let { runCatching { Uri.parse(it) }.getOrNull() } ?: return
        val resolver = application.contentResolver
        val readWriteFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        val readOnlyFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION

        if (!runCatching { resolver.releasePersistableUriPermission(uri, readWriteFlags) }.isSuccess) {
            runCatching { resolver.releasePersistableUriPermission(uri, readOnlyFlags) }
        }
    }
}
