package com.cocode.babakplayer.util

import android.net.Uri

data class SharePayload(
    val uris: List<Uri>,
    val caption: String?,
    val firstDescription: String?,
    val sourceApp: String?,
)
