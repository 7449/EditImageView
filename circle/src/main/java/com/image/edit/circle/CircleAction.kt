package com.image.edit.circle

import android.graphics.*
import com.image.edit.*
import com.image.edit.OnEditImageAction.Companion.INIT_X_Y
import kotlin.math.sqrt

/**
 * @author y
 * @create 2018/11/20
 */
class CircleAction(
        var pointColor: Int = Color.RED,
        var pointWidth: Float = 20f
) : OnEditImageAction<CirclePath> {

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

    override fun onDraw(editImageView: EditImageView, canvas: Canvas) {
        if (onNoDraw()) {
            return
        }
        pointPaint.color = pointColor
        pointPaint.strokeWidth = pointWidth
        canvas.drawCircle((startPointF.x + endPointF.x) / 2, (startPointF.y + endPointF.y) / 2, currentRadius, pointPaint)
    }

    override fun onDrawCache(editImageView: EditImageView, canvas: Canvas, editImageCache: EditImageCache<CirclePath>) {
        val circlePath = editImageCache.imageCache

        val radius = when {
            circlePath.scale == editImageView.scale -> {
                circlePath.radius
            }
            circlePath.scale > editImageView.scale -> {
                circlePath.radius / (circlePath.scale / editImageView.scale)
            }
            else -> {
                circlePath.radius * (editImageView.scale / circlePath.scale)
            }
        }

        val strokeWidth = when {
            circlePath.scale == editImageView.scale -> {
                circlePath.width
            }
            circlePath.scale > editImageView.scale -> {
                circlePath.width / (circlePath.scale / editImageView.scale)
            }
            else -> {
                circlePath.width * (editImageView.scale / circlePath.scale)
            }
        }

        pointPaint.color = circlePath.color
        pointPaint.strokeWidth = strokeWidth
        editImageView.sourceToViewCoord(circlePath.startPointF, cacheStartPointF)
        editImageView.sourceToViewCoord(circlePath.endPointF, cacheEndPointF)
        canvas.drawCircle(
                (cacheStartPointF.x + cacheEndPointF.x) / 2,
                (cacheStartPointF.y + cacheEndPointF.y) / 2,
                radius,
                pointPaint)
    }

    override fun onDrawBitmap(editImageView: EditImageView, canvas: Canvas, editImageCache: EditImageCache<CirclePath>) {
        val circlePath = editImageCache.imageCache
        pointPaint.color = circlePath.color
        pointPaint.strokeWidth = circlePath.width / circlePath.scale
        canvas.drawCircle(
                (circlePath.startPointF.x + circlePath.endPointF.x) / 2,
                (circlePath.startPointF.y + circlePath.endPointF.y) / 2,
                circlePath.radius / circlePath.scale,
                pointPaint)
    }

    override fun onDown(editImageView: EditImageView, x: Float, y: Float) {
        startPointF.set(x, y)
    }

    override fun onMove(editImageView: EditImageView, x: Float, y: Float) {
        currentRadius = sqrt(((x - startPointF.x) * (x - startPointF.x) + (y - startPointF.y) * (y - startPointF.y))) / 2
        endPointF.set(x, y)
        editImageView.invalidate()
    }

    override fun onUp(editImageView: EditImageView, x: Float, y: Float) {
        if (onNoDraw()) {
            currentRadius = INIT_X_Y
            startPointF.set(INIT_X_Y, INIT_X_Y)
            endPointF.set(INIT_X_Y, INIT_X_Y)
            return
        }
        if (editImageView.isMaxCount) {
            editImageView.onEditImageListener?.onLastCacheMax()
            return
        }
        editImageView.cacheArrayList.add(createCache(editImageView.state, CirclePath(
                editImageView.viewToSourceCoords(startPointF),
                editImageView.viewToSourceCoords(endPointF),
                currentRadius,
                pointPaint.strokeWidth,
                pointPaint.color,
                editImageView.scale)))
        currentRadius = INIT_X_Y
        startPointF.set(INIT_X_Y, INIT_X_Y)
        endPointF.set(INIT_X_Y, INIT_X_Y)
    }

    override fun onNoDraw(): Boolean {
        return startPointF.x == INIT_X_Y
                || startPointF.y == INIT_X_Y
                || endPointF.x == INIT_X_Y
                || endPointF.y == INIT_X_Y
                || currentRadius <= 0
    }
}
