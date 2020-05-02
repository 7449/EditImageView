package com.image.edit

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Point
import android.graphics.PointF
import android.util.Log

/**
 * @author y
 * @create 2019/12/30
 */
interface OnEditImageCallback {

    /**
     * [Context]
     */
    val viewContext: Context

    /**
     * 目标View缩放Scale
     */
    val viewScale: Float

    /**
     * [Bitmap]模式下返回目标View的图像Bitmap
     */
    val viewBitmap: Bitmap?
        get() = null

    /**
     * 橡皮擦[Canvas]
     * View#draw下的canvas,直接使用橡皮擦是黑色
     * 因此开启橡皮擦需要 new Canvas()
     */
    val supportCanvas: Canvas?
        get() = null

    /**
     * 是否开启自动识别是切片加载还是bitmap加载
     * default false
     * 如果为true,在 [viewBitmap] != null && [supportCanvas] != null 会获取 [supportCanvas]绘制痕迹
     * 切片加载不支持橡皮擦(痕迹为黑色,实际有效果)
     * bitmap模式加载支持橡皮擦
     * 目前只有「Circle Library」支持了自动识别加载方式
     * 如果不需要橡皮擦功能,照常加载即可
     */
    val intelligent: Boolean
        get() = false

    /**
     * 如果是[Bitmap]模式下的View,返回宽高,x == width,y == height
     */
    val bitmapHeightAndHeight: Point

    /**
     * 是否缓存最大数
     */
    val isMaxCacheCount: Boolean

    /**
     * 缓存是否为空
     */
    val isCacheEmpty: Boolean

    /**
     * 编辑模式
     * [EditType.ACTION]
     * [EditType.NONE]
     */
    val viewEditType: EditType

    /**
     * 回调
     */
    val onEditImageListener: OnEditImageListener?

    /**
     * 目标View当前Action
     */
    val onEditImageAction: OnEditImageAction?

    /**
     * 进入绘制状态
     */
    fun action(editImageAction: OnEditImageAction) = also {
        if (isMaxCacheCount) {
            onEditImageListener?.onLastCacheMax()
            return@also
        }
        updateAction(editImageAction)
        editTypeAction()
    }.onEditImageAction

    /**
     * 更新目标ViewAction
     */
    fun updateAction(action: OnEditImageAction)

    /**
     * 退出编辑模式
     */
    fun noneAction(): OnEditImageCallback

    /**
     * 进入编辑模式
     */
    fun editTypeAction(): OnEditImageCallback

    /**
     * View#invalidate
     */
    fun onInvalidate()

    /**
     * 此方法会遍历绘制缓存List,需要一个Canvas去绘制
     */
    fun onCanvasBitmap(canvas: Canvas)

    /**
     * 添加缓存并检查
     */
    fun onAddCacheAndCheck(cache: EditImageCache)

    /**
     * 清除所有绘制痕迹
     */
    fun clearImage() {
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
    fun lastImage() {
        if (isCacheEmpty) {
            onEditImageListener?.onLastImageEmpty()
            return
        }
        removeLastCache()
        noneAction()
    }

    /**
     * 清除所有缓存
     */
    fun removeAllCache()

    /**
     * 清除前一个缓存
     */
    fun removeLastCache(): EditImageCache?

    /**
     * 目标View源坐标转换为触摸坐标
     */
    fun onSourceToViewCoord(x: Float, y: Float, target: PointF) {}

    /**
     * 目标View源坐标转换为触摸坐标
     */
    fun onSourceToViewCoord(source: PointF, target: PointF)

    /**
     * 目标View源坐标转换为触摸坐标
     */
    fun onSourceToViewCoord(x: Float, y: Float): PointF = PointF(x, y)

    /**
     * 目标View源坐标转换为触摸坐标
     */
    fun onSourceToViewCoord(source: PointF): PointF = PointF(source.x, source.y)

    /**
     * 目标触摸坐标转换为View源坐标
     */
    fun onViewToSourceCoord(x: Float, y: Float, target: PointF) {}

    /**
     * 目标触摸坐标转换为View源坐标
     */
    fun onViewToSourceCoord(source: PointF, target: PointF)

    /**
     * 目标触摸坐标转换为View源坐标
     */
    fun onViewToSourceCoord(x: Float, y: Float): PointF = PointF(x, y)

    /**
     * 目标触摸坐标转换为View源坐标
     */
    fun onViewToSourceCoord(source: PointF): PointF

    fun <T> findObj(obj: Any?): T? {
        @Suppress("UNCHECKED_CAST")
        return obj as T?
    }

    /**
     * 附带obj1
     */
    val obj1: Any?
        get() = Unit

    fun <T> findObj1(): T? = findObj<T>(obj1)

    /**
     * 附带obj2
     */
    val obj2: Any?
        get() = Unit

    fun <T> findObj2(): T? = findObj<T>(obj2)

    /**
     * 附带obj3
     */
    val obj3: Any?
        get() = Unit

    fun <T> findObj3(): T? = findObj<T>(obj3)

    /**
     * 附带obj4
     */
    val obj4: Any?
        get() = Unit

    fun <T> findObj4(): T? = findObj<T>(obj4)

    /**
     * 附带obj5
     */
    val obj5: Any?
        get() = Unit

    fun <T> findObj5(): T? = findObj<T>(obj5)

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

    /**
     * 返回最终使用的Canvas
     * 如果不开启自动识别切片或者[supportCanvas][viewBitmap]中有任何一个为null
     * 返回目标View的[Canvas]
     * else
     * 则返回自定义[Canvas]用于橡皮擦功能
     */
    fun finalCanvas(viewCanvas: Canvas): Canvas {
        if (!intelligent || supportCanvas == null || viewBitmap == null) {
            return viewCanvas
        }
        return supportCanvas ?: throw KotlinNullPointerException("supportCanvas == null")
    }

    /**
     * 检查最终使用的Canvas
     * true:不可使用橡皮擦，因为返回的是目标view的[Canvas]
     * 应传入目标View的[Canvas]而不是[OnEditImageCallback.supportCanvas]
     * true 不可使用橡皮擦
     * false 可以使用橡皮擦
     */
    fun checkCanvas(viewCanvas: Canvas): Boolean {
        return viewCanvas == finalCanvas(viewCanvas)
    }

    /**
     * 返回最终作用在Canvas上的坐标
     * 如果是目标View的Canvas则转换坐标,反之直接返回
     */
    fun finalSourceToViewCoord(canvas: Canvas, source: PointF, target: PointF) {
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
    fun finalViewToSourceCoord(canvas: Canvas, source: PointF, target: PointF) {
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
    fun finalParameter(canvas: Canvas, cacheScale: Float, target: Float): Float {
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
    fun finalParameterNo(canvas: Canvas, cacheScale: Float, target: Float): Float {
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

}