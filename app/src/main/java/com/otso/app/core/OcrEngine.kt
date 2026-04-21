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
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
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
    }

    data class OcrOutput(
        val text: String,
        val engineUsed: String,
    )

    @Volatile
    var mode: EngineMode = EngineMode.MLKIT_HYBRID

    suspend fun extract(context: Context, uri: Uri): OcrOutput {
        return when (mode) {
            EngineMode.MLKIT_BASELINE -> OcrOutput(runMlKit(context, uri), "mlkit-baseline")
            EngineMode.MLKIT_PREPROCESSED -> OcrOutput(runMlKitPreprocessed(context, uri), "mlkit-preprocessed")
            EngineMode.MLKIT_MULTISCALE -> OcrOutput(runMlKitMultiScale(context, uri), "mlkit-multiscale")
            EngineMode.MLKIT_LINEBOOST -> OcrOutput(runMlKitLineBoost(context, uri), "mlkit-lineboost")
            EngineMode.MLKIT_HYBRID -> runHybrid(context, uri)
            EngineMode.NEURAL_BOOST -> OcrOutput(runNeuralBoost(context, uri), "neural-boost")
        }
    }

    suspend fun extractText(context: Context, uri: Uri): String {
        return extract(context, uri).text
    }

    private suspend fun runHybrid(context: Context, uri: Uri): OcrOutput {
        val baseline = runCatching { OcrOutput(runMlKit(context, uri), "mlkit-baseline") }.getOrNull()
        val preprocessed = runCatching { OcrOutput(runMlKitPreprocessed(context, uri), "mlkit-preprocessed") }.getOrNull()
        val neuralBoost = runCatching { OcrOutput(runNeuralBoost(context, uri), "neural-boost") }.getOrNull()
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
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        return try {
            val result = recognizer.process(image).await()
            reconstructLayout(result).normalizeForScoring().cleanupSemanticNoise().cleanupNoise()
        } finally {
            recognizer.close()
        }
    }

    private fun reconstructLayout(visionText: com.google.mlkit.vision.text.Text): String {
        if (visionText.textBlocks.isEmpty()) return ""

        // DNA: Grid+ Engine (The Final Boss)
        // Flatten ALL elements (words) for absolute spatial control
        val allElements = visionText.textBlocks.flatMap { it.lines }.flatMap { it.elements }
        if (allElements.isEmpty()) return ""

        // Project X-axis Gutters (Column Detection)
        // We look for horizontal intervals that contain text across the whole document
        val sortedByLeft = allElements.sortedBy { it.boundingBox?.left ?: 0 }
        
        // Group elements into ROWS using a flexible PVG logic
        val rows = mutableListOf<MutableList<com.google.mlkit.vision.text.Text.Element>>()
        val sortedByTop = allElements.sortedBy { it.boundingBox?.top ?: 0 }
        
        for (element in sortedByTop) {
            val elBox = element.boundingBox ?: continue
            val elMidY = (elBox.top + elBox.bottom) / 2
            
            val matchingRow = rows.find { row ->
                val rTop = row.minOf { it.boundingBox?.top ?: Int.MAX_VALUE }
                val rBottom = row.maxOf { it.boundingBox?.bottom ?: Int.MIN_VALUE }
                val rMidY = (rTop + rBottom) / 2
                
                val midDistance = Math.abs(elMidY - rMidY)
                val tolerance = (elBox.height() * 0.8f).coerceAtLeast(12f)
                
                midDistance < tolerance || elMidY in rTop..rBottom
            }
            
            if (matchingRow != null) {
                matchingRow.add(element)
            } else {
                rows.add(mutableListOf(element))
            }
        }

        val resultText = StringBuilder()
        for (row in rows) {
            val sortedRow = row.sortedBy { it.boundingBox?.left ?: 0 }
            var currentRowText = ""
            var lastRight = -1
            
            for (element in sortedRow) {
                val left = element.boundingBox?.left ?: 0
                val right = element.boundingBox?.right ?: 0
                val height = element.boundingBox?.height() ?: 20
                
                if (lastRight != -1) {
                    val gap = left - lastRight
                    if (gap > 0) {
                        // DNA: Monospaced Gutter Alignment
                        // We use the character width for THIS specific word to calculate spaces
                        val charWidth = element.boundingBox?.width()?.let { it / element.text.length.coerceAtLeast(1) } ?: (height / 2)
                        val spaceCount = (gap / charWidth.coerceAtLeast(1)).coerceIn(1, 40)
                        currentRowText += " ".repeat(spaceCount)
                    } else if (gap > -5) {
                        currentRowText += " "
                    }
                }
                
                currentRowText += element.text
                lastRight = right
            }
            
            if (resultText.isNotEmpty()) resultText.append("\n")
            resultText.append(currentRowText)
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
private fun String.cleanupSemanticNoise(): String {
    if (this.isBlank()) return ""
    return this.replace(Regex("(\\d+)6(?=\\s|$)"), "$1G") // DNA: Fix 506 -> 50G (common units error)
               .replace(Regex("(?i)Indcmaret|Indemart|TIndksmaat"), "Indomaret") // DNA: Brand normalization
               .replace(Regex("(?i)IDM KC6"), "IDM KCG") // Indomaret specific
}
