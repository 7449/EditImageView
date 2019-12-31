@file:Suppress("UNCHECKED_CAST")

package com.image.edit

import android.graphics.Bitmap
import android.graphics.Canvas
import com.image.edit.impl.EditSubsamplingScaleImageView


@Deprecated("")
fun EditSubsamplingScaleImageView.newBitmap(config: Bitmap.Config = Bitmap.Config.ARGB_8888): Bitmap {
    val bitmap = Bitmap.createBitmap(sWidth, sHeight, config)
    val canvas = Canvas(bitmap)
    onCanvasBitmap(canvas)
    return bitmap
}

@Deprecated("")
fun EditSubsamplingScaleImageView.newCanvasBitmap(config: Bitmap.Config = Bitmap.Config.ARGB_8888): Bitmap {
    val bitmap = Bitmap.createBitmap(sWidth, sHeight, config)
    val canvas = Canvas(bitmap)
//    canvas.drawBitmap(imageBitmap, 0f, 0f, null)
//    canvas.drawBitmap(newBitmap, 0f, 0f, null)
    canvas.save()
    return bitmap
}