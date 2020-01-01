package com.image.edit

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Point
import android.graphics.PointF

/**
 * @author y
 * @create 2019/12/30
 */
interface OnEditImageCallback {

    val viewContext: Context

    val viewScale: Float

    val isMaxCacheCount: Boolean

    val bitmapHeightAndHeight: Point

    val supportBitmap: Bitmap?

    var maxCacheCount: Int

    val isCacheEmpty: Boolean

    var viewEditType: EditType

    var onEditImageListener: OnEditImageListener?

    var onEditImageAction: OnEditImageAction?

    val obj1: Any?
        get() = Unit

    val obj2: Any?
        get() = Unit

    val obj3: Any?
        get() = Unit

    val obj4: Any?
        get() = Unit

    val obj5: Any?
        get() = Unit

    fun onInvalidate()

    fun onCanvasBitmap(canvas: Canvas)

    fun onSourceToViewCoord(pointF: PointF, target: PointF)

    fun onSourceToViewCoord(pointF: PointF): PointF

    fun onViewToSourceCoord(pointF: PointF, target: PointF)

    fun onViewToSourceCoord(pointF: PointF): PointF

    fun onAddCacheAndCheck(cache: EditImageCache)

    fun onAddCacheAnd(cache: EditImageCache)

    fun removeAllCache()

    fun removeLastCache(): EditImageCache?
}