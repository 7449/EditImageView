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
    var viewEditType: EditType

    /**
     * 回调
     */
    var onEditImageListener: OnEditImageListener?

    /**
     * 目标View当前Action
     */
    var onEditImageAction: OnEditImageAction?

    /**
     * 附带obj1
     */
    val obj1: Any?
        get() = Unit

    /**
     * 附带obj2
     */
    val obj2: Any?
        get() = Unit

    /**
     * 附带obj3
     */
    val obj3: Any?
        get() = Unit

    /**
     * 附带obj4
     */
    val obj4: Any?
        get() = Unit

    /**
     * 附带obj5
     */
    val obj5: Any?
        get() = Unit

    /**
     * View#invalidate
     */
    fun onInvalidate()

    /**
     * 此方法会遍历绘制缓存List,需要一个Canvas去绘制
     */
    fun onCanvasBitmap(canvas: Canvas)

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

    /**
     * 添加缓存并检查
     */
    fun onAddCacheAndCheck(cache: EditImageCache)

    /**
     * 清除所有缓存
     */
    fun removeAllCache()

    /**
     * 清除前一个缓存
     */
    fun removeLastCache(): EditImageCache?
}