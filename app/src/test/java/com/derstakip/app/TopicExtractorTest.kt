package com.derstakip.app

import com.derstakip.app.util.TopicExtractor
import org.junit.Assert.assertEquals
import org.junit.Test

class TopicExtractorTest {

    @Test
    fun `extractTopic identifies English educational topics accurately`() {
        assertEquals(
            "Gramer (Grammar)",
            TopicExtractor.extractTopic("İngilizce Gramer #1 - Present Simple (Geniş Zaman)")
        )

        assertEquals(
            "Gramer (Grammar)",
            TopicExtractor.extractTopic("English Grammar Course: Modals and Conditionals")
        )

        assertEquals(
            "Kelime Bilgisi (Vocabulary)",
            TopicExtractor.extractTopic("Kelime Bilgisi | Günlük Yaşamda 50 Phrasal Verb")
        )

        assertEquals(
            "Dinleme (Listening)",
            TopicExtractor.extractTopic("Dinleme (Listening) | A2 Seviye Podcast")
        )

        assertEquals(
            "Konuşma & Telaffuz",
            TopicExtractor.extractTopic("Konuşma & Telaffuz | Akıcı Konuşma Pratikleri")
        )

        assertEquals(
            "Okuma & Çeviri (Reading)",
            TopicExtractor.extractTopic("Okuma & Çeviri | Kısa Hikayelerle İngilizce")
        )

        assertEquals(
            "Sınav Hazırlığı",
            TopicExtractor.extractTopic("YDS & YÖKDİL Gramer Soru Çözümü #1")
        )
    }

    @Test
    fun `extractTopic falls back appropriately when no keyword matches`() {
        assertEquals(
            "Temel İngilizce",
            TopicExtractor.extractTopic(null, "Temel İngilizce")
        )

        assertEquals(
            "Genel Konular",
            TopicExtractor.extractTopic("Rastgele Bir Video Başlığı", null)
        )
    }
}
