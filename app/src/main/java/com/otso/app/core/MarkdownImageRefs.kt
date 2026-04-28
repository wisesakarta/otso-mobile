package com.otso.app.core

object MarkdownImageRefs {
    private val imageRefRegex = Regex("""!\[([^\]]*)\]\(images/([^)]+)\)""")

    data class ImageReference(
        val matchText: String,
        val altText: String,
        val filename: String,
        val startIndex: Int,
        val endIndex: Int,
    )

    fun findAllInText(text: String): List<ImageReference> {
        return imageRefRegex.findAll(text).map { match ->
            ImageReference(
                matchText = match.value,
                altText = match.groupValues[1],
                filename = match.groupValues[2],
                startIndex = match.range.first,
                endIndex = match.range.last,
            )
        }.toList()
    }

    fun findAtPosition(text: String, cursorPos: Int): ImageReference? {
        return findAllInText(text).firstOrNull { reference ->
            cursorPos in reference.startIndex..reference.endIndex
        }
    }
}
