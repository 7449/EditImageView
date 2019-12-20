package com.image.edit.eraser

import android.graphics.*
import com.image.edit.EditImageView
import com.image.edit.OnEditImageAction
import com.image.edit.EditImageCache
import com.image.edit.createCache

/**
 * @author y
 * @create 2018/11/20
 */
class EraserAction(
        var pointWidth: Float = 25f,
        var isSave: Boolean = true
) : OnEditImageAction<EraserPath> {

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
        eraserPaint.strokeWidth = pointWidth
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
        editImageView.invalidate()
    }

    override fun onUp(editImageView: EditImageView, x: Float, y: Float) {
        onSaveImageCache(editImageView)
    }

    override fun onSaveImageCache(editImageView: EditImageView) {
        if (!isSave) {
            return
        }
        editImageView.cacheArrayList.add(createCache(editImageView.state, EraserPath(paintPath, eraserPaint.strokeWidth, eraserPaint.color)))
    }

    override fun onLastImageCache(editImageView: EditImageView, editImageCache: EditImageCache<EraserPath>) {
        val eraserPath = editImageCache.imageCache
        eraserPaint.color = eraserPath.color
        eraserPaint.strokeWidth = eraserPath.width
        editImageView.newBitmapCanvas.drawPath(eraserPath.path, eraserPaint)
    }
}
