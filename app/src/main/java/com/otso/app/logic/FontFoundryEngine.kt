package com.otso.app.logic

import android.app.Application
import android.content.Intent
import android.graphics.Matrix
import android.graphics.Typeface
import android.net.Uri
import android.util.LruCache
import com.otso.app.core.FontManager
import com.otso.app.core.OtsoPreferences
import com.otso.app.viewmodel.EditorUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
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
            android.util.Log.d(FOUNDRY_TAG, "[FLOW] initializeFontFlow: coroutine started")
            OtsoPreferences.folderFoundryUriFlow(application)
                .distinctUntilChanged()
                .collect { uriString ->
                android.util.Log.i(FOUNDRY_TAG, "[FLOW-1] collect fired: uri=${uriString?.take(80) ?: "<null>"}, isFontLoading=${_uiState.value.isFontLoading}")
                try {
                    if (uriString.isNullOrBlank()) {
                        android.util.Log.d(FOUNDRY_TAG, "[FLOW-1a] uri is null/blank → clearing foundry state")
                        _uiState.update {
                            it.copy(
                                foundryFolderUri = null,
                                font = it.font.copy(
                                    activeFoundryFamily = null,
                                    activeFoundryVariantCount = 0,
                                ),
                                activeFoundryFamilyName = null,
                                isFontInitialized = true,
                                isFontLoading = false,
                            )
                        }
                        return@collect
                    }

                    _uiState.update { it.copy(foundryFolderUri = uriString, isFontLoading = true) }
                    android.util.Log.i(FOUNDRY_TAG, "[FLOW-2] isFontLoading=true set, parsing URI")

                    val parsedUri = runCatching { Uri.parse(uriString) }.getOrNull()
                    if (parsedUri == null) {
                        android.util.Log.e(FOUNDRY_TAG, "[FLOW-2a] Uri.parse failed for: $uriString")
                        _uiState.update {
                            it.copy(
                                font = it.font.copy(
                                    activeFoundryFamily = null,
                                    activeFoundryVariantCount = 0,
                                ),
                                activeFoundryFamilyName = null,
                                fileAccessError = "Stored font folder URI is invalid.",
                                isFontInitialized = true,
                                isFontLoading = false,
                            )
                        }
                        return@collect
                    }

                    android.util.Log.d(FOUNDRY_TAG, "[FLOW-3] checking persisted URI permission for: $parsedUri")
                    val hasPermission = hasPersistedUriPermission(parsedUri)
                    android.util.Log.i(FOUNDRY_TAG, "[FLOW-3] hasPersistedUriPermission=$hasPermission")
                    val allPersistedUris = application.contentResolver.persistedUriPermissions
                        .map { "${it.uri}  read=${it.isReadPermission}" }
                    android.util.Log.d(FOUNDRY_TAG, "[FLOW-3] all persisted URIs (${allPersistedUris.size}): ${allPersistedUris.joinToString(" | ")}")

                    if (!hasPermission) {
                        android.util.Log.w(FOUNDRY_TAG, "[FLOW-3a] permission lost → clearing pref and state")
                        _uiState.update {
                            it.copy(
                                font = it.font.copy(
                                    activeFoundryFamily = null,
                                    activeFoundryVariantCount = 0,
                                ),
                                activeFoundryFamilyName = null,
                                foundryFolderUri = null,
                                fileAccessError = "Font folder access expired. Please re-select your font folder.",
                                isFontInitialized = true,
                                isFontLoading = false,
                            )
                        }
                        OtsoPreferences.setFolderFoundryUri(application, null)
                        return@collect
                    }

                    android.util.Log.i(FOUNDRY_TAG, "[FLOW-4] starting FontManager.scanFolderForFamilies")
                    val startTime = System.currentTimeMillis()
                    val foundries = withContext(Dispatchers.IO) {
                        try {
                            val result = FontManager.scanFolderForFamilies(parsedUri)
                            android.util.Log.i(FOUNDRY_TAG, "[FLOW-IO] scan returned ${result.size} families: ${result.map { it.name }}")
                            result
                        } catch (e: Exception) {
                            android.util.Log.e(FOUNDRY_TAG, "[FLOW-IO] scan EXCEPTION (${e.javaClass.simpleName}): ${e.message}", e)
                            emptyList()
                        }
                    }

                    val elapsed = System.currentTimeMillis() - startTime
                    android.util.Log.i(FOUNDRY_TAG, "[FLOW-5] scan finished in ${elapsed}ms. families=${foundries.size}, isFontInitialized=${_uiState.value.isFontInitialized}")
                    if (elapsed < 600L && _uiState.value.isFontInitialized) {
                        android.util.Log.d(FOUNDRY_TAG, "[FLOW-5] stability delay: ${600L - elapsed}ms")
                        kotlinx.coroutines.delay(600L - elapsed)
                    }

                    val firstFamily = foundries.firstOrNull()
                    android.util.Log.i(FOUNDRY_TAG, "[FLOW-6] applying scan result: family=${firstFamily?.name}, variants=${firstFamily?.variantCount} → isFontLoading=false")
                    _uiState.update { state ->
                        state.copy(
                            font = state.font.copy(
                                activeFoundryFamily = firstFamily?.composeFamily,
                                activeFoundryVariantCount = firstFamily?.variantCount ?: 0,
                            ),
                            activeFoundryFamilyName = firstFamily?.name,
                            customFontName = firstFamily?.name ?: state.customFontName,
                            fileAccessError = if (firstFamily == null && foundries.isEmpty()) {
                                "No supported font families found in selected folder."
                            } else {
                                null
                            },
                            isFontInitialized = true,
                            isFontLoading = false,
                        )
                    }
                    android.util.Log.i(FOUNDRY_TAG, "[FLOW-6] state update done. isFontLoading=${_uiState.value.isFontLoading}")
                } catch (e: Exception) {
                    android.util.Log.e(FOUNDRY_TAG, "[FLOW-FATAL] uncaught exception in collect block (${e.javaClass.simpleName}): ${e.message}", e)
                    _uiState.update { it.copy(isFontLoading = false, isFontInitialized = true, fileAccessError = "Font loading failed: ${e.message}") }
                }
            }
        }
    }

    fun setFoundryFolder(uri: Uri) {
        val uriString = uri.toString()
        android.util.Log.i(FOUNDRY_TAG, "[SET-1] setFoundryFolder called: uri=${uriString.take(80)}")
        android.util.Log.d(FOUNDRY_TAG, "[SET-1] stateUri=${_uiState.value.foundryFolderUri?.take(80)}, isFontLoading=${_uiState.value.isFontLoading}")

        _uiState.update { it.copy(isFontLoading = true) }
        scope.launch {
            try {
                // Read from prefs (not state): state can be wiped by ViewModel init race,
                // causing a false "new URI" path when the URI is already stored in DataStore.
                // If we write the same value DataStore already holds, it won't emit → flow
                // never fires → isFontLoading stuck true forever.
                val currentPrefsUri = withContext(Dispatchers.IO) {
                    OtsoPreferences.folderFoundryUriFlow(application).first()
                }
                android.util.Log.i(FOUNDRY_TAG, "[SET-2] currentPrefsUri=${currentPrefsUri?.take(80)}, sameAsSelected=${uriString == currentPrefsUri}")

                if (uriString == currentPrefsUri) {
                    // URI already stored in prefs → distinctUntilChanged() on initializeFontFlow
                    // would suppress the DataStore re-emission → must rescan directly.
                    android.util.Log.i(FOUNDRY_TAG, "[SET-3] same URI in prefs → direct rescan")
                    val startTime = System.currentTimeMillis()
                    val foundries = withContext(Dispatchers.IO) {
                        try {
                            val result = FontManager.scanFolderForFamilies(uri)
                            android.util.Log.i(FOUNDRY_TAG, "[SET-RESCAN-IO] scan returned ${result.size} families")
                            result
                        } catch (e: Exception) {
                            android.util.Log.e(FOUNDRY_TAG, "[SET-RESCAN-IO] scan EXCEPTION (${e.javaClass.simpleName}): ${e.message}", e)
                            emptyList()
                        }
                    }
                    val elapsed = System.currentTimeMillis() - startTime
                    android.util.Log.i(FOUNDRY_TAG, "[SET-RESCAN] done in ${elapsed}ms, families=${foundries.size}")
                    if (elapsed < 600L) kotlinx.coroutines.delay(600L - elapsed)
                    val firstFamily = foundries.firstOrNull()
                    _uiState.update { state ->
                        state.copy(
                            foundryFolderUri = uriString,
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
                            isFontLoading = false,
                        )
                    }
                    android.util.Log.i(FOUNDRY_TAG, "[SET-RESCAN] done. isFontLoading=false, family=${firstFamily?.name}")
                } else {
                    // New (or first-time) URI → persist SAF permission, write to prefs.
                    // initializeFontFlow will receive the new emission and handle the scan.
                    android.util.Log.i(FOUNDRY_TAG, "[SET-4] new URI → persisting SAF permission")
                    persistFoundryUriPermission(uri)
                    val permOk = hasPersistedUriPermission(uri)
                    android.util.Log.i(FOUNDRY_TAG, "[SET-5] hasPersistedUriPermission=$permOk")
                    val allPersistedUris = application.contentResolver.persistedUriPermissions
                        .map { "${it.uri}  read=${it.isReadPermission}" }
                    android.util.Log.d(FOUNDRY_TAG, "[SET-5] all persisted URIs (${allPersistedUris.size}): ${allPersistedUris.joinToString(" | ")}")

                    if (permOk) {
                        android.util.Log.i(FOUNDRY_TAG, "[SET-6] permission OK → saving to prefs (initializeFontFlow will scan)")
                        OtsoPreferences.setFolderFoundryUri(application, uriString)
                        OtsoPreferences.setCustomFontPath(application, null)
                        OtsoPreferences.setCustomFontName(application, null)
                    } else {
                        android.util.Log.e(FOUNDRY_TAG, "[SET-6a] permission NOT persisted → isFontLoading=false")
                        _uiState.update {
                            it.copy(
                                isFontLoading = false,
                                fileAccessError = "Unable to persist font folder access. Please try again.",
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e(FOUNDRY_TAG, "[SET-FATAL] (${e.javaClass.simpleName}): ${e.message}", e)
                _uiState.update { it.copy(isFontLoading = false, fileAccessError = "Font setup failed: ${e.message}") }
            }
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

    /**
     * Checks whether the app still holds a persisted read permission for the given URI.
     * This prevents SecurityException crashes when the user revokes access or reinstalls the app.
     */
    private fun hasPersistedUriPermission(uri: Uri): Boolean {
        val persistedPermissions = application.contentResolver.persistedUriPermissions
        return persistedPermissions.any { perm ->
            perm.uri == uri && perm.isReadPermission
        }
    }
}
