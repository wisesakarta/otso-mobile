package com.otso.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.os.SystemClock
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.otso.app.core.NeuralVisionEngine
import com.otso.app.core.OcrEngine
import java.io.File
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OcrSyntheticBenchmarkTest {

    @Test
    fun benchmarkGeneratedPrintedAndReceiptSamples() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val workDir = File(context.cacheDir, "ocr-synthetic-benchmark").apply { mkdirs() }
        val reportDir = File(context.filesDir, "ocr-benchmark").apply { mkdirs() }
        val report = JSONArray()
        val samples = listOf(
            SyntheticSample(
                name = "printed_note",
                expected = "OTSO VISION DOCUMENT CLEANUP MODEL READY",
                lines = listOf(
                    "OTSO VISION",
                    "DOCUMENT CLEANUP",
                    "MODEL READY",
                ),
            ),
            SyntheticSample(
                name = "receipt_id",
                expected = "TOKO OTSO TOTAL RP 125000 TERIMA KASIH",
                lines = listOf(
                    "TOKO OTSO",
                    "KOPI SUSU      25000",
                    "BUKU CATATAN  100000",
                    "TOTAL RP 125000",
                    "TERIMA KASIH",
                ),
            ),
        )
        val modes = listOf(
            OcrEngine.EngineMode.MLKIT_BASELINE,
            OcrEngine.EngineMode.NEURAL_BOOST,
        )
        var nonBlankCount = 0
        val neuralFailures = mutableListOf<String>()
        val neuralBlankOutputs = mutableListOf<String>()

        for (sample in samples) {
            val imageFile = renderSample(workDir, sample)
            val uri = Uri.fromFile(imageFile)

            for (mode in modes) {
                OcrEngine.mode = mode
                OcrEngine.targetScript = OcrEngine.ScriptType.LATIN
                val startedAt = SystemClock.elapsedRealtime()
                val output = OcrEngine.extract(context, uri)
                val elapsedMs = SystemClock.elapsedRealtime() - startedAt
                val text = output.text.trim()
                if (text.isNotBlank()) nonBlankCount++

                val cer = OcrMetrics.cer(text, sample.expected)
                val wer = OcrMetrics.wer(text, sample.expected)
                val row = JSONObject()
                    .put("sample", sample.name)
                    .put("mode", mode.name)
                    .put("engineUsed", output.engineUsed)
                    .put("visionStatus", output.visionStatus.name)
                    .put("modelFailure", NeuralVisionEngine.lastFailureMessage)
                    .put("elapsedMs", elapsedMs)
                    .put("cer", cer)
                    .put("wer", wer)
                    .put("text", text)
                    .put("expected", sample.expected)
                report.put(row)

                Log.i(
                    TAG,
                    "sample=${sample.name} mode=${mode.name} engine=${output.engineUsed} " +
                        "status=${output.visionStatus.name} elapsedMs=$elapsedMs " +
                        "CER=${"%.3f".format(cer)} WER=${"%.3f".format(wer)} " +
                        "text=\"${text.replace(Regex("\\s+"), " ").take(140)}\"",
                )

                if (mode == OcrEngine.EngineMode.NEURAL_BOOST) {
                    if (output.visionStatus != NeuralVisionEngine.RuntimeStatus.MODEL_READY) {
                        neuralFailures += "${sample.name}:${output.visionStatus.name}:${NeuralVisionEngine.lastFailureMessage}"
                    }
                    if (text.isBlank()) {
                        neuralBlankOutputs += sample.name
                    }
                }
            }
        }

        val reportFile = File(reportDir, "synthetic_ocr_benchmark.json")
        reportFile.writeText(report.toString(2))
        Log.i(TAG, "REPORT_PATH=${reportFile.absolutePath}")

        assertTrue("OCR benchmark produced blank output for every sample/mode.", nonBlankCount > 0)
        assertTrue(
            "Neural model did not reach MODEL_READY. Report: ${reportFile.absolutePath}. " +
                neuralFailures.joinToString(" | "),
            neuralFailures.isEmpty(),
        )
        assertTrue(
            "Neural model returned blank OCR output for: ${neuralBlankOutputs.joinToString()}",
            neuralBlankOutputs.isEmpty(),
        )
    }

    private fun renderSample(workDir: File, sample: SyntheticSample): File {
        val bitmap = Bitmap.createBitmap(1600, 1000, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 86f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(15, 15, 15)
            textSize = 58f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(220, 220, 220)
            strokeWidth = 3f
        }

        canvas.drawLine(90f, 120f, 1510f, 120f, linePaint)
        sample.lines.forEachIndexed { index, line ->
            val paint = if (index == 0) titlePaint else bodyPaint
            canvas.drawText(line, 120f, 230f + (index * 115f), paint)
        }
        canvas.drawLine(90f, 850f, 1510f, 850f, linePaint)

        val imageFile = File(workDir, "${sample.name}.png")
        imageFile.outputStream().use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        }
        bitmap.recycle()
        return imageFile
    }

    private data class SyntheticSample(
        val name: String,
        val expected: String,
        val lines: List<String>,
    )

    private companion object {
        const val TAG = "OcrSyntheticBenchmark"
    }
}
