package com.cocode.babakplayer.model

import org.junit.Assert.assertEquals
import org.junit.Test

class AppLanguageTest {

    @Test
    fun danish_is_a_choosable_language() {
        assertEquals(AppLanguage.DANISH, AppLanguage.fromTag("da"))
        assertEquals("da", AppLanguage.DANISH.tag)
    }

    @Test
    fun every_language_round_trips_through_its_tag() {
        for (language in AppLanguage.entries) {
            assertEquals(language, AppLanguage.fromTag(language.tag))
        }
    }

    @Test
    fun an_unknown_tag_falls_back_to_english() {
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromTag("xx"))
    }
}
