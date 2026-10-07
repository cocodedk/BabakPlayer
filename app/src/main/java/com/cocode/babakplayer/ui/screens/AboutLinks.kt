package com.cocode.babakplayer.ui.screens

enum class AboutLink { Update, Website, Privacy, Source, Issues, Cocode }

private const val SITE = "https://player.cocode.dk/"
private const val REPO = "https://github.com/cocodedk/BabakPlayer"
private const val FDROID_PAGE = "https://f-droid.org/packages/com.cocode.babakplayer/"

/** Languages the site has a home page for, at `<site><code>/`. */
private val HOME_LANGUAGES = setOf("da", "fa")

/**
 * Languages the site has a privacy page for, at `<site><code>/privacy/`. Persian has none yet, so a
 * Persian user gets the English privacy page; add "fa" here once fa/privacy/ exists.
 */
private val PRIVACY_LANGUAGES = setOf("da")

/** A site page in the app's language when [available] has it, otherwise the English page. */
private fun sitePage(language: String, available: Set<String>, path: String = ""): String =
    if (language in available) "$SITE$language/$path" else "$SITE$path"

/**
 * The address behind each About button. The app never contacts any of them itself: the browser
 * opens them when the person taps a button, and nothing here checks for updates over the network.
 * The website and privacy pages follow [language] (a code such as "da" from the app's current locale).
 */
fun aboutUrl(link: AboutLink, language: String, onFdroid: Boolean = UPDATE_ON_FDROID): String =
    when (link) {
        AboutLink.Update -> if (onFdroid) FDROID_PAGE else "$REPO/releases/latest"
        AboutLink.Website -> sitePage(language, HOME_LANGUAGES)
        AboutLink.Privacy -> sitePage(language, PRIVACY_LANGUAGES, "privacy/")
        AboutLink.Source -> REPO
        AboutLink.Issues -> "$REPO/issues"
        AboutLink.Cocode -> "https://cocode.dk"
    }
