package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object ImageStorageHelper {

    /**
     * Creates a temporary file in internal cache and returns both the FileProvider Uri (for camera intent)
     * and the destination file's absolute path.
     */
    fun createTempImageUri(context: Context, prefix: String = "order"): Pair<Uri, String>? {
        return try {
            val photosDir = File(context.filesDir, "order_photos")
            if (!photosDir.exists()) photosDir.mkdirs()

            val fileName = "${prefix}_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
            val destFile = File(photosDir, fileName)
            if (!destFile.exists()) {
                destFile.createNewFile()
            }

            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, destFile)
            Pair(uri, destFile.absolutePath)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Copies a Uri (from gallery or camera) into app internal storage and returns the local file path.
     */
    fun saveImageFromUri(context: Context, sourceUri: Uri, prefix: String = "photo"): String? {
        return try {
            val photosDir = File(context.filesDir, "order_photos")
            if (!photosDir.exists()) photosDir.mkdirs()

            val fileName = "${prefix}_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
            val destFile = File(photosDir, fileName)

            val inputStream: InputStream? = context.contentResolver.openInputStream(sourceUri)
            val outputStream = FileOutputStream(destFile)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Saves a Bitmap (e.g. from camera thumbnail or capture) to internal storage.
     */
    fun saveBitmap(context: Context, bitmap: Bitmap, prefix: String = "photo"): String? {
        return try {
            val photosDir = File(context.filesDir, "order_photos")
            if (!photosDir.exists()) photosDir.mkdirs()

            val fileName = "${prefix}_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
            val destFile = File(photosDir, fileName)

            val outputStream = FileOutputStream(destFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()
            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Saves workshop logo to internal storage.
     */
    fun saveWorkshopLogo(context: Context, sourceUri: Uri): String? {
        return saveImageFromUri(context, sourceUri, "workshop_logo")
    }
}

