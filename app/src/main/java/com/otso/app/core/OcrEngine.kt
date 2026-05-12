package com.otso.app.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import com.google.android.gms.tasks.Task
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.max

object OcrEngine {

    enum class EngineMode {
        MLKIT_BASELINE,
        MLKIT_PREPROCESSED,
        MLKIT_MULTISCALE,
        MLKIT_LINEBOOST,
        MLKIT_HYBRID,
        NEURAL_BOOST,
        HANDWRITING,
    }

    data class OcrOutput(
        val text: String,
        val engineUsed: String,
        val visionStatus: NeuralVisionEngine.RuntimeStatus = NeuralVisionEngine.runtimeStatus,
    )

    @Volatile
    var mode: EngineMode = EngineMode.MLKIT_HYBRID

    enum class ScriptType {
        AUTO, LATIN, CHINESE, JAPANESE, KOREAN, DEVANAGARI
    }

    @Volatile
    var targetScript: ScriptType = ScriptType.AUTO

    private data class SemanticRule(val pattern: String, val replacement: String, val category: String)
    private var semanticRules: List<SemanticRule>? = null

    private fun loadRulesIfNeeded(context: Context) {
        if (semanticRules != null) return
        try {
            val jsonString = context.assets.open("ocr_cleanup_rules.json").bufferedReader().use { it.readText() }
            val jsonArray = org.json.JSONArray(jsonString)
            val rules = mutableListOf<SemanticRule>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                rules.add(SemanticRule(
                    pattern = obj.getString("pattern"),
                    replacement = obj.getString("replacement"),
                    category = obj.getString("category")
                ))
            }
            semanticRules = rules
        } catch (e: Exception) {
            semanticRules = emptyList()
        }
    }

    suspend fun extract(context: Context, uri: Uri): OcrOutput {
        loadRulesIfNeeded(context)
        NeuralVisionEngine.loadModel(context)
        val rawOutput = when (mode) {
            EngineMode.MLKIT_BASELINE -> OcrOutput(runMlKit(context, uri), "mlkit-baseline")
            EngineMode.MLKIT_PREPROCESSED -> OcrOutput(runMlKitPreprocessed(context, uri), "mlkit-preprocessed")
            EngineMode.MLKIT_MULTISCALE -> OcrOutput(runMlKitMultiScale(context, uri), "mlkit-multiscale")
            EngineMode.MLKIT_LINEBOOST -> OcrOutput(runMlKitLineBoost(context, uri), "mlkit-lineboost")
            EngineMode.MLKIT_HYBRID -> runHybrid(context, uri)
            EngineMode.NEURAL_BOOST -> {
                val text = runNeuralBoost(context, uri)
                OcrOutput(text, neuralEngineLabel(), NeuralVisionEngine.runtimeStatus)
            }
            EngineMode.HANDWRITING -> {
                // Handwriting benefits from higher resolution and less aggressive binarization
                val bitmap = withContext(Dispatchers.IO) { decodeBitmap(context, uri) } ?: return OcrOutput("", "none")
                val upscaled = upscaleIfNeeded(bitmap, minWidth = 2000)
                val image = InputImage.fromBitmap(upscaled, 0)
                OcrOutput(recognize(image), "handwriting-specialized")
            }
        }
        
        return rawOutput.copy(text = deepPostProcess(context, rawOutput.text))
    }

    private suspend fun deepPostProcess(context: Context, text: String): String {
        if (text.isBlank()) return text
        return IntelligenceEngine.extractAndFormat(context, text)
    }

    suspend fun extractText(context: Context, uri: Uri): String {
        return extract(context, uri).text
    }

    private suspend fun runHybrid(context: Context, uri: Uri): OcrOutput {
        val baseline = runCatching { OcrOutput(runMlKit(context, uri), "mlkit-baseline") }.getOrNull()
        val preprocessed = runCatching { OcrOutput(runMlKitPreprocessed(context, uri), "mlkit-preprocessed") }.getOrNull()
        val neuralBoost = runCatching {
            val text = runNeuralBoost(context, uri)
            OcrOutput(text, neuralEngineLabel(), NeuralVisionEngine.runtimeStatus)
        }.getOrNull()
        val multiscale = runCatching { OcrOutput(runMlKitMultiScale(context, uri), "mlkit-multiscale") }.getOrNull()
        val lineBoost = runCatching { OcrOutput(runMlKitLineBoost(context, uri), "mlkit-lineboost") }.getOrNull()

        // DNA: Prioritize Neural Boost as the highest quality baseline
        val primary = listOfNotNull(neuralBoost, baseline, preprocessed)
            .maxByOrNull { qualityScore(it.text) }
        if (primary != null && primary.text.isNotBlank()) return primary

        val fallback = listOfNotNull(multiscale, lineBoost)
            .maxByOrNull { qualityScore(it.text) }
        return fallback ?: OcrOutput("", "none")
    }

    private fun neuralEngineLabel(): String {
        val status = NeuralVisionEngine.runtimeStatus
        return if (status == NeuralVisionEngine.RuntimeStatus.MODEL_READY) {
            "neural-boost"
        } else {
            "neural-boost-degraded-heuristic:${status.name.lowercase()}"
        }
    }

    private suspend fun runMlKit(context: Context, uri: Uri): String {
        val image = InputImage.fromFilePath(context, uri)
        return recognize(image)
    }

    private suspend fun runMlKitPreprocessed(context: Context, uri: Uri): String {
        val bitmap = withContext(Dispatchers.IO) { decodeBitmap(context, uri) } ?: return ""
        val cleaned = withContext(Dispatchers.Default) { preprocess(bitmap) }
        val image = InputImage.fromBitmap(cleaned, 0)
        
        return recognize(image)
    }

    private suspend fun runNeuralBoost(context: Context, uri: Uri): String {
        val bitmap = withContext(Dispatchers.IO) { decodeBitmap(context, uri) } ?: return ""
        val boosted = withContext(Dispatchers.Default) { NeuralVisionEngine.neuralBoost(bitmap) }
        val image = InputImage.fromBitmap(boosted, 0)
        return recognize(image)
    }

    private suspend fun runMlKitMultiScale(context: Context, uri: Uri): String {
        val bitmap = withContext(Dispatchers.IO) { decodeBitmap(context, uri) } ?: return ""
        val variants = withContext(Dispatchers.Default) { buildMultiscaleVariants(bitmap) }
        if (variants.isEmpty()) return ""

        var best = ""
        var bestScore = Int.MIN_VALUE
        for (candidate in variants) {
            val text = runCatching {
                recognize(InputImage.fromBitmap(candidate, 0))
            }.getOrDefault("")
            val currentScore = qualityScore(text)
            if (currentScore > bestScore) {
                best = text
                bestScore = currentScore
            }
        }
        return best
    }

    private suspend fun runMlKitLineBoost(context: Context, uri: Uri): String {
        val bitmap = withContext(Dispatchers.IO) { decodeBitmap(context, uri) } ?: return ""
        val pre = withContext(Dispatchers.Default) { preprocess(bitmap) }
        val bands = withContext(Dispatchers.Default) { detectTextBands(pre) }
        if (bands.isEmpty()) return ""

        val lines = mutableListOf<String>()
        for (band in bands) {
            val cropped = withContext(Dispatchers.Default) { cropBand(bitmap, band.first, band.second) } ?: continue
            val variants = withContext(Dispatchers.Default) {
                val upscaled = upscaleIfNeeded(cropped, minWidth = 1600)
                val gray = toGrayscale(upscaled)
                val bin = otsuBinarize(gray)
                listOf(upscaled, gray, bin)
            }
            val bestLine = variants.map { variant ->
                runCatching {
                    recognize(InputImage.fromBitmap(variant, 0))
                }.getOrDefault("")
            }.maxByOrNull { qualityScore(it) }.orEmpty()
            if (bestLine.isNotBlank()) lines += bestLine
        }
        return lines.joinToString("\n")
    }

    private suspend fun recognize(image: InputImage): String {
        var currentScript = targetScript
        
        // US-004: Automatic Script Detection
        if (currentScript == ScriptType.AUTO) {
            // Pass 1: Fast Baseline (Latin) to get text for language ID
            val baselineOptions = TextRecognizerOptions.DEFAULT_OPTIONS
            val baselineRecognizer = TextRecognition.getClient(baselineOptions)
            val baselineResult = try {
                baselineRecognizer.process(image).await()
            } finally {
                baselineRecognizer.close()
            }
            
            val detectedLang = IntelligenceEngine.identifyLanguage(baselineResult.text)
            currentScript = mapToScript(detectedLang)
            
            // If it's just Latin, we already have the result
            if (currentScript == ScriptType.LATIN) {
                return reconstructLayout(baselineResult).processOcrResults()
            }
            
            // Otherwise, we need a Second Pass with the correct recognizer
            Log.d("OcrEngine", "Auto-detected script: $currentScript (Lang: $detectedLang). Re-running specialized scan.")
        }

        val options = when (currentScript) {
            ScriptType.CHINESE -> ChineseTextRecognizerOptions.Builder().build()
            ScriptType.JAPANESE -> JapaneseTextRecognizerOptions.Builder().build()
            ScriptType.KOREAN -> KoreanTextRecognizerOptions.Builder().build()
            ScriptType.DEVANAGARI -> DevanagariTextRecognizerOptions.Builder().build()
            else -> TextRecognizerOptions.DEFAULT_OPTIONS
        }
        
        val recognizer = TextRecognition.getClient(options)
        return try {
            val result = recognizer.process(image).await()
            reconstructLayout(result).processOcrResults()
        } finally {
            recognizer.close()
        }
    }

    private fun String.processOcrResults(): String {
        return this.normalizeForScoring().cleanupSemanticNoise().cleanupNoise()
    }

    private fun mapToScript(langCode: String): ScriptType {
        return when (langCode) {
            "zh" -> ScriptType.CHINESE
            "ja" -> ScriptType.JAPANESE
            "ko" -> ScriptType.KOREAN
            "hi", "mr", "ne" -> ScriptType.DEVANAGARI
            else -> ScriptType.LATIN
        }
    }

    private fun reconstructLayout(visionText: com.google.mlkit.vision.text.Text): String {
        if (visionText.textBlocks.isEmpty()) return ""

        // DNA: Grid+ v2 (Elastic Tabulation)
        // 1. Flatten all elements and filter noise
        val allElements = visionText.textBlocks.flatMap { it.lines }.flatMap { it.elements }
            .filter { it.text.isNotBlank() }
        if (allElements.isEmpty()) return ""

        // 2. Row Detection (Horizontal Profiling)
        val rows = mutableListOf<MutableList<com.google.mlkit.vision.text.Text.Element>>()
        val sortedByTop = allElements.sortedBy { it.boundingBox?.top ?: 0 }
        
        for (element in sortedByTop) {
            val elBox = element.boundingBox ?: continue
            val elCenterY = (elBox.top + elBox.bottom) / 2
            
            val matchingRow = rows.find { row ->
                val rTop = row.minOf { it.boundingBox?.top ?: Int.MAX_VALUE }
                val rBottom = row.maxOf { it.boundingBox?.bottom ?: Int.MIN_VALUE }
                val rHeight = (rBottom - rTop).coerceAtLeast(1)
                
                // Allow 50% overlap or proximity within half-height
                val overlap = maxOf(0, minOf(elBox.bottom, rBottom) - maxOf(elBox.top, rTop))
                overlap > rHeight * 0.4 || elCenterY in rTop..rBottom
            }
            
            if (matchingRow != null) matchingRow.add(element)
            else rows.add(mutableListOf(element))
        }

        // 3. Column/Gutter Detection (Vertical Profiling)
        // We project the horizontal spans of all elements to find consistent vertical gaps
        val docWidth = allElements.maxOf { it.boundingBox?.right ?: 0 }
        val occupancy = IntArray(docWidth + 1)
        for (el in allElements) {
            val box = el.boundingBox ?: continue
            for (x in box.left.coerceAtLeast(0)..box.right.coerceAtMost(docWidth)) {
                occupancy[x]++
            }
        }

        // Detect Columns based on "Vertical Gutters" (valleys in occupancy)
        val columns = mutableListOf<Int>()
        var inColumn = false
        val threshold = (rows.size * 0.05).toInt().coerceAtLeast(1)
        
        for (x in 0..docWidth) {
            val isBusy = occupancy[x] >= threshold
            if (isBusy && !inColumn) {
                columns.add(x)
                inColumn = true
            } else if (!isBusy && inColumn) {
                inColumn = false
            }
        }

        // 4. Elastic Assembly
        val resultText = StringBuilder()
        for (row in rows.sortedBy { it.minOf { el -> el.boundingBox?.top ?: 0 } }) {
            val sortedRow = row.sortedBy { it.boundingBox?.left ?: 0 }
            val rowText = StringBuilder()
            var currentPos = 0
            
            for (element in sortedRow) {
                val elBox = element.boundingBox ?: continue
                
                // Find which column this element belongs to
                val colIdx = columns.indexOfLast { it <= elBox.left }.coerceAtLeast(0)
                val targetCharPos = colIdx * 20 // Fixed width columns for visual alignment
                
                val spacesNeeded = (targetCharPos - currentPos).coerceAtLeast(if (currentPos == 0) 0 else 1)
                rowText.append(" ".repeat(spacesNeeded))
                rowText.append(element.text)
                currentPos = targetCharPos + element.text.length + spacesNeeded
            }
            
            if (resultText.isNotEmpty()) resultText.append("\n")
            resultText.append(rowText)
        }

        return resultText.toString()
    }

    private fun qualityScore(text: String): Int {
        if (text.isBlank()) return 0
        val letters = text.count { it.isLetter() }
        val digits = text.count { it.isDigit() }
        val spaces = text.count { it.isWhitespace() }
        val newLines = text.count { it == '\n' }
        val characters = text.length.coerceAtLeast(1)
        val symbols = characters - letters - digits - spaces
        val unique = text.toSet().size
        val words = text.split(Regex("\\s+")).filter { it.isNotBlank() }
        val shortAlpha = words.count { token ->
            token.length <= 2 && token.any(Char::isLetter) && token.all { it.isLetter() }
        }
        val avgWordLen = if (words.isEmpty()) 0 else words.sumOf { it.length } / words.size
        val alphaRatio = ((letters + digits) * 100) / characters
        
        // Base score favors content over noise
        var score = (letters * 3) + (digits * 2) + spaces + symbols + (newLines * 4) + unique + alphaRatio + avgWordLen
        
        // Dynamic penalty for noise - reduced if borders/patterns are detected
        val noise = noiseScore(text)
        score -= (noise * 2) 
        
        // Reduced penalty for short words like "at", "to", "PP"
        score -= (shortAlpha * 2)
        
        return score
    }

    private fun noiseScore(text: String): Int {
        if (text.isBlank()) return 100
        val total = text.length.coerceAtLeast(1)
        val lines = text.split('\n')
        
        // Detect decorative border lines (e.g. @@@@@@@, #######, I I I I I)
        val borderLines = lines.count { line ->
            val trimmed = line.trim()
            if (trimmed.length < 8) return@count false
            
            val symbols = trimmed.count { !it.isLetterOrDigit() && !it.isWhitespace() }
            val symbolRatio = symbols.toFloat() / trimmed.length.toFloat()
            
            // Check for repetitive single characters (even letters like I or O)
            val uniqueChars = trimmed.replace(" ", "").toSet()
            val isRepetitive = uniqueChars.size <= 2 && trimmed.length > 10
            
            symbolRatio > 0.7f || isRepetitive
        }

        val symbols = text.count { !it.isLetterOrDigit() && !it.isWhitespace() }
        val symbolRatio = (symbols * 100) / total
        val tinyTokens = text.split(Regex("\\s+")).count { it.length == 1 }
        
        // Repeated runs like "@@@@" are often structures, not noise in this project
        val repeatedRuns = Regex("(.)\\1{3,}").findAll(text).count()
        val structuralDiscount = (borderLines * 25) + (repeatedRuns * 5)
        
        val baseNoise = symbolRatio + tinyTokens + (repeatedRuns * 4)
        return (baseNoise - structuralDiscount).coerceAtLeast(0)
    }

    private fun preprocess(input: Bitmap): Bitmap {
        val oriented = input
        val scaled = upscaleIfNeeded(oriented, minWidth = 1280)
        val sharp = sharpenBitmap(scaled)
        val gray = toGrayscale(sharp)
        // Revert to Otsu as primary - it's faster and cleaner for standard cases
        val bin = otsuBinarize(gray)
        return bin
    }

    private fun detectTextBands(binary: Bitmap): List<Pair<Int, Int>> {
        val width = binary.width
        val height = binary.height
        if (width <= 0 || height <= 0) return emptyList()

        val rowInk = IntArray(height)
        val pixels = IntArray(width)
        for (y in 0 until height) {
            binary.getPixels(pixels, 0, width, 0, y, width, 1)
            var ink = 0
            for (p in pixels) {
                if (Color.red(p) < 160) ink++
            }
            rowInk[y] = ink
        }

        val threshold = (width * 0.015f).toInt().coerceAtLeast(4)
        val minBandHeight = (height * 0.01f).toInt().coerceAtLeast(12)
        val margin = (height * 0.004f).toInt().coerceAtLeast(3)
        val bands = mutableListOf<Pair<Int, Int>>()

        var start = -1
        for (y in 0 until height) {
            val isText = rowInk[y] >= threshold
            if (isText && start == -1) start = y
            if (!isText && start != -1) {
                val end = y - 1
                if (end - start + 1 >= minBandHeight) {
                    bands += (start - margin).coerceAtLeast(0) to (end + margin).coerceAtMost(height - 1)
                }
                start = -1
            }
        }
        if (start != -1) {
            val end = height - 1
            if (end - start + 1 >= minBandHeight) {
                bands += (start - margin).coerceAtLeast(0) to end
            }
        }
        return bands.take(64)
    }

    private fun cropBand(source: Bitmap, startY: Int, endY: Int): Bitmap? {
        val safeStart = startY.coerceIn(0, source.height - 1)
        val safeEnd = endY.coerceIn(safeStart, source.height - 1)
        val h = (safeEnd - safeStart + 1).coerceAtLeast(1)
        return runCatching {
            Bitmap.createBitmap(source, 0, safeStart, source.width, h)
        }.getOrNull()
    }

    private fun buildMultiscaleVariants(input: Bitmap): List<Bitmap> {
        val variants = mutableListOf<Bitmap>()
        val scaledA = upscaleIfNeeded(input, minWidth = 1280)
        val scaledB = upscaleIfNeeded(input, minWidth = 1800)
        val grayA = toGrayscale(scaledA)
        val grayB = toGrayscale(scaledB)
        
        // Final Boss: Contrast Boost
        val contrastA = adjustContrast(grayA, 1.25f)
        val contrastB = adjustContrast(grayB, 1.5f)
        
        val otsuA = otsuBinarize(contrastA)
        val otsuB = otsuBinarize(contrastB)
        val adaptedA = adaptiveBinarize(contrastA)
        val invertedA = invertBitmap(otsuA)
        
        variants += listOf(scaledA, grayA, contrastA, otsuA, scaledB, grayB, contrastB, otsuB, adaptedA, invertedA)
        return variants
    }

    private fun String.cleanupNoise(): String {
        return this.split('\n').filter { line ->
            val trimmed = line.trim()
            if (trimmed.isEmpty()) return@filter false
            if (trimmed.length < 5) return@filter true // Keep short valid words
            
            val alphaDigits = trimmed.count { it.isLetterOrDigit() }
            val ratio = alphaDigits.toFloat() / trimmed.length.toFloat()
            
            // Discard lines that are mostly symbols (borders)
            if (ratio < 0.6f) return@filter false
            
            // Discard highly repetitive "letter borders" like IIIIIII or OOOOOO
            val contentChars = trimmed.replace(Regex("[\\s\\p{Punct}]"), "")
            val uniqueChars = contentChars.toSet()
            if (uniqueChars.size <= 1 && contentChars.length > 5) return@filter false
            
            // Discard lines with too many single-character tokens (ASCII art noise)
            val tokens = trimmed.split(Regex("\\s+")).filter { it.isNotBlank() }
            val singleCharTokens = tokens.count { it.length == 1 }
            if (singleCharTokens > 3 && singleCharTokens.toFloat() / tokens.size.toFloat() > 0.6f) return@filter false
            
            // Final check: if the line has very few characters but many spaces
            if (trimmed.length > 10 && trimmed.count { it == ' ' } > trimmed.length / 2) return@filter false
            
            true
        }.joinToString("\n")
    }

    private fun decodeBitmap(context: Context, uri: Uri): Bitmap? {
        val raw = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) } ?: return null
        val rotation = context.contentResolver.openInputStream(uri)?.use { stream ->
            runCatching { ExifInterface(stream).rotationDegrees }.getOrDefault(0)
        } ?: 0
        if (rotation == 0) return raw
        val matrix = android.graphics.Matrix().apply { postRotate(rotation.toFloat()) }
        return Bitmap.createBitmap(raw, 0, 0, raw.width, raw.height, matrix, true)
    }

    private fun upscaleIfNeeded(source: Bitmap, minWidth: Int): Bitmap {
        if (source.width >= minWidth) return source
        val ratio = minWidth.toFloat() / source.width.toFloat()
        val targetW = max(minWidth, (source.width * ratio).toInt())
        val targetH = max(1, (source.height * ratio).toInt())
        return Bitmap.createScaledBitmap(source, targetW, targetH, true)
    }

    private fun toGrayscale(source: Bitmap): Bitmap {
        val out = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val paint = Paint()
        val matrix = android.graphics.ColorMatrix().apply { setSaturation(0f) }
        paint.colorFilter = android.graphics.ColorMatrixColorFilter(matrix)
        canvas.drawBitmap(source, 0f, 0f, paint)
        return out
    }

    private fun otsuBinarize(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        val hist = IntArray(256)
        for (p in pixels) {
            val g = Color.red(p)
            hist[g]++
        }

        val threshold = otsuThreshold(hist, pixels.size)
        for (i in pixels.indices) {
            val g = Color.red(pixels[i])
            pixels[i] = if (g > threshold) Color.WHITE else Color.BLACK
        }

        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        out.setPixels(pixels, 0, width, 0, 0, width, height)
        return out
    }

    private fun otsuThreshold(hist: IntArray, total: Int): Int {
        var sum = 0.0
        for (i in 0..255) {
            sum += i * hist[i]
        }

        var sumB = 0.0
        var wB = 0
        var maxVar = 0.0
        var threshold = 127

        for (t in 0..255) {
            wB += hist[t]
            if (wB == 0) continue
            val wF = total - wB
            if (wF == 0) break

            sumB += (t * hist[t]).toDouble()
            val mB = sumB / wB
            val mF = (sum - sumB) / wF
            val between = wB.toDouble() * wF.toDouble() * (mB - mF) * (mB - mF)
            if (between > maxVar) {
                maxVar = between
                threshold = t
            }
        }
        return threshold
    }

    private fun adjustContrast(source: Bitmap, contrast: Float): Bitmap {
        val out = Bitmap.createBitmap(source.width, source.height, source.config ?: Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val paint = Paint()
        val colorMatrix = ColorMatrix(floatArrayOf(
            contrast, 0f, 0f, 0f, 0f,
            0f, contrast, 0f, 0f, 0f,
            0f, 0f, contrast, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        ))
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(source, 0f, 0f, paint)
        return out
    }

    private fun invertBitmap(source: Bitmap): Bitmap {
        val out = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(source.width * source.height)
        source.getPixels(pixels, 0, source.width, 0, 0, source.width, source.height)
        for (i in pixels.indices) {
            val p = pixels[i]
            val r = 255 - Color.red(p)
            val g = 255 - Color.green(p)
            val b = 255 - Color.blue(p)
            pixels[i] = Color.rgb(r, g, b)
        }
        out.setPixels(pixels, 0, source.width, 0, 0, source.width, source.height)
        return out
    }

    private fun sharpenBitmap(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val paint = Paint()
        
        // Simple Sharpening Convolution Kernel:
        // [ 0, -1,  0]
        // [-1,  5, -1]
        // [ 0, -1,  0]
        val kernel = floatArrayOf(
            0f, -1f, 0f,
            -1f, 5f, -1f,
            0f, -1f, 0f
        )
        
        // In Android, we can't easily do raw convolution without Renderscript/Vulkan/RS replacement
        // so we use a Bitmap overlay approach for a "surgical" sharpen effect
        canvas.drawBitmap(source, 0f, 0f, null)
        
        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)
        val outputPixels = IntArray(width * height)
        
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                var r = 0f
                var g = 0f
                var b = 0f
                
                for (ky in -1..1) {
                    for (kx in -1..1) {
                        val p = pixels[(y + ky) * width + (x + kx)]
                        val k = kernel[(ky + 1) * 3 + (kx + 1)]
                        r += Color.red(p) * k
                        g += Color.green(p) * k
                        b += Color.blue(p) * k
                    }
                }
                
                outputPixels[y * width + x] = Color.rgb(
                    r.toInt().coerceIn(0, 255),
                    g.toInt().coerceIn(0, 255),
                    b.toInt().coerceIn(0, 255)
                )
            }
        }
        out.setPixels(outputPixels, 0, width, 0, 0, width, height)
        return out
    }

    private fun adaptiveBinarize(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)
        
        val output = IntArray(width * height)
        val windowSize = 64
        val offset = 12 // Magic constant for background separation
        
        for (y in 0 until height step windowSize) {
            for (x in 0 until width step windowSize) {
                val winW = minOf(windowSize, width - x)
                val winH = minOf(windowSize, height - y)
                
                var sum = 0L
                for (wy in 0 until winH) {
                    for (wx in 0 until winW) {
                        sum += Color.red(pixels[(y + wy) * width + (x + wx)])
                    }
                }
                val mean = (sum / (winW * winH)).toInt()
                val threshold = mean - offset
                
                for (wy in 0 until winH) {
                    for (wx in 0 until winW) {
                        val idx = (y + wy) * width + (x + wx)
                        output[idx] = if (Color.red(pixels[idx]) > threshold) Color.WHITE else Color.BLACK
                    }
                }
            }
        }
        
        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        out.setPixels(output, 0, width, 0, 0, width, height)
        return out
    }

    private fun String.cleanupSemanticNoise(): String {
        if (this.isBlank()) return ""
        var current = this
        semanticRules?.forEach { rule ->
            current = current.replace(Regex(rule.pattern), rule.replacement)
        }
        return current
    }
}

private suspend fun <T> Task<T>.await(): T =
    suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result -> continuation.resume(result) }
        addOnFailureListener { error -> continuation.resumeWithException(error) }
        addOnCanceledListener { continuation.cancel() }
    }

private fun String.normalizeForScoring(): String {
    return lineSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .joinToString("\n")
}
