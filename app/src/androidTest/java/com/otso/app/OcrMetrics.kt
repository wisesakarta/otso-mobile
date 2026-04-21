package com.otso.app

object OcrMetrics {

    fun cer(prediction: String, reference: String): Double {
        val ref = normalizeChars(reference)
        val pred = normalizeChars(prediction)
        if (ref.isEmpty()) return if (pred.isEmpty()) 0.0 else 1.0
        return levenshtein(pred, ref).toDouble() / ref.length.toDouble()
    }

    fun wer(prediction: String, reference: String): Double {
        val ref = tokenize(reference)
        val pred = tokenize(prediction)
        if (ref.isEmpty()) return if (pred.isEmpty()) 0.0 else 1.0
        return levenshtein(pred, ref).toDouble() / ref.size.toDouble()
    }

    private fun normalizeChars(text: String): String {
        return text.lowercase()
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun tokenize(text: String): List<String> {
        val normalized = normalizeChars(text)
        if (normalized.isEmpty()) return emptyList()
        return normalized.split(" ")
    }

    private fun levenshtein(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j
        for (i in 1..a.length) {
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost,
                )
            }
        }
        return dp[a.length][b.length]
    }

    private fun levenshtein(a: List<String>, b: List<String>): Int {
        val dp = Array(a.size + 1) { IntArray(b.size + 1) }
        for (i in 0..a.size) dp[i][0] = i
        for (j in 0..b.size) dp[0][j] = j
        for (i in 1..a.size) {
            for (j in 1..b.size) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost,
                )
            }
        }
        return dp[a.size][b.size]
    }
}
