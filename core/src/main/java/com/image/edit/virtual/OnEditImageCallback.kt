package com.image.edit.virtual

import android.graphics.Canvas
import android.graphics.PointF
import com.image.edit.OnEditImageAction

/**
 * @author y
 * @create 2019/12/30
 */
interface OnEditImageCallback : OnEditImageBitmap {

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