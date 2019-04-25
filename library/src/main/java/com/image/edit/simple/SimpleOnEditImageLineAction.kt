package com.image.edit.simple

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PathEffect
import android.graphics.PointF
import com.image.edit.EditImageView
import com.image.edit.action.OnEditImageAction
import com.image.edit.cache.EditImageCache
import com.image.edit.cache.createCache
import com.image.edit.x.AllNotNull
import com.image.edit.x.refresh

/**
 * @author y
 * @create 2018/11/20
 */

data class EditImagePathLine(var startPointF: PointF, var endPointF: PointF, var width: Float, var color: Int)

class SimpleOnEditImageLineAction : OnEditImageAction {

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
        AllNotNull(startPointF, endPointF) { startPointF, endPointF ->
            pointPaint.color = editImageView.editImageConfig.pointColor
            pointPaint.strokeWidth = editImageView.editImageConfig.pointWidth
            canvas.drawLine(startPointF.x, startPointF.y, endPointF.x, endPointF.y, pointPaint)
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
        AllNotNull(startPointF, endPointF) { startPointF, endPointF ->
            editImageView.viewToSourceCoord(startPointF, startPointF)
            editImageView.viewToSourceCoord(endPointF, endPointF)
            pointPaint.strokeWidth /= editImageView.scale
            editImageView.newBitmapCanvas.drawLine(startPointF.x, startPointF.y, endPointF.x, endPointF.y, pointPaint)
            onSaveImageCache(editImageView)
        }
        startPointF = null
        endPointF = null
    }

    override fun onSaveImageCache(editImageView: EditImageView) {
        AllNotNull(startPointF, endPointF) { startPointF, endPointF ->
            editImageView.cacheArrayList.add(createCache(editImageView.state, EditImagePathLine(startPointF, endPointF, pointPaint.strokeWidth, pointPaint.color)))
        }
    }

    override fun onLastImageCache(editImageView: EditImageView, editImageCache: EditImageCache) {
        val transformerCache = editImageCache.transformerCache<EditImagePathLine>()
        pointPaint.color = transformerCache.color
        pointPaint.strokeWidth = transformerCache.width
        editImageView.newBitmapCanvas.drawLine(
                transformerCache.startPointF.x,
                transformerCache.startPointF.y,
                transformerCache.endPointF.x,
                transformerCache.endPointF.y,
                pointPaint)
    }
}
