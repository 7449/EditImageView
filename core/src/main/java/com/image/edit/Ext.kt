@file:Suppress("UNCHECKED_CAST")

package com.image.edit

import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log

fun OnEditImageCallback.clearImage() {
    if (isCacheEmpty) {
        onEditImageListener?.onLastImageEmpty()
        return
    }
    removeAllCache()
    noneAction()
}

fun OnEditImageCallback.lastImage() {
    if (isCacheEmpty) {
        onEditImageListener?.onLastImageEmpty()
        return
    }
    removeLastCache()
    noneAction()
}

fun OnEditImageCallback.newBitmap(config: Bitmap.Config = Bitmap.Config.ARGB_8888): Bitmap {
    val bitmap = Bitmap.createBitmap(bitmapHeightAndHeight.x, bitmapHeightAndHeight.y, config)
    val canvas = Canvas(bitmap)
    onCanvasBitmap(canvas)
    return bitmap
}

fun OnEditImageCallback.newCanvasBitmap(config: Bitmap.Config = Bitmap.Config.ARGB_8888): Bitmap {
    val bitmap = Bitmap.createBitmap(bitmapHeightAndHeight.x, bitmapHeightAndHeight.y, config)
    val canvas = Canvas(bitmap)
    if (supportBitmap == null) {
        Log.w("OnEditImageCallback", "Bitmap == null")
    }
    supportBitmap?.let { canvas.drawBitmap(it, 0f, 0f, null) }
    onCanvasBitmap(canvas)
    return bitmap
}

fun OnEditImageCallback.noneAction() = also { viewEditType = EditType.NONE }

fun OnEditImageCallback.editTypeAction() = also { viewEditType = EditType.ACTION }

fun OnEditImageCallback.customAction(editImageAction: OnEditImageAction) = action(editImageAction)

fun OnEditImageCallback.action(editImageAction: OnEditImageAction) = also {
    if (isMaxCacheCount) {
        onEditImageListener?.onLastCacheMax()
        return@also
    }
    onEditImageAction = editImageAction
    editTypeAction()
}.onEditImageAction

fun <T> OnEditImageCallback.findObj1() = obj1 as T?

fun <T> OnEditImageCallback.findObj2() = obj2 as T?

fun <T> OnEditImageCallback.findObj3() = obj3 as T?

fun <T> OnEditImageCallback.findObj4() = obj4 as T?

fun <T> OnEditImageCallback.findObj5() = obj5 as T?

fun <T> EditImageCache.findObj1() = obj1 as T?

fun <T> EditImageCache.findObj2() = obj2 as T?

fun <T> EditImageCache.findObj3() = obj3 as T?

fun <T> EditImageCache.findObj4() = obj4 as T?

fun <T> EditImageCache.findObj5() = obj5 as T?

fun <T> EditImageCache.findCache() = imageCache as T

fun OnEditImageAction.createCache(callback: OnEditImageCallback, imageCache: Any): EditImageCache {
    return EditImageCache(callback.obj1, callback.obj2, callback.obj3, callback.obj4, callback.obj5, copy(), imageCache)
}