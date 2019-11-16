package com.image.edit.react

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PathEffect
import android.graphics.PointF
import com.davemorrissey.labs.subscaleview.api.getState
import com.davemorrissey.labs.subscaleview.api.viewToSourceCoord
import com.image.edit.*
import com.image.edit.cache.EditImageCache

/**
 * @author y
 * @create 2018/11/20
 */
class RectAction : OnEditImageAction<EditImagePathRect> {

    private var startPointF: PointF? = null
    private var endPointF: PointF? = null
    private var pointPaint: Paint = Paint()

    init {
        pointPaint.flags = Paint.ANTI_ALIAS_FLAG
        pointPaint.isAntiAlias = true
        pointPaint.isDither = true
        pointPaint.strokeJoin = Paint.Join.ROUND
        pointPaint.strokeCap = Paint.Cap.ROUND
        pointPaint.pathEffect = PathEffect()
        pointPaint.style = Paint.Style.STROKE
    }

    override fun onDraw(editImageView: EditImageView, canvas: Canvas) {
        allNotNull(startPointF, endPointF) { startPointF, endPointF ->
            pointPaint.color = editImageView.editImageConfig.pointColor
            pointPaint.strokeWidth = editImageView.editImageConfig.pointWidth
            canvas.drawRect(startPointF.x, startPointF.y, endPointF.x, endPointF.y, pointPaint)
        }
    }

    override fun onDown(editImageView: EditImageView, x: Float, y: Float) {
        startPointF = PointF()
        endPointF = PointF()
        startPointF?.set(x, y)
    }

    override fun onMove(editImageView: EditImageView, x: Float, y: Float) {
        endPointF?.set(x, y)
        editImageView.invalidate()
    }

    override fun onUp(editImageView: EditImageView, x: Float, y: Float) {
        allNotNull(startPointF, endPointF) { startPointF, endPointF ->
            if (checkCoordinate(startPointF, endPointF, x, y)) {
                return
            }
            editImageView.viewToSourceCoord(startPointF, startPointF)
            editImageView.viewToSourceCoord(endPointF, endPointF)
            pointPaint.strokeWidth /= editImageView.scale
            editImageView.newBitmapCanvas.drawRect(startPointF.x, startPointF.y, endPointF.x, endPointF.y, pointPaint)
            onSaveImageCache(editImageView)
        }
        startPointF = null
        endPointF = null
    }

    override fun onSaveImageCache(editImageView: EditImageView) {
        allNotNull(startPointF, endPointF) { startPointF, endPointF ->
            editImageView.cacheArrayList.add(createCache(editImageView.getState(), EditImagePathRect(startPointF, endPointF, pointPaint.strokeWidth, pointPaint.color)))
        }
    }

    override fun onLastImageCache(editImageView: EditImageView, editImageCache: EditImageCache<EditImagePathRect>) {
        val editImagePathRect = editImageCache.imageCache

        pointPaint.strokeWidth = editImagePathRect.width
        pointPaint.color = editImagePathRect.color
        editImageView.newBitmapCanvas.drawRect(
                editImagePathRect.startPointF.x,
                editImagePathRect.startPointF.y,
                editImagePathRect.endPointF.x,
                editImagePathRect.endPointF.y,
                pointPaint)
    }
}
