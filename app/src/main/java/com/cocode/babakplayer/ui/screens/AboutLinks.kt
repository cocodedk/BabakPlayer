package com.cocode.babakplayer.ui.screens

enum class AboutLink { Update, Website, Privacy, Source, Issues, Cocode }

private const val SITE = "https://player.cocode.dk/"
private const val REPO = "https://github.com/cocodedk/BabakPlayer"
private const val FDROID_PAGE = "https://f-droid.org/packages/com.cocode.babakplayer/"

/**
 * Languages the site has both a home page and a privacy page for, at `<site><code>/` and
 * `<site><code>/privacy/`. Persian has a home page (fa/) but no privacy page yet, so it opens the
 * English pages until fa/privacy/ exists; add "fa" here then.
 */
private val SITE_LANGUAGES = setOf("da")

/** A site page in the app's language, or the English page when the site has no pages in that language. */
private fun sitePage(language: String, path: String = ""): String =
    if (language in SITE_LANGUAGES) "$SITE$language/$path" else "$SITE$path"

/**
 * The address behind each About button. The app never contacts any of them itself: the browser
 * opens them when the person taps a button, and nothing here checks for updates over the network.
 * The website and privacy pages follow [language] (a code such as "da" from the app's current locale).
 */
fun aboutUrl(link: AboutLink, language: String, onFdroid: Boolean = UPDATE_ON_FDROID): String =
    when (link) {
        AboutLink.Update -> if (onFdroid) FDROID_PAGE else "$REPO/releases/latest"
        AboutLink.Website -> sitePage(language)
        AboutLink.Privacy -> sitePage(language, "privacy/")
        AboutLink.Source -> REPO
        AboutLink.Issues -> "$REPO/issues"
        AboutLink.Cocode -> "https://cocode.dk"
    }
