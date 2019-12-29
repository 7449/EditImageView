@file:Suppress("UNCHECKED_CAST")

package com.image.edit

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PointF
import com.davemorrissey.labs.subscaleview.ImageViewState

fun EditImageView.noneAction() = apply { editType = EditType.NONE }

fun EditImageView.setMaxCacheCount(maxCount: Int) = also { maxCacheCount = maxCount }

fun <CACHE : CacheCallback, ACTION : OnEditImageAction<CACHE>> EditImageView.customAction(editImageAction: ACTION) = action(editImageAction).apply { editType = EditType.ACTION }

fun <CACHE : CacheCallback, ACTION : OnEditImageAction<CACHE>> EditImageView.action(editImageAction: ACTION) = apply { onEditImageAction = editImageAction as OnEditImageAction<CacheCallback> }.onEditImageAction as ACTION

fun <CACHE : CacheCallback> OnEditImageAction<CACHE>.createCache(imageViewState: ImageViewState?, imageCache: CACHE) = EditImageCache(imageViewState, this, imageCache) as EditImageCache<CacheCallback>

fun EditImageView.newBitmap(config: Bitmap.Config = Bitmap.Config.ARGB_8888): Bitmap {
    val bitmap = Bitmap.createBitmap(sWidth, sHeight, config)
    val canvas = Canvas(bitmap)
    cacheArrayList.forEach { it.onEditImageAction.onDrawBitmap(this, canvas, it) }
    return bitmap
}

fun EditImageView.newCanvasBitmap(config: Bitmap.Config = Bitmap.Config.ARGB_8888): Bitmap {
    val bitmap = Bitmap.createBitmap(sWidth, sHeight, config)
    val canvas = Canvas(bitmap)
//    canvas.drawBitmap(imageBitmap, 0f, 0f, null)
//    canvas.drawBitmap(newBitmap, 0f, 0f, null)
    canvas.save()
    return bitmap
}

fun EditImageView.viewToSourceCoords(pointF: PointF): PointF {
    return viewToSourceCoord(pointF) ?: throw NullPointerException("PointF == null")
}