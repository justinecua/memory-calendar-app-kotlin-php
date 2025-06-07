package com.example.sample.utils

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileOutputStream

fun getFileFromUri(context: Context, uri: Uri): File? {
    return try {
        val contentResolver = context.contentResolver
        val inputStream = contentResolver.openInputStream(uri) ?: return null

        val mimeType = contentResolver.getType(uri)
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "jpg"

        val tempFile = File.createTempFile("upload_", ".$extension", context.cacheDir)

        FileOutputStream(tempFile).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        inputStream.close()

        tempFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
