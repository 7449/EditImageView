package com.image.edit.line

import android.graphics.*
import com.image.edit.*
import com.image.edit.OnEditImageAction.Companion.INIT_X_Y

/**
 * @author y
 * @create 2018/11/20
 */
class LineAction(
        var pointColor: Int = Color.RED,
        var pointWidth: Float = 20f
) : OnEditImageAction {

    private val startPointF = PointF(INIT_X_Y, INIT_X_Y)
    private val endPointF = PointF(INIT_X_Y, INIT_X_Y)
    private val cacheStartPointF = PointF()
    private val cacheEndPointF = PointF()
    private val pointPaint = Paint()

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
        canvas.drawLine(startPointF.x, startPointF.y, endPointF.x, endPointF.y, pointPaint)
    }

    override fun onDrawCache(callback: OnEditImageCallback, canvas: Canvas, editImageCache: EditImageCache) {
        val linePath = editImageCache.findCache<LinePath>()

        val strokeWidth = when {
            linePath.scale == callback.viewScale -> {
                linePath.width
            }
            linePath.scale > callback.viewScale -> {
                linePath.width / (linePath.scale / callback.viewScale)
            }
            else -> {
                linePath.width * (callback.viewScale / linePath.scale)
            }
        }

        pointPaint.color = linePath.color
        pointPaint.strokeWidth = strokeWidth
        callback.onSourceToViewCoord(linePath.startPointF, cacheStartPointF)
        callback.onSourceToViewCoord(linePath.endPointF, cacheEndPointF)
        canvas.drawLine(
                cacheStartPointF.x,
                cacheStartPointF.y,
                cacheEndPointF.x,
                cacheEndPointF.y,
                pointPaint
        )
    }

    override fun onDrawBitmap(callback: OnEditImageCallback, canvas: Canvas, editImageCache: EditImageCache) {
        val linePath = editImageCache.findCache<LinePath>()
        pointPaint.color = linePath.color
        pointPaint.strokeWidth = linePath.width / linePath.scale
        canvas.drawLine(
                linePath.startPointF.x,
                linePath.startPointF.y,
                linePath.endPointF.x,
                linePath.endPointF.y,
                pointPaint
        )
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
        callback.onAddCacheAndCheck(createCache(callback, LinePath(
                callback.onViewToSourceCoord(startPointF),
                callback.onViewToSourceCoord(endPointF),
                pointPaint.strokeWidth,
                pointPaint.color,
                callback.viewScale)))
        startPointF.set(INIT_X_Y, INIT_X_Y)
        endPointF.set(INIT_X_Y, INIT_X_Y)
    }

    override fun copy(): OnEditImageAction {
        return LineAction(pointColor, pointWidth)
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
