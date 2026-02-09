package com.cocode.babakplayer.util

import android.content.Intent
import android.net.Uri
import androidx.core.content.IntentCompat

object ShareIntentParser {
    fun parse(intent: Intent?, sourcePackage: String?): SharePayload? {
        if (intent == null) return null
        val action = intent.action ?: return null
        if (action != Intent.ACTION_SEND && action != Intent.ACTION_SEND_MULTIPLE) return null

        val uris = when (action) {
            Intent.ACTION_SEND -> parseSingle(intent)
            Intent.ACTION_SEND_MULTIPLE -> parseMultiple(intent)
            else -> emptyList()
        }
        if (uris.isEmpty()) return null

        val firstDescription = extractFirstDescription(intent)
        val caption = intent.getStringExtra(Intent.EXTRA_TEXT)?.trim().takeIf { !it.isNullOrBlank() }
        return SharePayload(
            uris = uris,
            caption = caption,
            firstDescription = firstDescription,
            sourceApp = normalizeSourceApp(sourcePackage),
        )
    }

    private fun parseSingle(intent: Intent): List<Uri> {
        val uri = IntentCompat.getParcelableExtra(intent, Intent.EXTRA_STREAM, Uri::class.java)
            ?: intent.clipData?.getItemAt(0)?.uri
        return listOfNotNull(uri)
    }

    private fun parseMultiple(intent: Intent): List<Uri> {
        val fromExtra = IntentCompat.getParcelableArrayListExtra(intent, Intent.EXTRA_STREAM, Uri::class.java)
            .orEmpty()
        if (fromExtra.isNotEmpty()) return fromExtra
        return buildList {
            val clipData = intent.clipData ?: return@buildList
            for (index in 0 until clipData.itemCount) {
                clipData.getItemAt(index).uri?.let(::add)
            }
        }
    }

    private fun extractFirstDescription(intent: Intent): String? {
        val clipData = intent.clipData ?: return null
        val itemText = clipData.getItemAt(0).text?.toString()?.trim()
        if (!itemText.isNullOrBlank()) return itemText
        val label = clipData.description?.label?.toString()?.trim()
        return label?.takeIf { it.isNotBlank() }
    }

    private fun normalizeSourceApp(sourcePackage: String?): String? {
        val source = sourcePackage?.lowercase() ?: return null
        return when {
            "whatsapp" in source -> "whatsapp"
            else -> "unknown"
        }
    }
}
