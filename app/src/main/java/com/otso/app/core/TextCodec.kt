package com.otso.app.core

import com.otso.app.model.LineEnding
import com.otso.app.model.TextEncoding
import java.nio.charset.Charset
import kotlin.math.max

data class EncodedText(
    val content: String,
    val encoding: TextEncoding,
    val lineEnding: LineEnding,
)

object TextCodec {

    private val utf8 = Charsets.UTF_8
    private val utf16Le = Charset.forName("UTF-16LE")
    private val utf16Be = Charset.forName("UTF-16BE")
    private val utf8Bom = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
    private val utf16LeBom = byteArrayOf(0xFF.toByte(), 0xFE.toByte())
    private val utf16BeBom = byteArrayOf(0xFE.toByte(), 0xFF.toByte())

    fun decode(bytes: ByteArray): EncodedText {
        if (bytes.isEmpty()) {
            return EncodedText(
                content = "",
                encoding = TextEncoding.UTF8,
                lineEnding = LineEnding.LF,
            )
        }

        val (detectedEncoding, bomLength, charset) = detectEncoding(bytes)
        val text = bytes.copyOfRange(bomLength, bytes.size).toString(charset)
        val lineEnding = detectDominantLineEnding(text)

        return EncodedText(
            content = text,
            encoding = detectedEncoding,
            lineEnding = lineEnding,
        )
    }

    fun encode(
        text: String,
        encoding: TextEncoding,
        lineEnding: LineEnding,
    ): ByteArray {
        val normalized = normalizeLineEndings(text, lineEnding)
        return when (encoding) {
            TextEncoding.UTF8 -> normalized.toByteArray(utf8)
            TextEncoding.UTF8_BOM -> utf8Bom + normalized.toByteArray(utf8)
            TextEncoding.UTF16_LE -> utf16LeBom + normalized.toByteArray(utf16Le)
            TextEncoding.UTF16_BE -> utf16BeBom + normalized.toByteArray(utf16Be)
        }
    }

    private fun detectEncoding(bytes: ByteArray): Triple<TextEncoding, Int, Charset> {
        if (bytes.size >= 3 &&
            bytes[0] == utf8Bom[0] &&
            bytes[1] == utf8Bom[1] &&
            bytes[2] == utf8Bom[2]
        ) {
            return Triple(TextEncoding.UTF8_BOM, 3, utf8)
        }
        if (bytes.size >= 2 &&
            bytes[0] == utf16LeBom[0] &&
            bytes[1] == utf16LeBom[1]
        ) {
            return Triple(TextEncoding.UTF16_LE, 2, utf16Le)
        }
        if (bytes.size >= 2 &&
            bytes[0] == utf16BeBom[0] &&
            bytes[1] == utf16BeBom[1]
        ) {
            return Triple(TextEncoding.UTF16_BE, 2, utf16Be)
        }
        return Triple(TextEncoding.UTF8, 0, utf8)
    }

    private fun detectDominantLineEnding(text: String): LineEnding {
        var crlf = 0
        var lf = 0
        var cr = 0
        var i = 0
        while (i < text.length) {
            val c = text[i]
            if (c == '\r') {
                if (i + 1 < text.length && text[i + 1] == '\n') {
                    crlf++
                    i += 2
                    continue
                }
                cr++
            } else if (c == '\n') {
                lf++
            }
            i++
        }
        return when {
            crlf >= max(lf, cr) && crlf > 0 -> LineEnding.CRLF
            lf >= max(crlf, cr) && lf > 0 -> LineEnding.LF
            cr > 0 -> LineEnding.CR
            else -> LineEnding.LF
        }
    }

    private fun normalizeLineEndings(text: String, lineEnding: LineEnding): String {
        val canonical = StringBuilder(text.length)
        var i = 0
        while (i < text.length) {
            val c = text[i]
            if (c == '\r') {
                if (i + 1 < text.length && text[i + 1] == '\n') {
                    canonical.append('\n')
                    i += 2
                    continue
                }
                canonical.append('\n')
            } else {
                canonical.append(c)
            }
            i++
        }

        return when (lineEnding) {
            LineEnding.LF -> canonical.toString()
            LineEnding.CRLF -> canonical.toString().replace("\n", "\r\n")
            LineEnding.CR -> canonical.toString().replace("\n", "\r")
        }
    }
}
