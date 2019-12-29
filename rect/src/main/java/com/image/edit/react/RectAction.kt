package com.image.edit.react

import android.graphics.*
import com.image.edit.*
import com.image.edit.OnEditImageAction.Companion.INIT_X_Y

/**
 * @author y
 * @create 2018/11/20
 */
class RectAction(
        var pointColor: Int = Color.RED,
        var pointWidth: Float = 20f
) : OnEditImageAction<RectPath> {

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

    override fun onDraw(editImageView: EditImageView, canvas: Canvas) {
        if (onNoDraw()) {
            return
        }
        pointPaint.color = pointColor
        pointPaint.strokeWidth = pointWidth
        canvas.drawRect(startPointF.x, startPointF.y, endPointF.x, endPointF.y, pointPaint)
    }

    override fun onDrawCache(editImageView: EditImageView, canvas: Canvas, editImageCache: EditImageCache<RectPath>) {
        val rectPath = editImageCache.imageCache

        val strokeWidth = when {
            rectPath.scale == editImageView.scale -> {
                rectPath.width
            }
            rectPath.scale > editImageView.scale -> {
                rectPath.width / (rectPath.scale / editImageView.scale)
            }
            else -> {
                rectPath.width * (editImageView.scale / rectPath.scale)
            }
        }

        pointPaint.color = rectPath.color
        pointPaint.strokeWidth = strokeWidth
        editImageView.sourceToViewCoord(rectPath.startPointF, cacheStartPointF)
        editImageView.sourceToViewCoord(rectPath.endPointF, cacheEndPointF)
        canvas.drawRect(
                cacheStartPointF.x,
                cacheStartPointF.y,
                cacheEndPointF.x,
                cacheEndPointF.y,
                pointPaint)
    }

    override fun onDrawBitmap(editImageView: EditImageView, canvas: Canvas, editImageCache: EditImageCache<RectPath>) {
        val rectPath = editImageCache.imageCache
        pointPaint.color = rectPath.color
        pointPaint.strokeWidth = rectPath.width / rectPath.scale
        canvas.drawRect(
                rectPath.startPointF.x,
                rectPath.startPointF.y,
                rectPath.endPointF.x,
                rectPath.endPointF.y,
                pointPaint)
    }

    override fun onDown(editImageView: EditImageView, x: Float, y: Float) {
        startPointF.set(x, y)
    }

    override fun onMove(editImageView: EditImageView, x: Float, y: Float) {
        endPointF.set(x, y)
        editImageView.invalidate()
    }

    override fun onUp(editImageView: EditImageView, x: Float, y: Float) {
        if (onNoDraw()) {
            startPointF.set(INIT_X_Y, INIT_X_Y)
            endPointF.set(INIT_X_Y, INIT_X_Y)
            return
        }
        if (editImageView.isMaxCount) {
            editImageView.onEditImageListener?.onLastCacheMax()
            return
        }
        editImageView.cacheArrayList.add(createCache(editImageView.state, RectPath(
                editImageView.viewToSourceCoords(startPointF),
                editImageView.viewToSourceCoords(endPointF),
                pointPaint.strokeWidth,
                pointPaint.color,
                editImageView.scale)))
        startPointF.set(INIT_X_Y, INIT_X_Y)
        endPointF.set(INIT_X_Y, INIT_X_Y)
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
