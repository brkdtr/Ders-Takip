package com.derstakip.app.util

import java.util.Locale
import java.util.regex.Pattern

object TopicExtractor {

    // Pre-defined category keywords commonly used in English language courses
    private val TOPIC_RULES = listOf(
        listOf("gramer", "grammar", "tenses", "present simple", "past simple", "continuous", "perfect", "passive", "modal", "conditional", "clause", "preposition", "adjective", "adverb") to "Gramer (Grammar)",
        listOf("kelime", "vocabulary", "idiom", "phrasal verb", "words", "collocation", "synonym") to "Kelime Bilgisi (Vocabulary)",
        listOf("dinleme", "listening", "podcast", "comprehension", "audio") to "Dinleme (Listening)",
        listOf("konuşma", "speaking", "pronunciation", "telaffuz", "fluency", "dialogue", "conversation") to "Konuşma & Telaffuz",
        listOf("okuma", "reading", "paragraf", "çeviri", "translation", "article") to "Okuma & Çeviri (Reading)",
        listOf("yazma", "writing", "essay", "paragraph writing", "composition") to "Yazma (Writing)",
        listOf("yds", "yökdil", "eyds", "toefl", "ielts", "yks dil", "soru çözümü", "deneme", "test") to "Sınav Hazırlığı",
        listOf("başlangıç", "beginner", "a1", "a2", "temel", "alfabe", "giriş") to "Temel Seviye (A1-A2)",
        listOf("orta seviye", "intermediate", "b1", "b2") to "Orta Seviye (B1-B2)",
        listOf("ileri seviye", "advanced", "c1", "c2") to "İleri Seviye (C1-C2)"
    )

    private val PREFIX_DELIMITERS = listOf(" - ", " – ", " — ", " : ", ": ", " | ", " / ", " #")

    /**
     * Extracts or categorizes a topic name from a video title.
     * Guaranteed to return a meaningful non-blank topic string.
     */
    fun extractTopic(videoTitle: String?, playlistTitle: String? = null): String {
        if (videoTitle.isNullOrBlank()) {
            return playlistTitle?.takeIf { it.isNotBlank() } ?: "Genel Dersler"
        }

        val lowerTitle = videoTitle.lowercase(Locale("tr"))

        // 1. Keyword rule match
        for ((keywords, category) in TOPIC_RULES) {
            for (kw in keywords) {
                if (lowerTitle.contains(kw)) {
                    return category
                }
            }
        }

        // 2. Structural parsing: look for prefix before delimiters (e.g. "Ünite 3: Aile Bireyleri" -> "Ünite 3")
        for (delim in PREFIX_DELIMITERS) {
            val idx = videoTitle.indexOf(delim)
            if (idx in 3..35) {
                val candidate = videoTitle.substring(0, idx).trim()
                if (isValidTopicName(candidate)) {
                    return candidate
                }
            }
        }

        // 3. Fallback to playlist title or general category
        if (!playlistTitle.isNullOrBlank() && playlistTitle.length <= 40) {
            return playlistTitle.trim()
        }

        return "Genel Konular"
    }

    private fun isValidTopicName(candidate: String): Boolean {
        if (candidate.isBlank() || candidate.length > 35) return false
        // Exclude purely numbers or generic labels like "#1"
        if (candidate.matches(Regex("^#?\\d+$"))) return false
        return true
    }
}
