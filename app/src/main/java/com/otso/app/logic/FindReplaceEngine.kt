package com.otso.app.logic

import com.otso.app.viewmodel.FindReplaceState

data class ReplaceResult(
    val newText: String,
    val newCursorOffset: Int?,
    val newState: FindReplaceState,
)

class FindReplaceEngine {

    fun findMatches(text: String, query: String, caseSensitive: Boolean): List<IntRange> {
        if (query.isBlank()) return emptyList()
        val results = mutableListOf<IntRange>()
        val searchText = if (caseSensitive) text else text.lowercase()
        val searchQuery = if (caseSensitive) query else query.lowercase()
        var startIndex = 0
        while (startIndex < searchText.length) {
            val index = searchText.indexOf(searchQuery, startIndex)
            if (index == -1) break
            results.add(index until index + query.length)
            startIndex = index + 1
        }
        return results
    }

    fun findNext(state: FindReplaceState): FindReplaceState {
        if (state.matches.isEmpty()) return state
        val next = (state.activeMatchIndex + 1) % state.matches.size
        return state.copy(activeMatchIndex = next)
    }

    fun findPrevious(state: FindReplaceState): FindReplaceState {
        if (state.matches.isEmpty()) return state
        val prev = if (state.activeMatchIndex <= 0) {
            state.matches.lastIndex
        } else {
            state.activeMatchIndex - 1
        }
        return state.copy(activeMatchIndex = prev)
    }

    fun replaceCurrent(currentText: String, state: FindReplaceState): ReplaceResult {
        val activeMatch = state.matches.getOrNull(state.activeMatchIndex)
            ?: return ReplaceResult(
                newText = currentText,
                newCursorOffset = null,
                newState = state,
            )

        val start = activeMatch.first.coerceIn(0, currentText.length)
        val end = (activeMatch.last + 1).coerceIn(start, currentText.length)
        val replacedText = currentText.replaceRange(start, end, state.replaceQuery)
        val cursor = start + state.replaceQuery.length
        val matches = findMatches(replacedText, state.findQuery, state.isCaseSensitive)
        val updatedState = state.copy(
            matches = matches,
            activeMatchIndex = if (matches.isNotEmpty()) 0 else -1,
        )
        return ReplaceResult(
            newText = replacedText,
            newCursorOffset = cursor,
            newState = updatedState,
        )
    }

    fun replaceAll(currentText: String, state: FindReplaceState): ReplaceResult {
        if (state.findQuery.isBlank()) {
            return ReplaceResult(
                newText = currentText,
                newCursorOffset = null,
                newState = state,
            )
        }
        val replacedText = if (state.isCaseSensitive) {
            currentText.replace(state.findQuery, state.replaceQuery)
        } else {
            currentText.replace(
                state.findQuery,
                state.replaceQuery,
                ignoreCase = true,
            )
        }
        val matches = findMatches(replacedText, state.findQuery, state.isCaseSensitive)
        val updatedState = state.copy(
            matches = matches,
            activeMatchIndex = if (matches.isNotEmpty()) 0 else -1,
        )
        return ReplaceResult(
            newText = replacedText,
            newCursorOffset = null,
            newState = updatedState,
        )
    }
}
