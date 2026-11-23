package com.example.hostelmanager.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.UUID

object ImageStore {

    private const val TAG = "ImageStore"
    private const val IMAGES_DIR = "images"
    private const val TEMP_PREFIX = "capture_"
    private const val MAX_DIMENSION = 1600
    private const val JPEG_QUALITY = 85
    private const val MAX_TEMP_AGE_MS = 24L * 60L * 60L * 1000L

    /**
     * Gives the camera app a temporary file (via FileProvider) to write the photo into.
     * Stale capture files from cancelled attempts are cleaned up first.
     */
    fun createTempCaptureUri(context: Context): Uri {
        cleanupStaleCaptures(context)
        val tempFile = File(context.cacheDir, "$TEMP_PREFIX${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempFile
        )
    }

    /**
     * Copies a photo (from the camera's temp file OR a gallery pick) into permanent
     * app-private storage, downscaling it to keep the database folder small.
     *
     * @return the saved file's absolute path, or null if the image could not be read
     *         or written. Callers should treat null as "no image chosen".
     */
    fun saveImage(context: Context, sourceUri: Uri): String? = runCatching {
        val imagesDir = File(context.filesDir, IMAGES_DIR)
        if (!imagesDir.exists() && !imagesDir.mkdirs()) {
            throw IOException("Could not create the images directory")
        }

        val resolver = context.contentResolver
        val size = resolver.openInputStream(sourceUri)?.use { input ->
            BitmapFactory.Options().apply { inJustDecodeBounds = true }
                .also { BitmapFactory.decodeStream(input, null, it) }
        } ?: throw IOException("Could not open $sourceUri")
        if (size.outWidth <= 0 || size.outHeight <= 0) {
            throw IOException("Image has no pixels")
        }

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSizeFor(size.outWidth, size.outHeight, MAX_DIMENSION)
        }
        val decoded = resolver.openInputStream(sourceUri)?.use { input ->
            BitmapFactory.decodeStream(input, null, decodeOptions)
        } ?: throw IOException("Could not decode $sourceUri")

        val oriented = applyExifRotation(resolver.openInputStream(sourceUri), decoded)

        val destFile = File(imagesDir, "img_${System.currentTimeMillis()}_${UUID.randomUUID()}.jpg")
        try {
            FileOutputStream(destFile).use { output ->
                if (!oriented.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)) {
                    throw IOException("Could not encode the image")
                }
            }
        } catch (error: Throwable) {
            destFile.delete()
            throw error
        } finally {
            oriented.recycle()
        }

        destFile.absolutePath
    }.getOrElse { error ->
        Log.w(TAG, "Could not save image from $sourceUri", error)
        null
    }

    /**
     * Deletes a previously saved image, but only when it lives inside this app's
     * private images directory. Safe to call with blank or already-deleted paths.
     */
    fun deleteImage(context: Context, path: String?) {
        if (path.isNullOrBlank()) return
        runCatching {
            val imagesDir = File(context.filesDir, IMAGES_DIR).canonicalFile
            val image = File(path).canonicalFile
            if (image.parentFile == imagesDir && image.exists()) {
                image.delete()
            }
        }
    }

    private fun cleanupStaleCaptures(context: Context) {
        val cutoff = System.currentTimeMillis() - MAX_TEMP_AGE_MS
        context.cacheDir
            .listFiles { file -> file.name.startsWith(TEMP_PREFIX) && file.lastModified() < cutoff }
            ?.forEach { file -> runCatching { file.delete() } }
    }

    private fun sampleSizeFor(width: Int, height: Int, maxDimension: Int): Int {
        var sample = 1
        while (width / sample > maxDimension || height / sample > maxDimension) {
            sample *= 2
        }
        return sample
    }

    private fun applyExifRotation(input: InputStream?, bitmap: Bitmap): Bitmap {
        val orientation = input?.use { stream ->
            runCatching {
                ExifInterface(stream).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)
        } ?: ExifInterface.ORIENTATION_NORMAL

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.postRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.postRotate(270f)
                matrix.postScale(-1f, 1f)
            }
        }
        if (matrix.isIdentity) return bitmap

        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (rotated !== bitmap) bitmap.recycle()
        return rotated
    }
}
