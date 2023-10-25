// Tencent is pleased to support the open source community by making ncnn available.
//
// Copyright (C) 2020 THL A29 Limited, a Tencent company. All rights reserved.
//
// Licensed under the BSD 3-Clause License (the "License"); you may not use this file except
// in compliance with the License. You may obtain a copy of the License at
//
// https://opensource.org/licenses/BSD-3-Clause
//
// Unless required by applicable law or agreed to in writing, software distributed
// under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
// CONDITIONS OF ANY KIND, either express or implied. See the License for the
// specific language governing permissions and limitations under the License.
package com.tencent.yolov5ncnn

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.AssetManager
import android.graphics.*
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import java.io.IOException


class YoloV5Ncnn {
    external fun Init(mgr: AssetManager?): Boolean
    inner class Obj {
        var x = 0f
        var y = 0f
        var w = 0f
        var h = 0f
        var label: String? = null
        var prob = 0f
    }

    external fun Detect(bitmap: Bitmap?, use_gpu: Boolean): Array<Obj?>?

    companion object {
        init {
            System.loadLibrary("yolov5ncnn")
        }
    }
}

// ok
fun decodeUri(selectedImage: Uri?,context: Context): Bitmap {
    // Decode image size
    val o = BitmapFactory.Options()
    o.inJustDecodeBounds = true

    val contentResolver=context.contentResolver
    BitmapFactory.decodeStream(contentResolver.openInputStream(selectedImage!!), null, o)

    // The new size we want to scale to
    val REQUIRED_SIZE = 640

    // Find the correct scale value. It should be the power of 2.
    var width_tmp = o.outWidth
    var height_tmp = o.outHeight
    var scale = 1
    while (true) {
        if (width_tmp / 2 < REQUIRED_SIZE
            || height_tmp / 2 < REQUIRED_SIZE
        ) {
            break
        }
        width_tmp /= 2
        height_tmp /= 2
        scale *= 2
    }

    // Decode with inSampleSize
    val o2 = BitmapFactory.Options()
    o2.inSampleSize = scale
    val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(selectedImage), null, o2)

    // Rotate according to EXIF
    var rotate = 0
    try {
        val exif = ExifInterface(contentResolver.openInputStream(selectedImage)!!)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_270 -> rotate = 270
            ExifInterface.ORIENTATION_ROTATE_180 -> rotate = 180
            ExifInterface.ORIENTATION_ROTATE_90 -> rotate = 90
        }
    } catch (e: IOException) {
        Log.e("MainActivity", "ExifInterface IOException")
    }
    val matrix = Matrix()
    matrix.postRotate(rotate.toFloat())
    return Bitmap.createBitmap(bitmap!!, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

@SuppressLint("SuspiciousIndentation")
fun showObjects(objects: Array<YoloV5Ncnn.Obj?>?, bitmap: Bitmap):Array<Bitmap> {

    // draw objects on bitmap
    val rgba: Bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
    val colors = intArrayOf(
        Color.rgb(54, 67, 244),
        Color.rgb(99, 30, 233),
        Color.rgb(176, 39, 156),
        Color.rgb(183, 58, 103),
        Color.rgb(181, 81, 63),
        Color.rgb(243, 150, 33),
        Color.rgb(244, 169, 3),
        Color.rgb(212, 188, 0),
        Color.rgb(136, 150, 0),
        Color.rgb(80, 175, 76),
        Color.rgb(74, 195, 139),
        Color.rgb(57, 220, 205),
        Color.rgb(59, 235, 255),
        Color.rgb(7, 193, 255),
        Color.rgb(0, 152, 255),
        Color.rgb(34, 87, 255),
        Color.rgb(72, 85, 121),
        Color.rgb(158, 158, 158),
        Color.rgb(139, 125, 96)
    )
    val image=bitmap.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(rgba)
    val paint = Paint()
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 4f
    val textbgpaint = Paint()
    textbgpaint.color = Color.TRANSPARENT
    textbgpaint.style = Paint.Style.FILL
    val textpaint = Paint()
    textpaint.color = Color.BLACK
    textpaint.textSize = 12f
    textpaint.textAlign = Paint.Align.LEFT

    var results= arrayOf(rgba)
    if (objects != null) {
        for (i in objects.indices) {
            paint.color = colors[i % 19]
                canvas.drawRect(objects[i]!!.x, objects[i]!!.y, objects[i]!!.x + objects[i]!!.w, objects[i]!!.y + objects[i]!!.h, paint)

            // draw filled text inside image
            run {
                val text =
                    objects[i]!!.label + " = " + String.format("%.1f", objects[i]!!.prob * 100) + "%"
                val text_width = textpaint.measureText(text)
                val text_height = -textpaint.ascent() + textpaint.descent()
                var x = objects[i]!!.x
                var y = objects[i]!!.y - text_height
                if (y < 0) y = 0f
                if (x + text_width > rgba.width) x = rgba.width - text_width
                canvas.drawRect(x, y, x + text_width, y + text_height, textbgpaint)
                canvas.drawText(text, x, y - textpaint.ascent(), textpaint)

                val rectBitmap = Bitmap.createBitmap(
                    image,
                    objects[i]!!.x.toInt(),
                    objects[i]!!.y.toInt(),
                    objects[i]!!.w.toInt(),
                    objects[i]!!.h.toInt()
                )
                results += rectBitmap
            }
        }
    }
    return results
}