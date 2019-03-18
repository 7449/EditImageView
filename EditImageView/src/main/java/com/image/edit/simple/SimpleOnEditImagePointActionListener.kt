package com.image.edit.simple

import android.graphics.Canvas
import android.graphics.Path
import android.graphics.PointF
import com.image.edit.EditImageView
import com.image.edit.action.OnEditImageActionListener
import com.image.edit.cache.EditImageCache
import com.image.edit.cache.EditImageCacheCallback
import com.image.edit.refresh
import com.image.edit.transformerCache

/**
 * @author y
 * @create 2018/11/20
 */

data class EditImagePath(var path: Path, var width: Float, var color: Int) : EditImageCacheCallback

class SimpleOnEditImagePointActionListener : OnEditImageActionListener {

    private var paintPath: Path = Path()
    private val pointF: PointF = PointF()

    override fun onDraw(editImageView: EditImageView, canvas: Canvas) {
        if (paintPath.isEmpty) {
            return
        }
        paintPath.quadTo(pointF.x, pointF.y, pointF.x, pointF.y)
        editImageView.newBitmapCanvas.drawPath(paintPath, editImageView.pointPaint)
    }

    override fun onDown(editImageView: EditImageView, x: Float, y: Float) {
        paintPath = Path()
        editImageView.viewToSourceCoord(x, y, pointF)
        paintPath.moveTo(pointF.x, pointF.y)
    }

    override fun onMove(editImageView: EditImageView, x: Float, y: Float) {
        if (Math.abs(x - pointF.x) >= 3 || Math.abs(y - pointF.y) >= 3) {
            editImageView.viewToSourceCoord(x, y, pointF)
            editImageView.refresh()
        }
    }

    override fun onUp(editImageView: EditImageView, x: Float, y: Float) {
        onSaveImageCache(editImageView)
    }

    override fun onSaveImageCache(editImageView: EditImageView) {
        val pointPaint = editImageView.pointPaint
        editImageView.cacheArrayList.add(EditImageCache.createCache(editImageView.state, this, EditImagePath(paintPath, pointPaint.strokeWidth, pointPaint.color)))
    }

    override fun onLastImageCache(editImageView: EditImageView, editImageCache: EditImageCache) {
        val editImagePath = transformerCache<EditImagePath>(editImageCache)

        val paint = editImageView.pointPaint
        paint.color = editImagePath.color
        paint.strokeWidth = editImagePath.width
        editImageView.newBitmapCanvas.drawPath(editImagePath.path, paint)
    }
}
