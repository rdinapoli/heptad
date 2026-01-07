package com.heptad.app.data.models

/**
 * Represents a word definition
 *
 * @property word The word being defined
 * @property definition The definition text
 * @property partOfSpeech The part of speech (noun, verb, adjective, etc.)
 */
data class Definition(
    val word: String,
    val definition: String,
    val partOfSpeech: String = "unknown"
) {
    companion object {
        /**
         * Placeholder for when no definition is available
         */
        fun unavailable(word: String): Definition = Definition(
            word = word,
            definition = "Definition not available",
            partOfSpeech = "unknown"
        )
    }
}
