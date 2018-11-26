package com.image.edit.helper

import android.graphics.Bitmap

/**
 * @author y
 * @create 2018/11/20
 */
object BitmapHelper {

    fun recycle(bitmap: Bitmap?) {
        if (bitmap != null && !bitmap.isRecycled) {
            bitmap.recycle()
        }
    }

}
