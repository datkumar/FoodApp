package com.phoenix.tech.foodapp.utils

import android.content.Context
import android.net.Uri
import java.io.File

object ProfileImageUtils {
    fun getStagedFile(context: Context): File {
        return File(context.cacheDir, "staged_profile_pic.jpg")
    }

    fun getPermanentFile(context: Context): File {
        return File(context.filesDir, "permanent_profile_pic.jpg")
    }

    fun copyUriToFile(context: Context, uri: Uri, targetFile: File) {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            targetFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }
}