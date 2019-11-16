package com.image.edit.point

import android.graphics.*
import com.davemorrissey.labs.subscaleview.api.getState
import com.davemorrissey.labs.subscaleview.api.viewToSourceCoord
import com.image.edit.EditImageView
import com.image.edit.OnEditImageAction
import com.image.edit.EditImageCache
import com.image.edit.createCache
import kotlin.math.abs

/**
 * @author y
 * @create 2018/11/20
 */
class PointAction(
        var pointColor: Int = Color.RED,
        var pointWidth: Float = 20f
) : OnEditImageAction<PointPath> {

    private var paintPath: Path = Path()
    private val pointF: PointF = PointF()
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
        if (paintPath.isEmpty) {
            return
        }
        paintPath.quadTo(pointF.x, pointF.y, pointF.x, pointF.y)
        pointPaint.color = pointColor
        pointPaint.strokeWidth = pointWidth
        editImageView.newBitmapCanvas.drawPath(paintPath, pointPaint)
    }

    override fun onDown(editImageView: EditImageView, x: Float, y: Float) {
        paintPath = Path()
        editImageView.viewToSourceCoord(x, y, pointF)
        paintPath.moveTo(pointF.x, pointF.y)
    }

    override fun onMove(editImageView: EditImageView, x: Float, y: Float) {
        if (abs(x - pointF.x) >= 3 || abs(y - pointF.y) >= 3) {
            editImageView.viewToSourceCoord(x, y, pointF)
            editImageView.invalidate()
        }
    }

    override fun onUp(editImageView: EditImageView, x: Float, y: Float) {
        onSaveImageCache(editImageView)
    }

    override fun onSaveImageCache(editImageView: EditImageView) {
        editImageView.cacheArrayList.add(createCache(editImageView.getState(), PointPath(paintPath, pointPaint.strokeWidth, pointPaint.color)))
    }

    override fun onLastImageCache(editImageView: EditImageView, editImageCache: EditImageCache<PointPath>) {
        val pointPath = editImageCache.imageCache
        pointPaint.color = pointPath.color
        pointPaint.strokeWidth = pointPath.width
        editImageView.newBitmapCanvas.drawPath(pointPath.path, pointPaint)
    }
}
