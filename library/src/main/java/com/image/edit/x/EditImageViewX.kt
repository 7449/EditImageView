@file:Suppress("FunctionName")

package com.image.edit.x

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PointF
import com.davemorrissey.labs.subscaleview.api.getBitmap
import com.image.edit.EditImageView
import com.image.edit.action.OnEditImageAction
import com.image.edit.type.EditType

/**
 * @author y
 * @create 2019/3/18
 */

fun EditImageView.recycleDrawBitmap() = newBitmap.supportRecycle()

fun EditImageView.newCanvasBitmap(): Bitmap {
    val bitmap = Bitmap.createBitmap(sWidth, sHeight, Bitmap.Config.ARGB_8888)
    val defaultBitmap = getBitmap()
    val canvas = Canvas(bitmap)
    if (defaultBitmap != null) {
        canvas.drawBitmap(defaultBitmap, 0f, 0f, null)
    }
    canvas.drawBitmap(newBitmap, 0f, 0f, null)
    canvas.save()
    return bitmap
}

fun EditImageView.reset() {
    recycleDrawBitmap()
    newBitmap = Bitmap.createBitmap(sWidth, sHeight, Bitmap.Config.ARGB_8888)
    newBitmapCanvas.setBitmap(newBitmap)
}

fun EditImageView.clearImage() {
    if (cacheArrayList.isEmpty()) {
        onEditImageListener?.onLastImageEmpty()
        return
    }
    for (editImageCache in cacheArrayList) {
        editImageCache.reset()
    }
    cacheArrayList.clear()
    reset()
    editType = EditType.NONE
}

fun EditImageView.lastImage() {
    if (cacheArrayList.isEmpty()) {
        onEditImageListener?.onLastImageEmpty()
        return
    }
    cacheArrayList.removeLast().reset()
    reset()
    for (editImageCache in cacheArrayList) {
        editImageCache.onEditImageAction?.onLastImageCache(this, editImageCache)
    }
    editType = EditType.NONE
}

fun EditImageView.refresh() = invalidate()

fun Bitmap?.supportRecycle() {
    if (this?.isRecycled == false) {
        recycle()
    }
}

inline fun <A, B> AllNotNull(first: A?, second: B?, block: (A, B) -> Unit) {
    if (first != null && second != null) block(first, second)
}

@Suppress("NOTHING_TO_INLINE")
inline fun OnEditImageAction.checkCoordinate(startPoint: PointF, endPointF: PointF, upX: Float, upY: Float): Boolean {
    return startPoint.x == upX && startPoint.y == upY && endPointF.x == 0F && endPointF.y == 0F
}

