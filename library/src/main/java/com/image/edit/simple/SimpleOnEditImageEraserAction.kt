package com.image.edit.simple

import android.graphics.*
import com.davemorrissey.labs.subscaleview.api.getState
import com.davemorrissey.labs.subscaleview.api.viewToSourceCoord
import com.image.edit.EditImageView
import com.image.edit.action.OnEditImageAction
import com.image.edit.cache.EditImageCache
import com.image.edit.cache.createCache
import com.image.edit.x.refresh

/**
 * @author y
 * @create 2018/11/20
 */

data class EditImageEraserPath(var path: Path, var width: Float, var color: Int)

class SimpleOnEditImageEraserAction : OnEditImageAction {

    private var paintPath: Path = Path()
    private val pointF: PointF = PointF()
    private var eraserPaint: Paint = Paint()

    init {
        eraserPaint.alpha = 0
        eraserPaint.color = Color.TRANSPARENT
        eraserPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        eraserPaint.isAntiAlias = true
        eraserPaint.isDither = true
        eraserPaint.style = Paint.Style.STROKE
        eraserPaint.strokeJoin = Paint.Join.ROUND
        eraserPaint.strokeCap = Paint.Cap.ROUND
        eraserPaint.pathEffect = PathEffect()
    }

    override fun onDraw(editImageView: EditImageView, canvas: Canvas) {
        if (paintPath.isEmpty) {
            return
        }
        eraserPaint.strokeWidth = editImageView.editImageConfig.eraserPointWidth
        paintPath.lineTo(pointF.x, pointF.y)
        editImageView.newBitmapCanvas.drawPath(paintPath, eraserPaint)
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
        editImageView.cacheArrayList.add(createCache(editImageView.getState(), EditImageEraserPath(paintPath, eraserPaint.strokeWidth, eraserPaint.color)))
    }

    override fun onLastImageCache(editImageView: EditImageView, editImageCache: EditImageCache) {
        val editImageEraserPath = editImageCache.transformerCache<EditImageEraserPath>()
        eraserPaint.color = editImageEraserPath.color
        eraserPaint.strokeWidth = editImageEraserPath.width
        editImageView.newBitmapCanvas.drawPath(editImageEraserPath.path, eraserPaint)
    }
}
