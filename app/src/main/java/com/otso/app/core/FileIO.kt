package com.otso.app.core

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object FileIO {

    private const val NOTES_DIR = "notes"
    @Volatile
    private var appContext: Context? = null

    fun bindContext(context: Context) {
        appContext = context.applicationContext
    }

    suspend fun saveInternal(
        context: Context,
        fileName: String,
        content: String,
    ) {
        saveInternalBytes(
            context = context,
            fileName = fileName,
            bytes = content.toByteArray(Charsets.UTF_8),
        )
    }

    suspend fun saveInternalBytes(
        context: Context,
        fileName: String,
        bytes: ByteArray,
    ) {
        withContext(Dispatchers.IO) {
            val notesDir = getNotesDir(context)
            val targetFile = File(notesDir, fileName)
            targetFile.writeBytes(bytes)
        }
    }

    suspend fun readInternal(
        context: Context,
        fileName: String,
    ): String {
        return readInternalBytes(context, fileName).toString(Charsets.UTF_8)
    }

    suspend fun readInternalBytes(
        context: Context,
        fileName: String,
    ): ByteArray {
        return withContext(Dispatchers.IO) {
            val targetFile = File(getNotesDir(context), fileName)
            if (!targetFile.exists()) return@withContext ByteArray(0)
            targetFile.readBytes()
        }
    }

    suspend fun checkStorageUsage(context: Context): Long {
        return withContext(Dispatchers.IO) {
            val notesDir = getNotesDir(context)
            notesDir.walkTopDown()
                .filter { it.isFile }
                .sumOf { it.length() }
        }
    }

    suspend fun deleteInternal(
        context: Context,
        fileName: String,
    ) {
        withContext(Dispatchers.IO) {
            val targetFile = File(getNotesDir(context), fileName)
            if (targetFile.exists()) {
                targetFile.delete()
            }
        }
    }

    suspend fun openExternalFile(uri: Uri): EncodedText {
        return withContext(Dispatchers.IO) {
            val context = requireNotNull(appContext) { "FileIO context is not bound." }
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: ByteArray(0)
            TextCodec.decode(bytes)
        }
    }

    suspend fun saveExternalFile(
        uri: Uri,
        content: String,
        encoding: com.otso.app.model.TextEncoding,
        lineEnding: com.otso.app.model.LineEnding,
    ) {
        withContext(Dispatchers.IO) {
            val context = requireNotNull(appContext) { "FileIO context is not bound." }
            val payload = TextCodec.encode(content, encoding, lineEnding)
            context.contentResolver.openOutputStream(uri, "wt")?.use { stream ->
                stream.write(payload)
                stream.flush()
            }
        }
    }

    fun takePersistableUriPermission(uri: Uri) {
        val context = requireNotNull(appContext) { "FileIO context is not bound." }
        val flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
            android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        runCatching {
            context.contentResolver.takePersistableUriPermission(uri, flags)
        }
    }

    private fun getNotesDir(context: Context): File {
        val notesDir = File(context.filesDir, NOTES_DIR)
        if (!notesDir.exists()) {
            notesDir.mkdirs()
        }
        return notesDir
    }
}
