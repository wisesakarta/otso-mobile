package com.otso.app.core

import com.google.android.gms.tasks.Task
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object TranslationEngine {

    suspend fun translate(
        text: String,
        source: String,
        target: String,
    ): String {
        val sourceText = text.trim()
        if (sourceText.isBlank()) {
            return sourceText
        }

        val sourceLanguage = resolveSourceLanguage(
            source = source,
            text = sourceText,
        )
            ?: throw IllegalStateException("Unable to detect source language.")

        val targetLanguage = resolvePreferredLanguage(target)
            ?: throw IllegalArgumentException("Unsupported target language: $target")

        if (sourceLanguage == targetLanguage) {
            return sourceText
        }

        val translator = Translation.getClient(
            TranslatorOptions.Builder()
                .setSourceLanguage(sourceLanguage)
                .setTargetLanguage(targetLanguage)
                .build()
        )

        return try {
            translator.downloadModelIfNeeded(DownloadConditions.Builder().build()).await()
            translator.translate(sourceText).await()
        } finally {
            translator.close()
        }
    }

    private suspend fun identifyLanguage(text: String): String {
        val languageIdentifier = LanguageIdentification.getClient()
        return try {
            languageIdentifier.identifyLanguage(text).await()
        } catch (_: Exception) {
            "und"
        } finally {
            languageIdentifier.close()
        }
    }

    private suspend fun resolveSourceLanguage(source: String, text: String): String? {
        val normalized = source.trim().lowercase()
        if (normalized == "auto" || normalized.isBlank()) {
            return resolvePreferredLanguage(identifyLanguage(text))
        }
        return resolvePreferredLanguage(normalized)
    }

    private fun resolvePreferredLanguage(tag: String?): String? {
        if (tag.isNullOrBlank()) return null
        return TranslateLanguage.fromLanguageTag(tag)
            ?: TranslateLanguage.fromLanguageTag(tag.substringBefore('-'))
    }

    private suspend fun <T> Task<T>.await(): T =
        suspendCancellableCoroutine { continuation ->
            addOnSuccessListener { result -> continuation.resume(result) }
            addOnFailureListener { error -> continuation.resumeWithException(error) }
            addOnCanceledListener { continuation.cancel() }
        }
}
