package com.cocode.babakplayer.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// The `foss` flavor ships no Cast SDK, so there is nothing to route to -- render
// nothing rather than a button that can't do anything.
@Composable
fun CastButton(@Suppress("UNUSED_PARAMETER") modifier: Modifier = Modifier) {
}
