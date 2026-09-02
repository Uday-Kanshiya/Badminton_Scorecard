package com.badminton.scorecard.core.sharing

import android.app.Activity
import android.content.ClipData
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object ShareUtils {
    
    fun saveBitmapAndGetUri(
        context: Context,
        bitmap: Bitmap,
        fileName: String = "match_summary_${System.currentTimeMillis()}.png"
    ): Uri {
        val cachePath = File(context.cacheDir, "shared_images")
        cachePath.mkdirs()
        val file = File(cachePath, fileName)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    /**
     * Saves bitmap directly to device Pictures/Gallery so user can access it without external apps.
     */
    fun saveBitmapToGallery(
        context: Context,
        bitmap: Bitmap,
        title: String = "Badminton Scorecard"
    ): Uri? {
        val fileName = "Badminton_${System.currentTimeMillis()}.png"
        return try {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/BadmintonScorecard")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                }
            }
            uri
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    fun shareImage(
        context: Context,
        imageUri: Uri,
        title: String = "Badminton Match Summary"
    ) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, "🏸 Check out this Badminton match summary!")
            clipData = ClipData.newRawUri(title, imageUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // Explicitly grant read permissions to all candidate target activities
        val resInfoList = context.packageManager.queryIntentActivities(shareIntent, PackageManager.MATCH_DEFAULT_ONLY)
        for (resolveInfo in resInfoList) {
            val packageName = resolveInfo.activityInfo.packageName
            try {
                context.grantUriPermission(packageName, imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: Exception) {
                // Ignore per-package errors
            }
        }

        val chooser = Intent.createChooser(shareIntent, title).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (context !is Activity) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        context.startActivity(chooser)
    }
}
