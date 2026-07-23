package dev.watchbox.core.util

import dev.watchbox.core.model.Movie

private val diacriticMap = mapOf(
    'à' to 'a', 'á' to 'a', 'ả' to 'a', 'ã' to 'a', 'ạ' to 'a',
    'ă' to 'a', 'ằ' to 'a', 'ắ' to 'a', 'ẳ' to 'a', 'ẵ' to 'a', 'ặ' to 'a',
    'â' to 'a', 'ầ' to 'a', 'ấ' to 'a', 'ẩ' to 'a', 'ẫ' to 'a', 'ậ' to 'a',
    'è' to 'e', 'é' to 'e', 'ẻ' to 'e', 'ẽ' to 'e', 'ẹ' to 'e',
    'ê' to 'e', 'ề' to 'e', 'ế' to 'e', 'ể' to 'e', 'ễ' to 'e', 'ệ' to 'e',
    'ì' to 'i', 'í' to 'i', 'ỉ' to 'i', 'ĩ' to 'i', 'ị' to 'i',
    'ò' to 'o', 'ó' to 'o', 'ỏ' to 'o', 'õ' to 'o', 'ọ' to 'o',
    'ô' to 'o', 'ồ' to 'o', 'ố' to 'o', 'ổ' to 'o', 'ỗ' to 'o', 'ộ' to 'o',
    'ơ' to 'o', 'ờ' to 'o', 'ớ' to 'o', 'ở' to 'o', 'ỡ' to 'o', 'ợ' to 'o',
    'ù' to 'u', 'ú' to 'u', 'ủ' to 'u', 'ũ' to 'u', 'ụ' to 'u',
    'ư' to 'u', 'ừ' to 'u', 'ứ' to 'u', 'ử' to 'u', 'ữ' to 'u', 'ự' to 'u',
    'ỳ' to 'y', 'ý' to 'y', 'ỷ' to 'y', 'ỹ' to 'y', 'ỵ' to 'y',
    'đ' to 'd',
)

private val whitespace = Regex("\\s+")

fun normalizeForSearch(value: String): String {
    val sb = StringBuilder(value.length)
    for (ch in value) {
        val lower = ch.lowercaseChar()
        sb.append(diacriticMap[lower] ?: lower)
    }
    return sb.toString()
        .replace("'", "")
        .replace("'", "")
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
