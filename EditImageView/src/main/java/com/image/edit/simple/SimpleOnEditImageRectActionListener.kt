package com.image.edit.simple

import android.graphics.Canvas
import android.graphics.PointF
import com.image.edit.EditImageView
import com.image.edit.action.OnEditImageActionListener
import com.image.edit.cache.EditImageCache
import com.image.edit.cache.EditImageCacheCallback
import com.image.edit.helper.AllNotNull
import com.image.edit.helper.refreshMatrix
import com.image.edit.refresh
import com.image.edit.transformerCache

/**
 * @author y
 * @create 2018/11/20
 */

data class EditImagePathRect(var startPointF: PointF, var endPointF: PointF, var width: Float, var color: Int) : EditImageCacheCallback

class SimpleOnEditImageRectActionListener : OnEditImageActionListener {

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
            editImageView.cacheArrayList.add(EditImageCache.createCache(editImageView.state, this,
                    EditImagePathRect(startPointF, endPointF, width, pointPaint.color)))
        }
    }

    override fun onLastImageCache(editImageView: EditImageView, editImageCache: EditImageCache) {
        val editImagePathRect = transformerCache<EditImagePathRect>(editImageCache)

        val paint = editImageView.pointPaint
        paint.strokeWidth = editImagePathRect.width
        paint.color = editImagePathRect.color
        editImageView.newBitmapCanvas.drawRect(
                editImagePathRect.startPointF.x,
                editImagePathRect.startPointF.y,
                editImagePathRect.endPointF.x,
                editImagePathRect.endPointF.y,
                paint)
    }
}
