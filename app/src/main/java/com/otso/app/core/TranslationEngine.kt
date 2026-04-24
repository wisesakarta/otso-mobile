package com.otso.app.core

import com.google.android.gms.tasks.Task
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class TranslationOutput(
    val text: String,
    val sourceLanguage: String,
    val targetLanguage: String,
    val engineUsed: String = "mlkit-translate",
)

object TranslationEngine {

    suspend fun translate(
        text: String,
        preferredTargetTag: String = Locale.getDefault().toLanguageTag(),
        preferredSourceTag: String? = null,
    ): TranslationOutput {
        val sourceText = text.trim()
        if (sourceText.isBlank()) {
            return TranslationOutput(
                text = sourceText,
                sourceLanguage = "und",
                targetLanguage = "und",
            )
        }

        val sourceLanguage = resolvePreferredLanguage(preferredSourceTag)
            ?: run {
                val detectedTag = identifyLanguage(sourceText)
                resolvePreferredLanguage(detectedTag)
            }
            ?: throw IllegalStateException("Unable to detect source language.")

        val targetLanguage = resolveTargetLanguage(
            sourceLanguage = sourceLanguage,
            preferredTargetTag = preferredTargetTag,
        )

        if (sourceLanguage == targetLanguage) {
            return TranslationOutput(
                text = sourceText,
                sourceLanguage = sourceLanguage,
                targetLanguage = targetLanguage,
            )
        }

        val translator = Translation.getClient(
            TranslatorOptions.Builder()
                .setSourceLanguage(sourceLanguage)
                .setTargetLanguage(targetLanguage)
                .build()
        )

        return try {
            translator.downloadModelIfNeeded(DownloadConditions.Builder().build()).await()
            val translated = translator.translate(sourceText).await()
            TranslationOutput(
                text = translated,
                sourceLanguage = sourceLanguage,
                targetLanguage = targetLanguage,
            )
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

    private fun resolveTargetLanguage(
        sourceLanguage: String,
        preferredTargetTag: String,
    ): String {
        val preferred = resolvePreferredLanguage(preferredTargetTag)

        if (preferred != null && preferred != sourceLanguage) {
            return preferred
        }

        return if (sourceLanguage != TranslateLanguage.ENGLISH) {
            TranslateLanguage.ENGLISH
        } else {
            TranslateLanguage.INDONESIAN
        }
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
