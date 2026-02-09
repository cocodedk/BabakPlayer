package com.cocode.babakplayer.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TitleResolver {
    fun resolve(
        firstDescription: String?,
        caption: String?,
        firstFileName: String?,
        createdAtMs: Long,
    ): String {
        val desc = firstDescription?.trim().takeIf { !it.isNullOrBlank() }
        if (desc != null) return desc

        val cap = caption?.trim().takeIf { !it.isNullOrBlank() }
        if (cap != null) return cap

        val byName = firstFileName?.let(::fileNameWithoutExtension)?.trim().takeIf { !it.isNullOrBlank() }
        if (byName != null) return byName

        val stamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(createdAtMs))
        val prefix = if (Locale.getDefault().language == "fa") "پلی‌لیست واردشده" else "Imported playlist"
        return "$prefix $stamp"
    }
}
