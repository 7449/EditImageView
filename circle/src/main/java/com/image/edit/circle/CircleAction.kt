package com.image.edit.circle

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PathEffect
import android.graphics.PointF
import com.davemorrissey.labs.subscaleview.api.getState
import com.davemorrissey.labs.subscaleview.api.viewToSourceCoord
import com.image.edit.*
import com.image.edit.cache.EditImageCache
import kotlin.math.sqrt

/**
 * @author y
 * @create 2018/11/20
 */
class CircleAction : OnEditImageAction<EditImagePathCircle> {

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
            pointPaint.color = editImageView.editImageConfig.pointColor
            pointPaint.strokeWidth = editImageView.editImageConfig.pointWidth
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
            editImageView.cacheArrayList.add(createCache(editImageView.getState(), EditImagePathCircle(startPointF, endPointF, currentRadius, pointPaint.strokeWidth, pointPaint.color)))
        }
    }

    override fun onLastImageCache(editImageView: EditImageView, editImageCache: EditImageCache<EditImagePathCircle>) {
        val editImagePath = editImageCache.imageCache
        pointPaint.color = editImagePath.color
        pointPaint.strokeWidth = editImagePath.width
        editImageView.newBitmapCanvas.drawCircle(
                (editImagePath.startPointF.x + editImagePath.endPointF.x) / 2,
                (editImagePath.startPointF.y + editImagePath.endPointF.y) / 2,
                editImagePath.radius,
                pointPaint)
    }
}
