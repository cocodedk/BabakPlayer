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
    fun privacy_opens_the_english_policy_in_english() {
        assertEquals("https://player.cocode.dk/privacy/", aboutUrl(AboutLink.Privacy, "en", onFdroid = false))
    }

    @Test
    fun privacy_opens_the_danish_policy_in_danish() {
        assertEquals("https://player.cocode.dk/da/privacy/", aboutUrl(AboutLink.Privacy, "da", onFdroid = false))
    }

    @Test
    fun privacy_falls_back_to_the_english_policy_in_a_language_the_site_lacks() {
        // Persian has no privacy page on the site yet.
        for (language in listOf("fa", "de")) {
            assertEquals("https://player.cocode.dk/privacy/", aboutUrl(AboutLink.Privacy, language, onFdroid = false))
        }
    }

    @Test
    fun website_opens_the_english_site_in_english() {
        assertEquals("https://player.cocode.dk/", aboutUrl(AboutLink.Website, "en", onFdroid = false))
    }

    @Test
    fun website_opens_the_danish_site_in_danish() {
        assertEquals("https://player.cocode.dk/da/", aboutUrl(AboutLink.Website, "da", onFdroid = false))
    }

    @Test
    fun website_falls_back_to_the_english_site_in_a_language_the_site_lacks() {
        for (language in listOf("fa", "de")) {
            assertEquals("https://player.cocode.dk/", aboutUrl(AboutLink.Website, language, onFdroid = false))
        }
    }

    @Test
    fun source_and_issues_point_at_the_repository_in_every_language() {
        for (language in listOf("en", "da", "fa")) {
            assertEquals("https://github.com/cocodedk/BabakPlayer", aboutUrl(AboutLink.Source, language, onFdroid = false))
            assertEquals(
                "https://github.com/cocodedk/BabakPlayer/issues",
                aboutUrl(AboutLink.Issues, language, onFdroid = false),
            )
        }
    }

    @Test
    fun made_by_opens_cocode() {
        assertEquals("https://cocode.dk", aboutUrl(AboutLink.Cocode, "en", onFdroid = false))
    }
}
