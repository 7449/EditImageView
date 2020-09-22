package com.image.edit.react

import android.graphics.*
import com.image.edit.EditImageCache
import com.image.edit.OnEditImageAction
import com.image.edit.OnEditImageAction.Companion.INIT_X_Y
import com.image.edit.virtual.OnEditImageCallback

/**
 * @author y
 * @create 2018/11/20
 */
class RectAction(
        var pointColor: Int = Color.RED,
        var pointWidth: Float = 20f,
) : OnEditImageAction {

    private val startPointF = PointF(INIT_X_Y, INIT_X_Y)
    private val endPointF = PointF(INIT_X_Y, INIT_X_Y)
    private val cacheStartPointF = PointF()
    private val cacheEndPointF = PointF()
    private val pointPaint: Paint = Paint()

    init {
        pointPaint.flags = Paint.ANTI_ALIAS_FLAG
        pointPaint.isAntiAlias = true
        pointPaint.isDither = true
        pointPaint.strokeJoin = Paint.Join.ROUND
        pointPaint.strokeCap = Paint.Cap.ROUND
        pointPaint.pathEffect = PathEffect()
        pointPaint.style = Paint.Style.STROKE
    }

    override fun onDraw(callback: OnEditImageCallback, canvas: Canvas) {
        if (onNoDraw()) {
            return
        }
        pointPaint.color = pointColor
        pointPaint.strokeWidth = pointWidth
        canvas.drawRect(startPointF.x, startPointF.y, endPointF.x, endPointF.y, pointPaint)
    }

    override fun onDrawCache(callback: OnEditImageCallback, canvas: Canvas, editImageCache: EditImageCache) {
        val rectPath = editImageCache.findCache<RectPath>()

        val strokeWidth = when {
            rectPath.scale == callback.viewScale -> {
                rectPath.width
            }
            rectPath.scale > callback.viewScale -> {
                rectPath.width / (rectPath.scale / callback.viewScale)
            }
            else -> {
                rectPath.width * (callback.viewScale / rectPath.scale)
            }
        }

        pointPaint.color = rectPath.color
        pointPaint.strokeWidth = strokeWidth
        callback.onSourceToViewCoord(rectPath.startPointF, cacheStartPointF)
        callback.onSourceToViewCoord(rectPath.endPointF, cacheEndPointF)
        canvas.drawRect(
                cacheStartPointF.x,
                cacheStartPointF.y,
                cacheEndPointF.x,
                cacheEndPointF.y,
                pointPaint)
    }

    override fun onDrawBitmap(callback: OnEditImageCallback, canvas: Canvas, editImageCache: EditImageCache) {
        val rectPath = editImageCache.findCache<RectPath>()
        pointPaint.color = rectPath.color
        pointPaint.strokeWidth = rectPath.width / rectPath.scale
        canvas.drawRect(
                rectPath.startPointF.x,
                rectPath.startPointF.y,
                rectPath.endPointF.x,
                rectPath.endPointF.y,
                pointPaint)
    }

    override fun onDown(callback: OnEditImageCallback, x: Float, y: Float) {
        startPointF.set(x, y)
    }

    override fun onMove(callback: OnEditImageCallback, x: Float, y: Float) {
        endPointF.set(x, y)
        callback.onInvalidate()
    }

    override fun onUp(callback: OnEditImageCallback, x: Float, y: Float) {
        if (onNoDraw()) {
            startPointF.set(INIT_X_Y, INIT_X_Y)
            endPointF.set(INIT_X_Y, INIT_X_Y)
            return
        }
        callback.onAddCache(createCache(callback, RectPath(
                callback.onViewToSourceCoord(startPointF),
                callback.onViewToSourceCoord(endPointF),
                pointPaint.strokeWidth,
                pointPaint.color,
                callback.viewScale)))
        startPointF.set(INIT_X_Y, INIT_X_Y)
        endPointF.set(INIT_X_Y, INIT_X_Y)
    }

    override fun copy(): OnEditImageAction {
        return RectAction(pointColor, pointWidth)
    }

    override fun onNoDraw(): Boolean {
        return startPointF.x == INIT_X_Y
                || startPointF.y == INIT_X_Y
                || endPointF.x == INIT_X_Y
                || endPointF.y == INIT_X_Y
                || startPointF.x == endPointF.x
                || startPointF.y == endPointF.y
    }
}
