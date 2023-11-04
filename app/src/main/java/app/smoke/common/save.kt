package app.smoke.common

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.RequiresApi
import java.io.File


@RequiresApi(Build.VERSION_CODES.Q)
fun saveImage(context: Context, bitmap: Bitmap, extension: String, format: Bitmap.CompressFormat, quality: Int): Uri {
    val imagesCollection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    val destDir = File(Environment.DIRECTORY_PICTURES, "Smoke")
    val date = System.currentTimeMillis()

    val newImage = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "$date.$extension")
        put(MediaStore.MediaColumns.MIME_TYPE, "image/$extension")
        put(MediaStore.MediaColumns.DATE_ADDED, date)
        put(MediaStore.MediaColumns.DATE_MODIFIED, date)
        put(MediaStore.MediaColumns.SIZE, bitmap.byteCount)
        put(MediaStore.MediaColumns.WIDTH, bitmap.width)
        put(MediaStore.MediaColumns.HEIGHT, bitmap.height)
        put(MediaStore.MediaColumns.RELATIVE_PATH, "$destDir${File.separator}")
        put(MediaStore.Images.Media.IS_PENDING, 1)
    }
    val newImageUri = context.contentResolver.insert(imagesCollection, newImage)
    context.contentResolver.openOutputStream(newImageUri!!).use {
        if (it != null) {
            bitmap.compress(format, quality, it)
        }
    }
    newImage.clear()
    newImage.put(MediaStore.Images.Media.IS_PENDING, 0)
    context.contentResolver.update(newImageUri, newImage, null, null)
    Toast.makeText(context, "image saved to Pictures/Smoke", Toast.LENGTH_SHORT).show()
    return newImageUri
}
