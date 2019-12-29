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
) : OnEditImageAction<PointPath> {

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

    override fun onDraw(editImageView: EditImageView, canvas: Canvas) {
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

    override fun onDrawCache(editImageView: EditImageView, canvas: Canvas, editImageCache: EditImageCache<PointPath>) {
        val pointPath = editImageCache.imageCache

        val strokeWidth = when {
            pointPath.scale == editImageView.scale -> {
                pointPath.width
            }
            pointPath.scale > editImageView.scale -> {
                pointPath.width / (pointPath.scale / editImageView.scale)
            }
            else -> {
                pointPath.width * (editImageView.scale / pointPath.scale)
            }
        }

        pointPaint.color = pointPath.color
        pointPaint.strokeWidth = strokeWidth
        paintPath.reset()
        editImageView.sourceToViewCoord(pointPath.pointFList[0], cachePointF)
        paintPath.moveTo(cachePointF.x, cachePointF.y)
        for (i in 1 until pointPath.pointFList.size) {
            editImageView.sourceToViewCoord(pointPath.pointFList[i], cachePointF)
            paintPath.quadTo(cachePointF.x, cachePointF.y, cachePointF.x, cachePointF.y)
        }
        canvas.drawPath(paintPath, pointPaint)
    }

    override fun onDrawBitmap(editImageView: EditImageView, canvas: Canvas, editImageCache: EditImageCache<PointPath>) {
        val pointPath = editImageCache.imageCache
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

    override fun onDown(editImageView: EditImageView, x: Float, y: Float) {
        listPointF.add(PointF(x, y))
        paintPath.moveTo(x, y)
    }

    override fun onMove(editImageView: EditImageView, x: Float, y: Float) {
        val pointF = listPointF[listPointF.size - 1]
        if (abs(x - pointF.x) >= 3 || abs(y - pointF.y) >= 3) {
            listPointF.add(PointF(x, y))
            editImageView.invalidate()
        }
    }

    override fun onUp(editImageView: EditImageView, x: Float, y: Float) {
        if (onNoDraw()) {
            listPointF.clear()
            return
        }
        if (editImageView.isMaxCount) {
            editImageView.onEditImageListener?.onLastCacheMax()
            return
        }
        val newList = ArrayList<PointF>()
        listPointF.forEach { newList.add(editImageView.viewToSourceCoords(it)) }
        editImageView.cacheArrayList.add(createCache(editImageView.state, PointPath(
                newList,
                pointPaint.strokeWidth,
                pointPaint.color,
                editImageView.scale)))
        listPointF.clear()
    }

    override fun onNoDraw(): Boolean {
        return listPointF.isEmpty() || listPointF.size <= 3
    }
}
