package com.cocode.babakplayer.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Test

class AboutLinksTest {

    @Test
    fun update_opens_github_release_until_fdroid_lists_the_app() {
        assertEquals(
            "https://github.com/cocodedk/BabakPlayer/releases/latest",
            aboutUrl(AboutLink.Update, "en", onFdroid = false),
        )
    }

    @Test
    fun update_opens_fdroid_page_once_the_app_is_live_there() {
        assertEquals(
            "https://f-droid.org/packages/com.cocode.babakplayer/",
            aboutUrl(AboutLink.Update, "en", onFdroid = true),
        )
    }

    @Test
    fun privacy_link_points_at_the_policy_page_in_every_language() {
        for (language in listOf("en", "fa", "da")) {
            assertEquals("https://player.cocode.dk/privacy/", aboutUrl(AboutLink.Privacy, language, onFdroid = false))
        }
    }

    @Test
    fun website_has_a_persian_front_page_and_an_english_one_for_the_rest() {
        assertEquals("https://player.cocode.dk/fa/", aboutUrl(AboutLink.Website, "fa", onFdroid = false))
        assertEquals("https://player.cocode.dk/", aboutUrl(AboutLink.Website, "en", onFdroid = false))
        assertEquals("https://player.cocode.dk/", aboutUrl(AboutLink.Website, "da", onFdroid = false))
    }

    @Test
    fun source_and_issues_point_at_the_repository() {
        assertEquals("https://github.com/cocodedk/BabakPlayer", aboutUrl(AboutLink.Source, "en", onFdroid = false))
        assertEquals("https://github.com/cocodedk/BabakPlayer/issues", aboutUrl(AboutLink.Issues, "en", onFdroid = false))
    }

    @Test
    fun made_by_opens_cocode() {
        assertEquals("https://cocode.dk", aboutUrl(AboutLink.Cocode, "en", onFdroid = false))
    }
}
