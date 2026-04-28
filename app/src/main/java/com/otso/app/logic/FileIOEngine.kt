package com.otso.app.logic

import android.content.Context
import android.net.Uri
import com.otso.app.core.FileIO
import com.otso.app.core.TextCodec
import com.otso.app.model.LineEnding
import com.otso.app.model.TextEncoding
import com.otso.app.model.TextSpan

data class DocumentData(
    val content: String,
    val spans: List<TextSpan> = emptyList(),
    val encoding: TextEncoding,
    val lineEnding: LineEnding,
    val internalFileName: String? = null,
    val hasContentBytes: Boolean = true,
)

class FileIOEngine(
    context: Context,
) {
    private val appContext = context.applicationContext

    init {
        FileIO.bindContext(appContext)
    }

    suspend fun openFile(uri: Uri): Result<DocumentData> = runCatching {
        FileIO.takePersistableUriPermission(uri)
        val decoded = FileIO.openExternalFile(uri)
        val block = decoded.content.toContentBlock()
        DocumentData(
            content = block.rawText,
            spans = block.spans,
            encoding = decoded.encoding,
            lineEnding = decoded.lineEnding,
        )
    }

    suspend fun readInternal(fileName: String): Result<DocumentData> = runCatching {
        val bytes = FileIO.readInternalBytes(
            context = appContext,
            fileName = fileName,
        )
        if (bytes.isEmpty()) {
            return@runCatching DocumentData(
                content = "",
                spans = emptyList(),
                encoding = TextEncoding.UTF8,
                lineEnding = LineEnding.LF,
                internalFileName = fileName,
                hasContentBytes = false,
            )
        }

        val decoded = TextCodec.decode(bytes)
        val block = decoded.content.toContentBlock()
        DocumentData(
            content = block.rawText,
            spans = block.spans,
            encoding = decoded.encoding,
            lineEnding = decoded.lineEnding,
            internalFileName = fileName,
            hasContentBytes = true,
        )
    }

    suspend fun saveFile(data: DocumentData, uri: Uri?): Result<Unit> = runCatching {
        if (uri != null) {
            FileIO.saveExternalFile(
                uri = uri,
                content = data.content,
                encoding = data.encoding,
                lineEnding = data.lineEnding,
            )
            FileIO.takePersistableUriPermission(uri)
            return@runCatching
        }

        val fileName = data.internalFileName
            ?: error("internalFileName is required when saving without a Uri.")
        val encoded = TextCodec.encode(
            text = data.content,
            encoding = data.encoding,
            lineEnding = data.lineEnding,
        )
        FileIO.saveInternalBytes(
            context = appContext,
            fileName = fileName,
            bytes = encoded,
        )
    }

    suspend fun deleteInternal(fileName: String): Result<Unit> = runCatching {
        FileIO.deleteInternal(
            context = appContext,
            fileName = fileName,
        )
    }
}
