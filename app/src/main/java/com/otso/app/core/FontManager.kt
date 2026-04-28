package com.otso.app.core

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.util.Locale

object FontManager {

    private const val TAG = "OtsoFoundry"
    private const val STAGED_FONTS_DIR = "foundry_staged_fonts"

    @Volatile
    private var appContext: Context? = null

    data class DetectedFontFamily(
        val name: String,
        val composeFamily: FontFamily,
        val variantCount: Int,
    )

    private data class FontCandidate(
        val fileUri: Uri,
        val fileName: String,
        val familyName: String,
        val weight: Int,
        val italic: Boolean,
    )

    private data class ParsedFontName(
        val familyName: String,
        val weight: Int,
        val italic: Boolean,
    )

    private data class BuiltFamily(
        val family: FontFamily,
        val loadedFonts: Int,
    )

    fun bindContext(context: Context) {
        appContext = context.applicationContext
    }

    fun scanFolderForFamilies(uri: Uri): List<DetectedFontFamily> {
        val context = requireNotNull(appContext) { "FontManager context is not bound." }
        return scanFolderForFamilies(context, uri)
    }

    fun scanFolderForFamilies(
        context: Context,
        uri: Uri,
    ): List<DetectedFontFamily> {
        val root = DocumentFile.fromTreeUri(context, uri) ?: return emptyList()
        if (!root.isDirectory) return emptyList()

        val fontFiles = collectFontFiles(root)
        fontFiles.forEach { file ->
            Log.d(TAG, "Found file in folder: name=${file.name ?: "<unnamed>"} uri=${file.uri}")
        }

        val candidates = fontFiles
            .mapNotNull { file ->
                val rawName = file.name ?: return@mapNotNull null
                val parsed = parseFontName(rawName) ?: return@mapNotNull null
                Log.d(
                    TAG,
                    "File '$rawName' mapped => family='${parsed.familyName}', weight=${parsed.weight}, bold=${parsed.weight >= 600}, italic=${parsed.italic}",
                )
                FontCandidate(
                    fileUri = file.uri,
                    fileName = rawName,
                    familyName = parsed.familyName,
                    weight = parsed.weight,
                    italic = parsed.italic,
                )
            }

        if (candidates.isEmpty()) return emptyList()

        return candidates
            .groupBy { it.familyName.lowercase(Locale.US) }
            .values
            .mapNotNull { grouped ->
                val built = buildComposeFamily(context, grouped) ?: return@mapNotNull null
                val displayName = grouped.firstOrNull()?.familyName ?: return@mapNotNull null
                Log.d(
                    TAG,
                    "Generated family '$displayName' with ${built.loadedFonts} fonts from ${grouped.size} mapped files",
                )
                DetectedFontFamily(
                    name = displayName,
                    composeFamily = built.family,
                    variantCount = built.loadedFonts,
                )
            }
            .sortedBy { it.name.lowercase(Locale.US) }
    }

    private fun collectFontFiles(root: DocumentFile): List<DocumentFile> {
        val queue = ArrayDeque<DocumentFile>()
        val results = mutableListOf<DocumentFile>()
        queue.add(root)

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            current.listFiles().forEach { doc ->
                when {
                    doc.isDirectory -> queue.add(doc)
                    doc.isFile && isSupportedFontName(doc.name) -> results.add(doc)
                }
            }
        }
        return results
    }

    private fun isSupportedFontName(name: String?): Boolean {
        if (name.isNullOrBlank()) return false
        val lower = name.lowercase(Locale.US)
        return lower.endsWith(".ttf") || lower.endsWith(".otf") || lower.endsWith(".ttc") || lower.endsWith(".otc")
    }

    private fun parseFontName(fileName: String): ParsedFontName? {
        val basename = fileName.substringBeforeLast('.', missingDelimiterValue = fileName).trim()
        if (basename.isBlank()) return null

        val normalizedForTokens = basename
            .replace(Regex("([a-z])([A-Z])"), "$1 $2")
            .replace(Regex("([A-Z]+)([A-Z][a-z])"), "$1 $2")
            .replace(Regex("[^A-Za-z0-9]+"), " ")
            .trim()
            .lowercase(Locale.US)
        if (normalizedForTokens.isBlank()) return null

        val tokens = normalizedForTokens
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .toMutableList()
        if (tokens.isEmpty()) return null

        var endIndex = tokens.lastIndex
        var italic = false
        var weight: Int? = null

        while (endIndex >= 0) {
            val token = tokens[endIndex]
            val compound = parseCompoundWeightItalic(token)
            if (compound != null) {
                if (weight == null) weight = compound.first
                italic = italic || compound.second
                endIndex--
                continue
            }

            val tokenWeight = parseWeightToken(token)
            if (tokenWeight != null) {
                if (weight == null) weight = tokenWeight
                endIndex--
                continue
            }

            if (isItalicToken(token)) {
                italic = true
                endIndex--
                continue
            }

            break
        }

        val familyTokens = if (endIndex >= 0) tokens.subList(0, endIndex + 1) else emptyList()
        val normalizedFamily = familyTokens.joinToString(" ")
            .replace(Regex("\\s+"), " ")
            .trim()
            .ifBlank {
                basename
                    .replace(Regex("[_\\-]+"), " ")
                    .replace(Regex("\\s+"), " ")
                    .trim()
            }
        return ParsedFontName(
            familyName = normalizedFamily,
            weight = weight ?: 400,
            italic = italic,
        )
    }

    private fun parseCompoundWeightItalic(token: String): Pair<Int, Boolean>? {
        val normalized = token
            .lowercase(Locale.US)
            .replace(Regex("[^a-z0-9]"), "")
        if (normalized.isBlank()) return null

        val italic = normalized.contains("italic") ||
            normalized.contains("oblique") ||
            normalized == "bdit" ||
            normalized == "itbd" ||
            normalized == "boldit" ||
            normalized == "itbold" ||
            normalized == "sbit" ||
            normalized == "itsb"

        val weight = when {
            normalized.contains("extrabold") || normalized.contains("ultrabold") || normalized.contains("xbold") -> 800
            normalized.contains("semibold") || normalized.contains("demibold") -> 600
            normalized.contains("bold") || normalized.startsWith("bd") -> 700
            normalized.contains("medium") -> 500
            normalized.contains("light") || normalized.contains("book") -> 300
            normalized.contains("black") || normalized.contains("heavy") -> 900
            normalized.contains("thin") -> 100
            normalized.contains("regular") || normalized.contains("normal") -> 400
            else -> null
        }

        return if (italic || weight != null) {
            (weight ?: 400) to italic
        } else {
            null
        }
    }

    private fun parseWeightToken(token: String): Int? {
        return when (token.lowercase(Locale.US)) {
            "thin", "hairline" -> 100
            "extralight", "ultralight", "xlight" -> 200
            "light", "book", "lgt" -> 300
            "regular", "normal", "reg", "roman" -> 400
            "medium", "med", "md" -> 500
            "semibold", "demibold", "semi", "demi", "sb" -> 600
            "bold", "bd" -> 700
            "extrabold", "ultrabold", "xbold", "xb" -> 800
            "black", "heavy", "blk" -> 900
            else -> null
        }
    }

    private fun isItalicToken(token: String): Boolean {
        return when (token.lowercase(Locale.US)) {
            "italic", "ital", "it", "oblique", "obl", "slanted", "slant" -> true
            else -> false
        }
    }

    private fun stageFontFile(
        context: Context,
        candidate: FontCandidate,
    ): File? {
        val stagingDir = File(context.cacheDir, STAGED_FONTS_DIR)
        if (!stagingDir.exists() && !stagingDir.mkdirs()) {
            Log.d(TAG, "Failed to create staging dir: ${stagingDir.absolutePath}")
            return null
        }

        val extension = candidate.fileName.substringAfterLast('.', "font")
            .lowercase(Locale.US)
        val id = candidate.fileUri.toString().hashCode().toUInt().toString(16)
        val staged = File(stagingDir, "$id.$extension")

        if (staged.exists() && staged.length() > 0L) {
            return staged
        }

        return runCatching {
            context.contentResolver.openInputStream(candidate.fileUri)?.use { input ->
                staged.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return null
            staged
        }.onFailure {
            Log.d(TAG, "Failed staging '${candidate.fileName}' from ${candidate.fileUri}: ${it.message}")
            staged.delete()
        }.getOrNull()
    }

    private fun buildComposeFamily(
        context: Context,
        grouped: List<FontCandidate>,
    ): BuiltFamily? {
        if (grouped.isEmpty()) return null

        val orderedUnique = grouped
            .sortedWith(compareBy<FontCandidate>({ it.weight }, { it.italic }))
            .distinctBy { it.weight to it.italic to it.fileUri.toString() }

        val composeFonts = orderedUnique.mapNotNull { candidate ->
            val staged = stageFontFile(context, candidate) ?: run {
                Log.d(TAG, "Skipping ${candidate.fileName}: staging failed")
                return@mapNotNull null
            }
            runCatching {
                Font(
                    file = staged,
                    weight = FontWeight(candidate.weight),
                    style = if (candidate.italic) FontStyle.Italic else FontStyle.Normal,
                )
            }.onFailure {
                Log.d(TAG, "Skipping ${candidate.fileName}: compose Font() failed (${it.message})")
            }.getOrNull()
        }

        if (composeFonts.isEmpty()) {
            return null
        }

        return BuiltFamily(
            family = FontFamily(*composeFonts.toTypedArray()),
            loadedFonts = composeFonts.size,
        )
    }
}
