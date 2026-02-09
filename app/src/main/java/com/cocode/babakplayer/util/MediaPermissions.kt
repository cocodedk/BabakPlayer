package com.cocode.babakplayer.util

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import com.cocode.babakplayer.data.local.MediaBrowseCategory

fun requiredPermissionsFor(category: MediaBrowseCategory): Array<String> {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        return arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    return when (category) {
        MediaBrowseCategory.AUDIO -> arrayOf(Manifest.permission.READ_MEDIA_AUDIO)
        MediaBrowseCategory.WHATSAPP,
        MediaBrowseCategory.DOWNLOADS,
        -> arrayOf(Manifest.permission.READ_MEDIA_AUDIO, Manifest.permission.READ_MEDIA_VIDEO)
    }
}

fun hasMediaPermissions(context: Context, category: MediaBrowseCategory): Boolean {
    return requiredPermissionsFor(category).all { permission ->
        ContextCompat.checkSelfPermission(context, permission) == PERMISSION_GRANTED
    }
}
