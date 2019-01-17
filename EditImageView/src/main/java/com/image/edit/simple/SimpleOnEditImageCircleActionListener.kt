package com.image.edit.simple

import android.graphics.Canvas
import android.graphics.PointF
import com.image.edit.EditImageView
import com.image.edit.action.OnEditImagePointActionListener
import com.image.edit.cache.EditImageCache
import com.image.edit.cache.EditImagePathCircle
import com.image.edit.x.AllNotNull
import com.image.edit.x.refreshMatrix

/**
 * @author y
 * @create 2018/11/20
 */
class SimpleOnEditImageCircleActionListener : OnEditImagePointActionListener {

    private var startPointF: PointF? = null
    private var endPointF: PointF? = null
    private var currentRadius = 0f

    override fun onDraw(editImageView: EditImageView, canvas: Canvas) {
        AllNotNull(startPointF, endPointF) { startPointF, endPointF -> canvas.drawCircle((startPointF.x + endPointF.x) / 2, (startPointF.y + endPointF.y) / 2, currentRadius, editImageView.pointPaint) }
    }

    override fun onDown(editImageView: EditImageView, x: Float, y: Float) {
        startPointF = PointF()
        endPointF = PointF()
        startPointF?.set(x, y)
    }

    override fun onMove(editImageView: EditImageView, x: Float, y: Float) {
        AllNotNull(startPointF, endPointF) { startPointF, endPointF ->
            currentRadius = Math.sqrt(((x - startPointF.x) * (x - startPointF.x) + (y - startPointF.y) * (y - startPointF.y)).toDouble()).toFloat() / 2
            endPointF.set(x, y)
            editImageView.refresh()
        }
    }

    override fun onUp(editImageView: EditImageView, x: Float, y: Float) {
        AllNotNull(startPointF, endPointF, editImageView.supperMatrix) { startPointF, endPointF, supperMatrix ->
            editImageView.newBitmapCanvas.refreshMatrix(supperMatrix
            ) { _, _, _, _ -> editImageView.newBitmapCanvas.drawCircle((startPointF.x + endPointF.x) / 2, (startPointF.y + endPointF.y) / 2, currentRadius, editImageView.pointPaint) }
            editImageView.viewToSourceCoord(startPointF, startPointF)
            editImageView.viewToSourceCoord(endPointF, endPointF)
            onSaveImageCache(editImageView)
        }
        currentRadius = 0f
        startPointF = null
        endPointF = null
    }

    override fun onSaveImageCache(editImageView: EditImageView) {
        AllNotNull(startPointF, endPointF) { startPointF, endPointF ->
            val pointPaint = editImageView.pointPaint
            val radius = currentRadius / editImageView.scale
            val width = editImageView.pointPaint.strokeWidth / editImageView.scale
            editImageView.cacheArrayList.add(EditImageCache.createPointCircleCache(editImageView.state, this, EditImagePathCircle(startPointF, endPointF, radius, width, pointPaint.color)))
        }
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
