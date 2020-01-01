package com.image.edit.eraser

import android.graphics.*
import com.image.edit.*
import kotlin.math.abs

/**
 * @author y
 * @create 2018/11/20
 */
class EraserAction(
        var pointWidth: Float = 20f
) : OnEditImageAction {

    private val paintPath = Path()
    private val eraserPaint = Paint()
    private val listPointF = ArrayList<PointF>()
    private val cachePointF = PointF()

    init {
        eraserPaint.alpha = 0
        eraserPaint.color = Color.TRANSPARENT
        eraserPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        eraserPaint.isAntiAlias = true
        eraserPaint.isDither = true
        eraserPaint.style = Paint.Style.STROKE
        eraserPaint.strokeJoin = Paint.Join.ROUND
        eraserPaint.strokeCap = Paint.Cap.ROUND
        eraserPaint.pathEffect = PathEffect()
    }

    override fun onDraw(callback: OnEditImageCallback, canvas: Canvas) {
        if (onNoDraw()) {
            return
        }
        eraserPaint.strokeWidth = pointWidth
        paintPath.reset()
        paintPath.moveTo(listPointF[0].x, listPointF[0].y)
        for (i in 1 until listPointF.size) {
            val pointF = listPointF[i]
            paintPath.quadTo(pointF.x, pointF.y, pointF.x, pointF.y)
        }
        canvas.drawPath(paintPath, eraserPaint)
    }

    override fun onDrawCache(callback: OnEditImageCallback, canvas: Canvas, editImageCache: EditImageCache) {
        val eraserPath = editImageCache.findCache<EraserPath>()

        if (eraserPath.pointFList.size <= 1) {
            return
        }

        val strokeWidth = when {
            eraserPath.scale == callback.viewScale -> {
                eraserPath.width
            }
            eraserPath.scale > callback.viewScale -> {
                eraserPath.width / (eraserPath.scale / callback.viewScale)
            }
            else -> {
                eraserPath.width * (callback.viewScale / eraserPath.scale)
            }
        }

        eraserPaint.strokeWidth = strokeWidth
        paintPath.reset()
        callback.onSourceToViewCoord(eraserPath.pointFList[0], cachePointF)
        paintPath.moveTo(cachePointF.x, cachePointF.y)
        for (i in 1 until eraserPath.pointFList.size) {
            callback.onSourceToViewCoord(eraserPath.pointFList[i], cachePointF)
            paintPath.quadTo(cachePointF.x, cachePointF.y, cachePointF.x, cachePointF.y)
        }
        canvas.drawPath(paintPath, eraserPaint)
    }

    override fun onDrawBitmap(callback: OnEditImageCallback, canvas: Canvas, editImageCache: EditImageCache) {
        val eraserPath = editImageCache.findCache<EraserPath>()
        eraserPaint.strokeWidth = eraserPath.width / eraserPath.scale
        paintPath.reset()
        paintPath.moveTo(eraserPath.pointFList[0].x, eraserPath.pointFList[0].y)
        for (i in 1 until eraserPath.pointFList.size) {
            val pointF = eraserPath.pointFList[i]
            paintPath.quadTo(pointF.x, pointF.y, pointF.x, pointF.y)
        }
        canvas.drawPath(paintPath, eraserPaint)
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
        callback.onAddCacheAndCheck(createCache(callback, EraserPath(
                newList,
                eraserPaint.strokeWidth,
                callback.viewScale)))
        listPointF.clear()
    }

    override fun copy(): OnEditImageAction {
        return EraserAction(pointWidth)
    }

    override fun onNoDraw(): Boolean {
        return listPointF.size <= 3
    }
}
