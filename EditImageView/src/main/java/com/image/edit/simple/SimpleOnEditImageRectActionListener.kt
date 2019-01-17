package com.image.edit.simple

import android.graphics.Canvas
import android.graphics.PointF
import com.image.edit.EditImageView
import com.image.edit.action.OnEditImagePointActionListener
import com.image.edit.cache.EditImageCache
import com.image.edit.cache.EditImagePathRect
import com.image.edit.x.AllNotNull
import com.image.edit.x.refreshMatrix

/**
 * @author y
 * @create 2018/11/20
 */
class SimpleOnEditImageRectActionListener : OnEditImagePointActionListener {

    private var startPointF: PointF? = null
    private var endPointF: PointF? = null

    override fun onDraw(editImageView: EditImageView, canvas: Canvas) {
        AllNotNull(startPointF, endPointF) { startPointF, endPointF ->
            canvas.drawRect(startPointF.x, startPointF.y, endPointF.x, endPointF.y, editImageView.pointPaint)
        }
    }

    override fun onDown(editImageView: EditImageView, x: Float, y: Float) {
        startPointF = PointF()
        endPointF = PointF()
        startPointF?.set(x, y)
    }

    override fun onMove(editImageView: EditImageView, x: Float, y: Float) {
        endPointF?.set(x, y)
        editImageView.refresh()
    }

    override fun onUp(editImageView: EditImageView, x: Float, y: Float) {
        AllNotNull(startPointF, endPointF, editImageView.supperMatrix) { startPointF, endPointF, supperMatrix ->
            editImageView.newBitmapCanvas.refreshMatrix(supperMatrix
            ) { _, _, _, _ -> editImageView.newBitmapCanvas.drawRect(startPointF.x, startPointF.y, endPointF.x, endPointF.y, editImageView.pointPaint) }
            editImageView.viewToSourceCoord(startPointF, startPointF)
            editImageView.viewToSourceCoord(endPointF, endPointF)
            onSaveImageCache(editImageView)
        }
        startPointF = null
        endPointF = null
    }

    override fun onSaveImageCache(editImageView: EditImageView) {
        AllNotNull(startPointF, endPointF) { startPointF, endPointF ->
            val pointPaint = editImageView.pointPaint
            val width = editImageView.pointPaint.strokeWidth / editImageView.scale
            editImageView.cacheArrayList.add(EditImageCache.createPointRectCache(editImageView.state, this,
                    EditImagePathRect(startPointF, endPointF, width, pointPaint.color)))
        }
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
