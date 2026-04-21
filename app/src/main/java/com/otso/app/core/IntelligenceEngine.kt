package com.otso.app.core

import android.content.Context
import com.google.mlkit.nl.entityextraction.EntityAnnotation
import com.google.mlkit.nl.entityextraction.EntityExtraction
import com.google.mlkit.nl.entityextraction.EntityExtractionParams
import com.google.mlkit.nl.entityextraction.EntityExtractorOptions
import com.google.mlkit.nl.languageid.LanguageIdentification
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import com.google.android.gms.tasks.Task

object IntelligenceEngine {

    /**
     * Identifies the language of the given text.
     * Returns the BCP-47 language code or "und" if undetermined.
     */
    suspend fun identifyLanguage(text: String): String {
        val languageIdentifier = LanguageIdentification.getClient()
        return try {
            languageIdentifier.identifyLanguage(text).await()
        } catch (e: Exception) {
            "und"
        } finally {
            languageIdentifier.close()
        }
    }

    /**
     * Extracts entities (Phone, Date, URL, etc.) and auto-formats the text.
     * For now, focuses on phone numbers and dates as per user request.
     */
    suspend fun extractAndFormat(context: Context, text: String): String {
        val languageCode = identifyLanguage(text)
        
        // Entity Extraction requires the language model.
        // We use the detected language if supported, otherwise fallback to English.
        val modelIdentifier = if (isEntityLanguageSupported(languageCode)) {
            languageCode
        } else {
            EntityExtractorOptions.ENGLISH
        }

        val extractor = EntityExtraction.getClient(
            EntityExtractorOptions.Builder(modelIdentifier).build()
        )

        return try {
            // Ensure model is downloaded
            extractor.downloadModelIfNeeded().await()
            
            val params = EntityExtractionParams.Builder(text).build()
            val annotations = extractor.annotate(params).await()
            
            applyAutoFormatting(text, annotations)
        } catch (e: Exception) {
            text // Fallback to original text on error
        } finally {
            extractor.close()
        }
    }

    /**
     * Applies automatic formatting for phone numbers and dates.
     * "Industrial" style: clean, consistent.
     */
    private fun applyAutoFormatting(text: String, annotations: List<EntityAnnotation>): String {
        if (annotations.isEmpty()) return text
        
        val sortedAnnotations = annotations.sortedByDescending { it.start }
        var result = text
        
        for (annotation in sortedAnnotations) {
            val entity = annotation.entities.firstOrNull() ?: continue
            val originalValue = text.substring(annotation.start, annotation.end)
            
            val formattedValue = when (entity.type) {
                com.google.mlkit.nl.entityextraction.Entity.TYPE_PHONE -> {
                    // Simple "Industrial" phone formatting: +X XXX-XXX-XXXX
                    // Actually, we'll keep it surgical and clean.
                    originalValue.replace(Regex("[^\\d+]"), "").let { digits ->
                        if (digits.startsWith("+")) digits else "+$digits"
                    }
                }
                com.google.mlkit.nl.entityextraction.Entity.TYPE_DATE_TIME -> {
                    // We could format to YYYY-MM-DD but keeping it conservative for now
                    originalValue
                }
                else -> originalValue
            }
            
            if (formattedValue != originalValue) {
                result = result.replaceRange(annotation.start, annotation.end, formattedValue)
            }
        }
        
        return result
    }

    private fun isEntityLanguageSupported(lang: String): Boolean {
        val supported = listOf(
            EntityExtractorOptions.ENGLISH,
            EntityExtractorOptions.GERMAN,
            EntityExtractorOptions.FRENCH,
            EntityExtractorOptions.ITALIAN,
            EntityExtractorOptions.JAPANESE,
            EntityExtractorOptions.KOREAN,
            EntityExtractorOptions.PORTUGUESE,
            EntityExtractorOptions.RUSSIAN,
            EntityExtractorOptions.CHINESE,
            EntityExtractorOptions.SPANISH,
            EntityExtractorOptions.TURKISH,
        )
        return supported.contains(lang)
    }

    private suspend fun <T> Task<T>.await(): T =
        suspendCancellableCoroutine { continuation ->
            addOnSuccessListener { result -> continuation.resume(result) }
            addOnFailureListener { error -> continuation.resumeWithException(error) }
            addOnCanceledListener { continuation.cancel() }
        }
}
