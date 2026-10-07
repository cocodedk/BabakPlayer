package com.cocode.babakplayer.ui.screens

enum class AboutLink { Update, Website, Privacy, Source, Issues, Cocode }

private const val SITE = "https://player.cocode.dk/"
private const val REPO = "https://github.com/cocodedk/BabakPlayer"
private const val FDROID_PAGE = "https://f-droid.org/packages/com.cocode.babakplayer/"

/**
 * The address behind each About button. The app never contacts any of them itself: the browser
 * opens them when the person taps a button, and nothing here checks for updates over the network.
 * The Persian site has its own front page; the privacy policy has a single page.
 */
fun aboutUrl(link: AboutLink, language: String, onFdroid: Boolean = UPDATE_ON_FDROID): String =
    when (link) {
        AboutLink.Update -> if (onFdroid) FDROID_PAGE else "$REPO/releases/latest"
        AboutLink.Website -> if (language == "fa") SITE + "fa/" else SITE
        AboutLink.Privacy -> "https://player.cocode.dk/privacy/"
        AboutLink.Source -> REPO
        AboutLink.Issues -> "$REPO/issues"
        AboutLink.Cocode -> "https://cocode.dk"
    }
