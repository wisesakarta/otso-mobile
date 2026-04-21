package com.otso.app

import android.net.Uri
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.otso.app.core.OcrEngine
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class OcrImageOneProbeTest {

    @Test
    fun probeImageOneAcrossEngines() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val assets = InstrumentationRegistry.getInstrumentation().context.assets

        val imageName = "imageOne.jpg"
        val gtName = "imageOne.gt.txt"
        val workDir = File(context.cacheDir, "ocr-probe").apply { mkdirs() }
        val imageFile = File(workDir, imageName)

        assets.open("ocrTesting/$imageName").use { input ->
            imageFile.outputStream().use { output -> input.copyTo(output) }
        }
        val expected = assets.open("ocrTesting/$gtName").bufferedReader().use { it.readText() }
        val uri = Uri.fromFile(imageFile)

        val modes = listOf(
            OcrEngine.EngineMode.MLKIT_BASELINE,
            OcrEngine.EngineMode.MLKIT_PREPROCESSED,
            OcrEngine.EngineMode.MLKIT_MULTISCALE,
            OcrEngine.EngineMode.MLKIT_LINEBOOST,
            OcrEngine.EngineMode.MLKIT_HYBRID,
        )

        for (mode in modes) {
            OcrEngine.mode = mode
            val out = OcrEngine.extract(context, uri).text
            val cer = OcrMetrics.cer(out, expected)
            val wer = OcrMetrics.wer(out, expected)
            Log.i(
                "OcrImageOneProbe",
                "mode=${mode.name} CER=${"%.3f".format(cer)} WER=${"%.3f".format(wer)} text=\"${out.replace(Regex("\\s+"), " ").take(160)}\""
            )
        }
    }
}
