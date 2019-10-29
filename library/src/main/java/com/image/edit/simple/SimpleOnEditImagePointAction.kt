package com.image.edit.simple

import android.graphics.*
import com.davemorrissey.labs.subscaleview.api.getState
import com.davemorrissey.labs.subscaleview.api.viewToSourceCoord
import com.image.edit.EditImageView
import com.image.edit.action.OnEditImageAction
import com.image.edit.cache.EditImageCache
import com.image.edit.cache.createCache
import com.image.edit.x.refresh
import kotlin.math.abs

/**
 * @author y
 * @create 2018/11/20
 */

data class EditImagePath(var path: Path, var width: Float, var color: Int)

class SimpleOnEditImagePointAction : OnEditImageAction {

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
        pointPaint.color = editImageView.editImageConfig.pointColor
        pointPaint.strokeWidth = editImageView.editImageConfig.pointWidth
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
            editImageView.refresh()
        }
    }

    override fun onUp(editImageView: EditImageView, x: Float, y: Float) {
        onSaveImageCache(editImageView)
    }

    override fun onSaveImageCache(editImageView: EditImageView) {
        editImageView.cacheArrayList.add(createCache(editImageView.getState(), EditImagePath(paintPath, pointPaint.strokeWidth, pointPaint.color)))
    }

    override fun onLastImageCache(editImageView: EditImageView, editImageCache: EditImageCache) {
        val editImagePath = editImageCache.transformerCache<EditImagePath>()
        pointPaint.color = editImagePath.color
        pointPaint.strokeWidth = editImagePath.width
        editImageView.newBitmapCanvas.drawPath(editImagePath.path, pointPaint)
    }
}
