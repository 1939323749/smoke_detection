package app.smoke.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

fun saveImage(context: Context, uri: Uri?, name:String?):Boolean{
    if (uri==null){
        return false
    }
    val bitmap = BitmapFactory.decodeFile(uri.path )
    val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), (name ?: Date().toString()) +".jpg")

    try {
        val fos = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        fos.flush()
        fos.close()

        // Update the gallery
        MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), null, null)
    } catch (e: IOException) {
        e.printStackTrace()
        return false
    }
    return true
}
