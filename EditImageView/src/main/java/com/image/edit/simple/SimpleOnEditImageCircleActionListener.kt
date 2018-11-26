package com.image.edit.simple

import android.graphics.Canvas
import android.graphics.PointF
import com.image.edit.EditImageView
import com.image.edit.action.DEFAULT_X
import com.image.edit.action.DEFAULT_Y
import com.image.edit.action.OnEditImagePointActionListener
import com.image.edit.action.initPointF
import com.image.edit.cache.EditImageCache
import com.image.edit.cache.EditImagePathCircle
import com.image.edit.helper.MatrixAndRectHelper

/**
 * @author y
 * @create 2018/11/20
 */
class SimpleOnEditImageCircleActionListener : OnEditImagePointActionListener {

    private var startPointF: PointF = initPointF()
    private var endPointF: PointF = initPointF()
    private var currentRadius = 0f

    override fun onDraw(editImageView: EditImageView, canvas: Canvas) {
        if (startPointF.x == DEFAULT_X || startPointF.y == DEFAULT_Y || endPointF.x == DEFAULT_X || endPointF.y == DEFAULT_Y) return
        canvas.drawCircle((startPointF.x + endPointF.x) / 2, (startPointF.y + endPointF.y) / 2, currentRadius, editImageView.pointPaint)
    }

    override fun onDown(editImageView: EditImageView, x: Float, y: Float) {
        startPointF.set(x, y)
    }

    override fun onMove(editImageView: EditImageView, x: Float, y: Float) {
        currentRadius = Math.sqrt(((x - startPointF.x) * (x - startPointF.x) + (y - startPointF.y) * (y - startPointF.y)).toDouble()).toFloat() / 2
        endPointF.set(x, y)
        editImageView.refresh()
    }

    override fun onUp(editImageView: EditImageView, x: Float, y: Float) {
        MatrixAndRectHelper.refreshMatrix(editImageView.newBitmapCanvas, editImageView.supperMatrix!!
        ) { _, _, _, _ -> editImageView.newBitmapCanvas.drawCircle((startPointF.x + endPointF.x) / 2, (startPointF.y + endPointF.y) / 2, currentRadius, editImageView.pointPaint) }
        editImageView.viewToSourceCoord(startPointF, startPointF)
        editImageView.viewToSourceCoord(endPointF, endPointF)
        onSaveImageCache(editImageView)
        currentRadius = 0f
        startPointF = initPointF()
        endPointF = initPointF()
    }

    override fun onSaveImageCache(editImageView: EditImageView) {
        val pointPaint = editImageView.pointPaint
        val radius = currentRadius / editImageView.scale
        val width = editImageView.pointPaint.strokeWidth / editImageView.scale
        editImageView.setCache(EditImageCache.createPointCircleCache(editImageView.state, this, EditImagePathCircle(startPointF, endPointF, radius, width, pointPaint.color)))
    }

    override fun onLastImageCache(editImageView: EditImageView, editImageCache: EditImageCache) {
        val paint = editImageView.pointPaint
        paint.color = editImageCache.editImagePathCircle.color
        paint.strokeWidth = editImageCache.editImagePathCircle.width
        editImageView.newBitmapCanvas.drawCircle(
                (editImageCache.editImagePathCircle.startPointF.x + editImageCache.editImagePathCircle.endPointF.x) / 2,
                (editImageCache.editImagePathCircle.startPointF.y + editImageCache.editImagePathCircle.endPointF.y) / 2,
                editImageCache.editImagePathCircle.radius,
                paint)
    }
}
