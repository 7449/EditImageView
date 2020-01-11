@file:Suppress("UNCHECKED_CAST")

package com.image.edit

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PointF
import android.util.Log

/**
 * 清除所有绘制痕迹
 */
fun OnEditImageCallback.clearImage() {
    if (isCacheEmpty) {
        onEditImageListener?.onLastImageEmpty()
        return
    }
    removeAllCache()
    noneAction()
}

/**
 * 回退绘制痕迹
 */
fun OnEditImageCallback.lastImage() {
    if (isCacheEmpty) {
        onEditImageListener?.onLastImageEmpty()
        return
    }
    removeLastCache()
    noneAction()
}

/**
 * 获取痕迹Bitmap
 */
val OnEditImageCallback.newBitmap: Bitmap
    get() {
        val bitmap = Bitmap.createBitmap(bitmapHeightAndHeight.x, bitmapHeightAndHeight.y, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        onCanvasBitmap(canvas)
        return bitmap
    }

/**
 * 获取目标View的Bitmap和痕迹Bitmap合并之后的Bitmap
 */
val OnEditImageCallback.newCanvasBitmap: Bitmap
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

/**
 * 返回最终使用的Canvas
 */
fun OnEditImageCallback.finalCanvas(viewCanvas: Canvas): Canvas {
    if (!intelligent || supportCanvas == null || viewBitmap == null) {
        return viewCanvas
    }
    return supportCanvas ?: throw KotlinNullPointerException("supportCanvas == null")
}

/**
 * 检查最终使用的Canvas
 * true:不可使用橡皮擦，因为返回的是目标view的[Canvas]
 * 应传入目标View的[Canvas]而不是[OnEditImageCallback.supportCanvas]
 * true 则 intelligent 为 false 或 supportCanvas supportBitmap 为 null
 */
fun OnEditImageCallback.checkCanvas(viewCanvas: Canvas): Boolean {
    return viewCanvas == finalCanvas(viewCanvas)
}

/**
 * 返回最终作用在Canvas上的坐标
 * 如果是目标View的Canvas则转换坐标,反之直接返回
 */
fun OnEditImageCallback.finalSourceToViewCoord(canvas: Canvas, source: PointF, target: PointF) {
    if (checkCanvas(canvas)) {
        onSourceToViewCoord(source, target)
    } else {
        target.x = source.x
        target.y = source.y
    }
}

/**
 * 返回最终作用在Canvas上的坐标
 * 如果是目标View的Canvas则直接返回,反之转换坐标
 */
fun OnEditImageCallback.finalViewToSourceCoord(canvas: Canvas, source: PointF, target: PointF) {
    if (!checkCanvas(canvas)) {
        onViewToSourceCoord(source, target)
    } else {
        target.x = source.x
        target.y = source.y
    }
}

/**
 * 返回最终作用在Canvas上的参数
 */
fun OnEditImageCallback.finalParameter(canvas: Canvas, cacheScale: Float, target: Float): Float {
    return if (checkCanvas(canvas)) {
        when {
            cacheScale == viewScale -> target
            cacheScale > viewScale -> target / (cacheScale / viewScale)
            else -> target * (viewScale / cacheScale)
        }
    } else {
        target / cacheScale
    }
}

/**
 * 返回最终作用在Canvas上的参数
 */
fun OnEditImageCallback.finalParameterNo(canvas: Canvas, cacheScale: Float, target: Float): Float {
    return if (checkCanvas(canvas)) {
        when {
            cacheScale == viewScale -> target
            cacheScale > viewScale -> target / (cacheScale / viewScale)
            else -> target * (viewScale / cacheScale)
        }
    } else {
        target
    }
}

/**
 * 退出编辑模式
 */
fun OnEditImageCallback.noneAction() = also { viewEditType = EditType.NONE }

/**
 * 进入编辑模式
 */
fun OnEditImageCallback.editTypeAction() = also { viewEditType = EditType.ACTION }

/**
 * 自定义绘制回调
 */
fun OnEditImageCallback.customAction(editImageAction: OnEditImageAction) = action(editImageAction)

/**
 * 进入绘制状态
 */
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