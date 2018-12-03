package com.image.edit.simple

import android.graphics.Canvas
import android.graphics.PointF
import com.image.edit.EditImageView
import com.image.edit.action.DEFAULT_X
import com.image.edit.action.DEFAULT_Y
import com.image.edit.action.OnEditImagePointActionListener
import com.image.edit.action.initPointF
import com.image.edit.cache.EditImageCache
import com.image.edit.cache.EditImagePathLine
import com.image.edit.helper.MatrixAndRectHelper

/**
 * @author y
 * @create 2018/11/20
 */
class SimpleOnEditImageLineActionListener : OnEditImagePointActionListener {

    private var startPointF: PointF = initPointF()
    private var endPointF: PointF = initPointF()

    override fun onDraw(editImageView: EditImageView, canvas: Canvas) {
        if (startPointF.x == DEFAULT_X || startPointF.y == DEFAULT_Y || endPointF.x == DEFAULT_X || endPointF.y == DEFAULT_Y) return
        canvas.drawLine(startPointF.x, startPointF.y, endPointF.x, endPointF.y, editImageView.pointPaint)
    }

    override fun onDown(editImageView: EditImageView, x: Float, y: Float) {
        startPointF.set(x, y)
    }

    override fun onMove(editImageView: EditImageView, x: Float, y: Float) {
        endPointF.set(x, y)
        editImageView.refresh()
    }

    override fun onUp(editImageView: EditImageView, x: Float, y: Float) {
        MatrixAndRectHelper.refreshMatrix(editImageView.newBitmapCanvas, editImageView.supperMatrix!!
        ) { _, _, _, _ -> editImageView.newBitmapCanvas.drawLine(startPointF.x, startPointF.y, endPointF.x, endPointF.y, editImageView.pointPaint) }
        editImageView.viewToSourceCoord(startPointF, startPointF)
        editImageView.viewToSourceCoord(endPointF, endPointF)
        onSaveImageCache(editImageView)
        startPointF = initPointF()
        endPointF = initPointF()
    }

    override fun onSaveImageCache(editImageView: EditImageView) {
        val pointPaint = editImageView.pointPaint
        val pointWidth = editImageView.pointPaint.strokeWidth / editImageView.scale
        editImageView.cacheArrayList.add(EditImageCache.createPointLineCache(editImageView.state, this,
                EditImagePathLine(startPointF, endPointF, pointWidth, pointPaint.color)))
    }

    override fun onLastImageCache(editImageView: EditImageView, editImageCache: EditImageCache) {
        val paint = editImageView.pointPaint
        paint.color = editImageCache.editImagePathLine.color
        paint.strokeWidth = editImageCache.editImagePathLine.width
        editImageView.newBitmapCanvas.drawLine(
                editImageCache.editImagePathLine.startPointF.x,
                editImageCache.editImagePathLine.startPointF.y,
                editImageCache.editImagePathLine.endPointF.x,
                editImageCache.editImagePathLine.endPointF.y,
                paint)
    }
}
