package com.cocode.babakplayer.ui.screens

// Where "Check for updates" points. Only the `foss` build is the one F-Droid publishes, so only
// this flavor ever opens the F-Droid page. Set it to true once F-Droid lists BabakPlayer
// (apps.yml says `fdroid: live`); until then the GitHub release page is the update source.
internal const val UPDATE_ON_FDROID = false
