package com.otso.app.model

import java.util.UUID

enum class TextEncoding { UTF8, UTF8_BOM, UTF16_LE, UTF16_BE }
enum class LineEnding { LF, CRLF, CR }
enum class TabSource { INTERNAL, SAF }

data class TabDocument(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Untitled",
    val source: TabSource = TabSource.INTERNAL,
    val uriOrPath: String? = null,
    val content: String = "",
    val cursorLine: Int = 0,
    val cursorCol: Int = 0,
    val fontSizeSp: Int = 15,
    val encoding: TextEncoding = TextEncoding.UTF8,
    val lineEnding: LineEnding = LineEnding.LF,
    val isModified: Boolean = false,
)