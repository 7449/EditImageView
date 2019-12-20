package com.image.edit.circle

import android.graphics.*
import com.image.edit.*
import kotlin.math.sqrt

/**
 * @author y
 * @create 2018/11/20
 */
class CircleAction(
        var pointColor: Int = Color.RED,
        var pointWidth: Float = 20f
) : OnEditImageAction<CirclePath> {

    private var startPointF: PointF? = null
    private var endPointF: PointF? = null
    private var currentRadius = 0f
    private var pointPaint: Paint = Paint()

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
        allNotNull(startPointF, endPointF) { startPointF, endPointF ->
            pointPaint.color = pointColor
            pointPaint.strokeWidth = pointWidth
            canvas.drawCircle((startPointF.x + endPointF.x) / 2, (startPointF.y + endPointF.y) / 2, currentRadius, pointPaint)
        }
    }

    override fun onDown(editImageView: EditImageView, x: Float, y: Float) {
        startPointF = PointF()
        endPointF = PointF()
        startPointF?.set(x, y)
    }

    override fun onMove(editImageView: EditImageView, x: Float, y: Float) {
        allNotNull(startPointF, endPointF) { startPointF, endPointF ->
            currentRadius = sqrt(((x - startPointF.x) * (x - startPointF.x) + (y - startPointF.y) * (y - startPointF.y)).toDouble()).toFloat() / 2
            endPointF.set(x, y)
            editImageView.invalidate()
        }
    }

    override fun onUp(editImageView: EditImageView, x: Float, y: Float) {
        allNotNull(startPointF, endPointF) { startPointF, endPointF ->
            if (checkCoordinate(startPointF, endPointF, x, y)) {
                return
            }
            editImageView.viewToSourceCoord(startPointF, startPointF)
            editImageView.viewToSourceCoord(endPointF, endPointF)
            currentRadius /= editImageView.scale
            pointPaint.strokeWidth /= editImageView.scale
            editImageView.newBitmapCanvas.drawCircle((startPointF.x + endPointF.x) / 2, (startPointF.y + endPointF.y) / 2, currentRadius, pointPaint)
            onSaveImageCache(editImageView)
        }
        currentRadius = 0f
        startPointF = null
        endPointF = null
    }

    override fun onSaveImageCache(editImageView: EditImageView) {
        allNotNull(startPointF, endPointF) { startPointF, endPointF ->
            editImageView.cacheArrayList.add(createCache(editImageView.state, CirclePath(startPointF, endPointF, currentRadius, pointPaint.strokeWidth, pointPaint.color)))
        }
    }

    override fun onLastImageCache(editImageView: EditImageView, editImageCache: EditImageCache<CirclePath>) {
        val circlePath = editImageCache.imageCache
        pointPaint.color = circlePath.color
        pointPaint.strokeWidth = circlePath.width
        editImageView.newBitmapCanvas.drawCircle(
                (circlePath.startPointF.x + circlePath.endPointF.x) / 2,
                (circlePath.startPointF.y + circlePath.endPointF.y) / 2,
                circlePath.radius,
                pointPaint)
    }
}
