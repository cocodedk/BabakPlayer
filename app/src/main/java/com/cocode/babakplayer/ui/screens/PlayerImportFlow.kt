package com.cocode.babakplayer.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.OpenMultipleDocuments
import androidx.activity.result.contract.ActivityResultContracts.PickMultipleVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.cocode.babakplayer.R
import com.cocode.babakplayer.data.local.BrowsableMedia
import com.cocode.babakplayer.data.local.MediaBrowseCategory
import com.cocode.babakplayer.data.local.MediaStoreBrowser
import com.cocode.babakplayer.ui.components.ImportMediaBottomSheet
import com.cocode.babakplayer.ui.components.ImportOption
import com.cocode.babakplayer.ui.components.MediaSelectionBottomSheet
import com.cocode.babakplayer.ui.components.selectedUrisInBrowseOrder
import com.cocode.babakplayer.ui.components.toCategoryOrNull
import com.cocode.babakplayer.util.hasMediaPermissions
import com.cocode.babakplayer.util.requiredPermissionsFor
import kotlinx.coroutines.launch

@Composable
fun rememberImportFromDeviceAction(onImportFromDevice: (List<Uri>) -> Unit): () -> Unit {
    val context = LocalContext.current
    val browser = remember(context) { MediaStoreBrowser(context) }
    val scope = rememberCoroutineScope()

    var showImportMenu by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var pendingPermissionFor by remember { mutableStateOf<MediaBrowseCategory?>(null) }
    var browseCategory by remember { mutableStateOf<MediaBrowseCategory?>(null) }
    var mediaItems by remember { mutableStateOf<List<BrowsableMedia>>(emptyList()) }
    var selectedUris by remember { mutableStateOf<Set<String>>(emptySet()) }
    var isLoading by remember { mutableStateOf(false) }

    suspend fun loadCategory(category: MediaBrowseCategory) {
        browseCategory = category
        selectedUris = emptySet()
        isLoading = true
        try {
            mediaItems = browser.query(category)
        } finally {
            isLoading = false
        }
    }

    fun closeBrowseSheet() {
        browseCategory = null
        mediaItems = emptyList()
        selectedUris = emptySet()
        isLoading = false
    }

    val permissionLauncher = rememberLauncherForActivityResult(RequestMultiplePermissions()) { results ->
        val category = pendingPermissionFor
        pendingPermissionFor = null
        if (category == null) return@rememberLauncherForActivityResult
        val allGranted = results.values.all { it }
        if (!allGranted) {
            showPermissionDialog = true
            return@rememberLauncherForActivityResult
        }
        scope.launch { loadCategory(category) }
    }

    val manualPickerLauncher = rememberLauncherForActivityResult(OpenMultipleDocuments()) { uris ->
        if (uris.isNotEmpty()) onImportFromDevice(uris)
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(PickMultipleVisualMedia()) { uris ->
        if (uris.isNotEmpty()) onImportFromDevice(uris)
    }

    fun requestOrLoadCategory(category: MediaBrowseCategory) {
        if (hasMediaPermissions(context, category)) {
            scope.launch { loadCategory(category) }
            return
        }
        pendingPermissionFor = category
        permissionLauncher.launch(requiredPermissionsFor(category))
    }

    fun handleImportOption(option: ImportOption) {
        showImportMenu = false
        when (option) {
            ImportOption.VIDEOS -> {
                videoPickerLauncher.launch(PickVisualMediaRequest(PickVisualMedia.VideoOnly))
            }

            ImportOption.MANUAL -> {
                manualPickerLauncher.launch(arrayOf("audio/*", "video/*"))
            }

            else -> option.toCategoryOrNull()?.let(::requestOrLoadCategory)
        }
    }

    if (showImportMenu) {
        ImportMediaBottomSheet(
            onDismiss = { showImportMenu = false },
            onSelect = ::handleImportOption,
        )
    }

    browseCategory?.let { category ->
        MediaSelectionBottomSheet(
            category = category,
            mediaItems = mediaItems,
            selectedUris = selectedUris,
            loading = isLoading,
            onDismiss = ::closeBrowseSheet,
            onToggle = { uriKey ->
                selectedUris = if (selectedUris.contains(uriKey)) selectedUris - uriKey else selectedUris + uriKey
            },
            onCreatePlaylist = {
                val orderedUris = selectedUrisInBrowseOrder(mediaItems, selectedUris)
                if (orderedUris.isNotEmpty()) onImportFromDevice(orderedUris)
                closeBrowseSheet()
            },
        )
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text(stringResource(R.string.media_permission_denied_title)) },
            text = { Text(stringResource(R.string.media_permission_denied_message)) },
            confirmButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text(stringResource(R.string.action_ok))
                }
            },
        )
    }

    return { showImportMenu = true }
}
