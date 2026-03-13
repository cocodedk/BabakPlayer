package com.cocode.babakplayer.cast

import android.content.Context
import android.net.Uri
import fi.iki.elonen.NanoHTTPD
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.net.NetworkInterface
import java.util.concurrent.ConcurrentHashMap

class LocalMediaServer(private val context: Context) : NanoHTTPD(0) {

    private data class ServedFile(
        val pathOrUri: String,
        val mimeType: String,
        val size: Long,
    )

    private val registry = ConcurrentHashMap<String, ServedFile>()

    fun registerFile(itemId: String, pathOrUri: String, mimeType: String, size: Long) {
        registry[itemId] = ServedFile(pathOrUri, mimeType, size)
    }

    fun clearRegistry() {
        registry.clear()
    }

    fun getStreamUrl(itemId: String): String? {
        if (!registry.containsKey(itemId)) return null
        val ip = getDeviceIpAddress() ?: return null
        return "http://$ip:$listeningPort/media/$itemId"
    }

    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri
        if (!uri.startsWith("/media/")) {
            return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not found")
        }

        val itemId = uri.removePrefix("/media/")
        val served = registry[itemId]
            ?: return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not found")

        val fileSize = served.size
        val rangeHeader = session.headers["range"]

        return if (rangeHeader != null) {
            servePartial(served, fileSize, rangeHeader)
        } else {
            serveFull(served, fileSize)
        }
    }

    private fun serveFull(served: ServedFile, fileSize: Long): Response {
        val inputStream = openStream(served.pathOrUri)
            ?: return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "Cannot open file")
        return newFixedLengthResponse(Response.Status.OK, served.mimeType, inputStream, fileSize).apply {
            addHeader("Accept-Ranges", "bytes")
            addHeader("Content-Length", fileSize.toString())
        }
    }

    private fun servePartial(served: ServedFile, fileSize: Long, rangeHeader: String): Response {
        val rangeValue = rangeHeader.replace("bytes=", "").trim()
        val parts = rangeValue.split("-")
        val start = parts[0].toLongOrNull() ?: 0L
        val end = if (parts.size > 1 && parts[1].isNotEmpty()) {
            parts[1].toLongOrNull() ?: (fileSize - 1)
        } else {
            fileSize - 1
        }

        val contentLength = end - start + 1
        val inputStream = openStream(served.pathOrUri)
            ?: return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "Cannot open file")

        inputStream.skip(start)

        return newFixedLengthResponse(
            Response.Status.PARTIAL_CONTENT,
            served.mimeType,
            inputStream,
            contentLength,
        ).apply {
            addHeader("Accept-Ranges", "bytes")
            addHeader("Content-Range", "bytes $start-$end/$fileSize")
            addHeader("Content-Length", contentLength.toString())
        }
    }

    private fun openStream(pathOrUri: String): InputStream? {
        return try {
            val parsed = Uri.parse(pathOrUri)
            if (parsed.scheme.isNullOrBlank() || parsed.scheme == "file") {
                val filePath = if (parsed.scheme == "file") parsed.path!! else pathOrUri
                FileInputStream(File(filePath))
            } else {
                context.contentResolver.openInputStream(parsed)
            }
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        fun getDeviceIpAddress(): String? {
            try {
                val interfaces = NetworkInterface.getNetworkInterfaces() ?: return null
                for (intf in interfaces) {
                    if (!intf.isUp || intf.isLoopback) continue
                    for (addr in intf.inetAddresses) {
                        if (addr.isLoopbackAddress) continue
                        val hostAddress = addr.hostAddress ?: continue
                        // IPv4 only
                        if (!hostAddress.contains(':')) return hostAddress
                    }
                }
            } catch (_: Exception) {
            }
            return null
        }
    }
}
