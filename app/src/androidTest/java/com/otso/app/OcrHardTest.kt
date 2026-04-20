package com.otso.app

import android.net.Uri
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.otso.app.core.OcrEngine
import kotlinx.coroutines.runBlocking
import org.junit.Assume.assumeTrue
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class OcrHardTest {

    @Test
    fun ocrBatch_fromProvidedDataset_reportsCerWerAgainstGroundTruth() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val assets = InstrumentationRegistry.getInstrumentation().context.assets
        val imageNames = assets.list("ocrTesting")
            ?.filter { it.endsWith(".jpg", ignoreCase = true) || it.endsWith(".jpeg", ignoreCase = true) || it.endsWith(".png", ignoreCase = true) }
            ?.sorted()
            .orEmpty()

        assertTrue("No OCR test images found in assets/ocrTesting", imageNames.isNotEmpty())

        val gtNames = assets.list("ocrTesting")
            ?.filter { it.endsWith(".gt.txt", ignoreCase = true) }
            ?.sorted()
            .orEmpty()
        assumeTrue(
            "No ground-truth files found. Add *.gt.txt files in androidTest/assets/ocrTesting.",
            gtNames.isNotEmpty()
        )

        val workDir = File(context.cacheDir, "ocr-hard")
        workDir.mkdirs()

        val missingGt = mutableListOf<String>()
        val invalidGt = mutableListOf<String>()
        val cerValues = mutableListOf<Double>()
        val werValues = mutableListOf<Double>()

        for (name in imageNames) {
            val target = File(workDir, name)
            assets.open("ocrTesting/$name").use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            }

            val text = OcrEngine.extractText(context, Uri.fromFile(target)).trim()
            val stem = name.substringBeforeLast('.')
            val gtName = "$stem.gt.txt"
            if (gtName !in gtNames) {
                missingGt += name
                continue
            }

            val expected = assets.open("ocrTesting/$gtName").bufferedReader().use { it.readText() }
            if (expected.trim().length < 10) {
                invalidGt += gtName
                continue
            }
            val cer = OcrMetrics.cer(text, expected)
            val wer = OcrMetrics.wer(text, expected)
            cerValues += cer
            werValues += wer
            Log.i(
                "OcrHardTest",
                "$name -> CER=${"%.3f".format(cer)} WER=${"%.3f".format(wer)} pred=\"${text.replace(Regex("\\s+"), " ").take(80)}\""
            )
        }

        assertTrue("Missing ground-truth sidecars for: ${missingGt.joinToString()}", missingGt.isEmpty())
        assertTrue("Ground-truth files look empty/placeholder: ${invalidGt.joinToString()}", invalidGt.isEmpty())
        assertTrue("No image had matching ground-truth sidecar.", cerValues.isNotEmpty())

        val avgCer = cerValues.average()
        val avgWer = werValues.average()
        Log.i("OcrHardTest", "AVG_CER=${"%.3f".format(avgCer)} AVG_WER=${"%.3f".format(avgWer)}")

        assertTrue(
            "CER/WER too high. avgCer=${"%.3f".format(avgCer)} avgWer=${"%.3f".format(avgWer)}",
            avgCer <= 0.20 && avgWer <= 0.30
        )
    }
}
