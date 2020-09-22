package com.image.edit.virtual

import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log

/**
 * 获取创建的bitmap
 */
interface OnEditImageBitmap : OnEditImageObj {
    /**
     * [Bitmap]模式下返回目标View的图像Bitmap
     */
    val viewBitmap: Bitmap?
        get() = null

    /**
     * 获取痕迹Bitmap
     */
    val newMergeBitmap: Bitmap
        get() {
            val bitmap = Bitmap.createBitmap(bitmapHeightAndHeight.x, bitmapHeightAndHeight.y, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            onCanvasBitmap(canvas)
            return bitmap
        }

    /**
     * 获取目标View的Bitmap和痕迹Bitmap合并之后的Bitmap
     */
    val newMergeCanvasBitmap: Bitmap
        get() {
            val bitmap = Bitmap.createBitmap(bitmapHeightAndHeight.x, bitmapHeightAndHeight.y, Bitmap.Config.ARGB_8888)
            val newBitmap = Bitmap.createBitmap(bitmapHeightAndHeight.x, bitmapHeightAndHeight.y, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val newCanvas = Canvas(newBitmap)
            if (viewBitmap == null) {
                Log.w("OnEditImageCallback", "Bitmap == null")
            }
            viewBitmap?.let { canvas.drawBitmap(it, 0f, 0f, null) }
            onCanvasBitmap(newCanvas)
            canvas.drawBitmap(newBitmap, 0f, 0f, null)
            return bitmap
        }
}