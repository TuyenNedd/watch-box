package dev.watchbox.tv.core.util

import dev.watchbox.tv.core.model.Movie
import java.text.Normalizer
import java.util.Locale

private val combiningMarks = Regex("\\p{M}+")
private val apostrophes = Regex("['’]")
private val punctuation = Regex("[^\\p{L}\\p{N}\\s]+")
private val whitespace = Regex("\\s+")

fun normalizeForSearch(value: String): String {
    val withMappedD = value.replace('đ', 'd').replace('Đ', 'D')
    return Normalizer.normalize(withMappedD, Normalizer.Form.NFD)
        .replace(combiningMarks, "")
        .lowercase(Locale.ROOT)
        .replace(apostrophes, "")
        .replace(punctuation, " ")
        .trim()
        .replace(whitespace, " ")
}

fun Movie.matchesSearch(query: String): Boolean {
    val normalizedQuery = normalizeForSearch(query)
    if (normalizedQuery.isBlank()) return false
    return sequenceOf(title, originalTitle.orEmpty(), description)
        .map(::normalizeForSearch)
        .any { normalizedQuery in it }
}
