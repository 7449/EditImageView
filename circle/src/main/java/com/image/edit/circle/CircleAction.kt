package com.image.edit.circle

import android.graphics.*
import com.image.edit.EditImageCache
import com.image.edit.OnEditImageAction
import com.image.edit.OnEditImageAction.Companion.INIT_X_Y
import com.image.edit.virtual.OnEditImageCallback
import kotlin.math.sqrt

/**
 * @author y
 * @create 2018/11/20
 */
class CircleAction(
        var pointColor: Int = Color.RED,
        var pointWidth: Float = 20f,
) : OnEditImageAction {

    private val startPointF = PointF(INIT_X_Y, INIT_X_Y)
    private val endPointF = PointF(INIT_X_Y, INIT_X_Y)
    private val cacheStartPointF = PointF()
    private val cacheEndPointF = PointF()
    private val pointPaint = Paint()
    private var currentRadius = INIT_X_Y

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
        canvas.drawCircle((startPointF.x + endPointF.x) / 2, (startPointF.y + endPointF.y) / 2, currentRadius, pointPaint)
    }

    override fun onDrawCache(callback: OnEditImageCallback, canvas: Canvas, editImageCache: EditImageCache) {
        val circlePath = editImageCache.findCache<CirclePath>()

        pointPaint.color = circlePath.color
        pointPaint.strokeWidth = callback.finalParameter(canvas, circlePath.scale, circlePath.width)
        callback.finalSourceToViewCoord(canvas, circlePath.startPointF, cacheStartPointF)
        callback.finalSourceToViewCoord(canvas, circlePath.endPointF, cacheEndPointF)
        callback.finalCanvas(canvas).drawCircle(
                (cacheStartPointF.x + cacheEndPointF.x) / 2,
                (cacheStartPointF.y + cacheEndPointF.y) / 2,
                callback.finalParameter(canvas, circlePath.scale, circlePath.radius),
                pointPaint)
    }

    override fun onDrawBitmap(callback: OnEditImageCallback, canvas: Canvas, editImageCache: EditImageCache) {
        val circlePath = editImageCache.findCache<CirclePath>()
        pointPaint.color = circlePath.color
        pointPaint.strokeWidth = circlePath.width / circlePath.scale
        canvas.drawCircle(
                (circlePath.startPointF.x + circlePath.endPointF.x) / 2,
                (circlePath.startPointF.y + circlePath.endPointF.y) / 2,
                circlePath.radius / circlePath.scale,
                pointPaint)
    }

    override fun onDown(callback: OnEditImageCallback, x: Float, y: Float) {
        startPointF.set(x, y)
    }

    override fun onMove(callback: OnEditImageCallback, x: Float, y: Float) {
        currentRadius = sqrt(((x - startPointF.x) * (x - startPointF.x) + (y - startPointF.y) * (y - startPointF.y))) / 2
        endPointF.set(x, y)
        callback.onInvalidate()
    }

    override fun onUp(callback: OnEditImageCallback, x: Float, y: Float) {
        if (onNoDraw()) {
            currentRadius = INIT_X_Y
            startPointF.set(INIT_X_Y, INIT_X_Y)
            endPointF.set(INIT_X_Y, INIT_X_Y)
            return
        }
        val circlePath = CirclePath(
                callback.onViewToSourceCoord(startPointF),
                callback.onViewToSourceCoord(endPointF),
                currentRadius,
                pointPaint.strokeWidth,
                pointPaint.color,
                callback.viewScale)
        callback.onAddCache(createCache(callback, circlePath))
        currentRadius = INIT_X_Y
        startPointF.set(INIT_X_Y, INIT_X_Y)
        endPointF.set(INIT_X_Y, INIT_X_Y)
    }

    override fun copy(): OnEditImageAction {
        return CircleAction(pointColor, pointWidth)
    }

    override fun onNoDraw(): Boolean {
        return startPointF.x == INIT_X_Y
                || startPointF.y == INIT_X_Y
                || endPointF.x == INIT_X_Y
                || endPointF.y == INIT_X_Y
                || currentRadius <= 0
    }
}
