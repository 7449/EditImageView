package com.image.edit.point

import android.graphics.*
import com.image.edit.*
import kotlin.math.abs

/**
 * @author y
 * @create 2018/11/20
 */
class PointAction(
        var pointColor: Int = Color.RED,
        var pointWidth: Float = 20f
) : OnEditImageAction {

    private val paintPath = Path()
    private val pointPaint = Paint()
    private val listPointF = ArrayList<PointF>()
    private val cachePointF = PointF()

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
        paintPath.reset()
        paintPath.moveTo(listPointF[0].x, listPointF[0].y)
        for (i in 1 until listPointF.size) {
            val pointF = listPointF[i]
            paintPath.quadTo(pointF.x, pointF.y, pointF.x, pointF.y)
        }
        canvas.drawPath(paintPath, pointPaint)
    }

    override fun onDrawCache(callback: OnEditImageCallback, canvas: Canvas, editImageCache: EditImageCache) {
        val pointPath = editImageCache.findCache<PointPath>()

        val strokeWidth = when {
            pointPath.scale == callback.viewScale -> {
                pointPath.width
            }
            pointPath.scale > callback.viewScale -> {
                pointPath.width / (pointPath.scale / callback.viewScale)
            }
            else -> {
                pointPath.width * (callback.viewScale / pointPath.scale)
            }
        }

        pointPaint.color = pointPath.color
        pointPaint.strokeWidth = strokeWidth
        paintPath.reset()
        callback.onSourceToViewCoord(pointPath.pointFList[0], cachePointF)
        paintPath.moveTo(cachePointF.x, cachePointF.y)
        for (i in 1 until pointPath.pointFList.size) {
            callback.onSourceToViewCoord(pointPath.pointFList[i], cachePointF)
            paintPath.quadTo(cachePointF.x, cachePointF.y, cachePointF.x, cachePointF.y)
        }
        canvas.drawPath(paintPath, pointPaint)
    }

    override fun onDrawBitmap(callback: OnEditImageCallback, canvas: Canvas, editImageCache: EditImageCache) {
        val pointPath = editImageCache.findCache<PointPath>()
        pointPaint.color = pointPath.color
        pointPaint.strokeWidth = pointPath.width / pointPath.scale
        paintPath.reset()
        paintPath.moveTo(pointPath.pointFList[0].x, pointPath.pointFList[0].y)
        for (i in 1 until pointPath.pointFList.size) {
            val pointF = pointPath.pointFList[i]
            paintPath.quadTo(pointF.x, pointF.y, pointF.x, pointF.y)
        }
        canvas.drawPath(paintPath, pointPaint)
    }

    override fun onDown(callback: OnEditImageCallback, x: Float, y: Float) {
        listPointF.add(PointF(x, y))
        paintPath.moveTo(x, y)
    }

    override fun onMove(callback: OnEditImageCallback, x: Float, y: Float) {
        val pointF = listPointF[listPointF.size - 1]
        if (abs(x - pointF.x) >= 3 || abs(y - pointF.y) >= 3) {
            listPointF.add(PointF(x, y))
            callback.onInvalidate()
        }
    }

    override fun onUp(callback: OnEditImageCallback, x: Float, y: Float) {
        if (onNoDraw()) {
            listPointF.clear()
            return
        }
        val newList = ArrayList<PointF>()
        listPointF.forEach { newList.add(callback.onViewToSourceCoord(it)) }
        callback.onAddCacheAndCheck(createCache(callback, PointPath(
                newList,
                pointPaint.strokeWidth,
                pointPaint.color,
                callback.viewScale)))
        listPointF.clear()
    }

    override fun copy(): OnEditImageAction {
        return PointAction(pointColor, pointWidth)
    }

    override fun onNoDraw(): Boolean {
        return listPointF.isEmpty() || listPointF.size <= 3
    }
}
