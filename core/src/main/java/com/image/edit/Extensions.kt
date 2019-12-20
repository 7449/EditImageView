@file:Suppress("UNCHECKED_CAST")

package com.image.edit

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PointF
import com.davemorrissey.labs.subscaleview.ImageViewState

fun EditImageView.noneAction() = apply { editType = EditType.NONE }

fun EditImageView.setMaxCacheCount(maxCount: Int) = also { maxCacheCount = maxCount }

fun <CACHE : CacheCallback, ACTION : OnEditImageAction<CACHE>> EditImageView.customAction(editImageAction: ACTION) = action(editImageAction).apply { editType = EditType.ACTION }

fun <CACHE : CacheCallback, ACTION : OnEditImageAction<CACHE>> EditImageView.action(editImageAction: ACTION) = apply { onEditImageAction = editImageAction }.onEditImageAction as ACTION

fun <CACHE : CacheCallback> OnEditImageAction<CACHE>.createCache(imageViewState: ImageViewState?, imageCache: CACHE) = EditImageCache(imageViewState, this, imageCache) as EditImageCache<CacheCallback>

fun EditImageView.recycleDrawBitmap() = newBitmap.supportRecycle()

fun EditImageView.newCanvasBitmap(config: Bitmap.Config = Bitmap.Config.ARGB_8888): Bitmap {
    val bitmap = Bitmap.createBitmap(sWidth, sHeight, config)
    val defaultBitmap = getBitmap()
    val canvas = Canvas(bitmap)
    if (defaultBitmap != null) {
        canvas.drawBitmap(defaultBitmap, 0f, 0f, null)
    }
    canvas.drawBitmap(newBitmap, 0f, 0f, null)
    canvas.save()
    return bitmap
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

fun Bitmap?.supportRecycle() {
    if (this?.isRecycled == false) {
        recycle()
    }
}

inline fun <A, B> allNotNull(first: A?, second: B?, block: (A, B) -> Unit) {
    if (first != null && second != null) block(first, second)
}

fun checkCoordinate(startPoint: PointF, endPointF: PointF, upX: Float, upY: Float): Boolean {
    return startPoint.x == upX && startPoint.y == upY && endPointF.x == 0F && endPointF.y == 0F
}