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

data class EditImageEraserPath(var path: Path, var width: Float, var color: Int) : EditImageCacheCallback

class SimpleOnEditImageEraserActionListener : OnEditImageActionListener {

    private var paintPath: Path = Path()
    private val pointF: PointF = PointF()

    override fun onDraw(editImageView: EditImageView, canvas: Canvas) {
        if (paintPath.isEmpty) {
            return
        }
        paintPath.lineTo(pointF.x, pointF.y)
        editImageView.newBitmapCanvas.drawPath(paintPath, editImageView.eraserPaint)
    }

    override fun onDown(editImageView: EditImageView, x: Float, y: Float) {
        paintPath = Path()
        editImageView.viewToSourceCoord(x, y, pointF)
        paintPath.moveTo(pointF.x, pointF.y)
    }

    override fun onMove(editImageView: EditImageView, x: Float, y: Float) {
        editImageView.viewToSourceCoord(x, y, pointF)
        editImageView.refresh()
    }

    override fun onUp(editImageView: EditImageView, x: Float, y: Float) {
        onSaveImageCache(editImageView)
    }

    override fun onSaveImageCache(editImageView: EditImageView) {
        if (!editImageView.editImageConfig.eraserSave) {
            return
        }
        val pointPaint = editImageView.eraserPaint
        editImageView.cacheArrayList.add(EditImageCache.createCache(editImageView.state, this, EditImagePath(paintPath, pointPaint.strokeWidth, pointPaint.color)))
    }

    override fun onLastImageCache(editImageView: EditImageView, editImageCache: EditImageCache) {

        val editImageEraserPath = transformerCache<EditImageEraserPath>(editImageCache)

        val eraserPaint = editImageView.eraserPaint
        eraserPaint.color = editImageEraserPath.color
        eraserPaint.strokeWidth = editImageEraserPath.width
        editImageView.newBitmapCanvas.drawPath(editImageEraserPath.path, eraserPaint)
    }
}
