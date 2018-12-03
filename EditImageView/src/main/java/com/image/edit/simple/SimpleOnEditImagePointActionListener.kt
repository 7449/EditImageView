package com.image.edit.simple

import android.graphics.Canvas
import android.graphics.Path
import android.graphics.PointF
import com.image.edit.EditImageView
import com.image.edit.action.OnEditImagePointActionListener
import com.image.edit.cache.EditImageCache
import com.image.edit.cache.EditImagePath

/**
 * @author y
 * @create 2018/11/20
 */
class SimpleOnEditImagePointActionListener : OnEditImagePointActionListener {

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
        editImageView.cacheArrayList.add(EditImageCache.createPointCache(editImageView.state, this, EditImagePath(paintPath, pointPaint.strokeWidth, pointPaint.color)))
    }

    override fun onLastImageCache(editImageView: EditImageView, editImageCache: EditImageCache) {
        val paint = editImageView.pointPaint
        paint.color = editImageCache.editImagePath.color
        paint.strokeWidth = editImageCache.editImagePath.width
        editImageView.newBitmapCanvas.drawPath(editImageCache.editImagePath.path, paint)
    }
}
