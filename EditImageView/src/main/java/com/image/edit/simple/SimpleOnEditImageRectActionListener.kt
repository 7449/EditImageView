package com.image.edit.simple

import android.graphics.Canvas
import android.graphics.PointF
import com.image.edit.EditImageView
import com.image.edit.action.DEFAULT_X
import com.image.edit.action.DEFAULT_Y
import com.image.edit.action.OnEditImagePointActionListener
import com.image.edit.action.initPointF
import com.image.edit.cache.EditImageCache
import com.image.edit.cache.EditImagePathRect
import com.image.edit.helper.MatrixAndRectHelper

/**
 * @author y
 * @create 2018/11/20
 */
class SimpleOnEditImageRectActionListener : OnEditImagePointActionListener {

    private var startPointF: PointF = initPointF()
    private var endPointF: PointF = initPointF()

    override fun onDraw(editImageView: EditImageView, canvas: Canvas) {
        if (startPointF.x == DEFAULT_X || startPointF.y == DEFAULT_Y || endPointF.x == DEFAULT_X || endPointF.y == DEFAULT_Y) return
        canvas.drawRect(startPointF.x, startPointF.y, endPointF.x, endPointF.y, editImageView.pointPaint)
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
        ) { _, _, _, _ -> editImageView.newBitmapCanvas.drawRect(startPointF.x, startPointF.y, endPointF.x, endPointF.y, editImageView.pointPaint) }
        editImageView.viewToSourceCoord(startPointF, startPointF)
        editImageView.viewToSourceCoord(endPointF, endPointF)
        onSaveImageCache(editImageView)
        startPointF = initPointF()
        endPointF = initPointF()
    }

    override fun onSaveImageCache(editImageView: EditImageView) {
        val pointPaint = editImageView.pointPaint
        val width = editImageView.pointPaint.strokeWidth / editImageView.scale
        editImageView.cacheArrayList.add(EditImageCache.createPointRectCache(editImageView.state, this,
                EditImagePathRect(startPointF, endPointF, width, pointPaint.color)))
    }

    override fun onLastImageCache(editImageView: EditImageView, editImageCache: EditImageCache) {
        val paint = editImageView.pointPaint
        paint.strokeWidth = editImageCache.editImagePathRect.width
        paint.color = editImageCache.editImagePathRect.color
        editImageView.newBitmapCanvas.drawRect(
                editImageCache.editImagePathRect.startPointF.x,
                editImageCache.editImagePathRect.startPointF.y,
                editImageCache.editImagePathRect.endPointF.x,
                editImageCache.editImagePathRect.endPointF.y,
                paint)
    }
}
