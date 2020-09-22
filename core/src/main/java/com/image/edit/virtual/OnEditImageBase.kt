package com.image.edit.virtual

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Point
import android.graphics.PointF
import com.image.edit.EditImageCache
import com.image.edit.EditType
import com.image.edit.OnEditImageAction
import com.image.edit.OnEditImageListener

interface OnEditImageBase {
    /**
     * [Context]
     */
    val viewContext: Context

    /**
     * 目标View缩放Scale
     */
    val viewScale: Float

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
     * 添加缓存
     */
    fun onAddCache(cache: EditImageCache)

    /**
     * 清除所有缓存
     */
    fun removeAllCache()

    /**
     * 清除前一个缓存
     */
    fun removeLastCache(): EditImageCache

    /**
     * 此方法会遍历绘制缓存List,需要一个Canvas去绘制
     */
    fun onCanvasBitmap(canvas: Canvas)

    /**
     * 目标View源坐标转换为触摸坐标
     */
    fun onSourceToViewCoord(source: PointF, target: PointF)

    /**
     * 目标触摸坐标转换为View源坐标
     */
    fun onViewToSourceCoord(source: PointF, target: PointF)

    /**
     * 目标触摸坐标转换为View源坐标
     */
    fun onViewToSourceCoord(source: PointF): PointF
}