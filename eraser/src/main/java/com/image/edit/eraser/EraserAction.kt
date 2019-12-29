package com.image.edit.eraser

import android.graphics.*
import com.image.edit.*
import kotlin.math.abs

/**
 * @author y
 * @create 2018/11/20
 */
@Deprecated("")
class EraserAction(
        var pointWidth: Float = 20f,
        var isSave: Boolean = true
) : OnEditImageAction<EraserPath> {

    private val paintPath = Path()
    private val eraserPaint = Paint()
    private val listPointF = ArrayList<PointF>()
    private val cachePointF = PointF()

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
        if (onNoDraw()) {
            return
        }
        eraserPaint.strokeWidth = pointWidth
        paintPath.reset()
        paintPath.moveTo(listPointF[0].x, listPointF[0].y)
        for (i in 1 until listPointF.size) {
            val pointF = listPointF[i]
            paintPath.quadTo(pointF.x, pointF.y, pointF.x, pointF.y)
        }
        canvas.drawPath(paintPath, eraserPaint)
    }

    override fun onDrawCache(editImageView: EditImageView, canvas: Canvas, editImageCache: EditImageCache<EraserPath>) {
        val eraserPath = editImageCache.imageCache

        if (eraserPath.pointFList.size <= 1) {
            return
        }

        val strokeWidth = when {
            eraserPath.scale == editImageView.scale -> {
                eraserPath.width
            }
            eraserPath.scale > editImageView.scale -> {
                eraserPath.width / (eraserPath.scale / editImageView.scale)
            }
            else -> {
                eraserPath.width * (editImageView.scale / eraserPath.scale)
            }
        }

        eraserPaint.color = eraserPath.color
        eraserPaint.strokeWidth = strokeWidth
        paintPath.reset()
        editImageView.sourceToViewCoord(eraserPath.pointFList[0], cachePointF)
        paintPath.moveTo(cachePointF.x, cachePointF.y)
        for (i in 1 until eraserPath.pointFList.size) {
            editImageView.sourceToViewCoord(eraserPath.pointFList[i], cachePointF)
            paintPath.quadTo(cachePointF.x, cachePointF.y, cachePointF.x, cachePointF.y)
        }
        canvas.drawPath(paintPath, eraserPaint)
    }

    override fun onDrawBitmap(editImageView: EditImageView, canvas: Canvas, editImageCache: EditImageCache<EraserPath>) {
        val eraserPath = editImageCache.imageCache
        eraserPaint.color = eraserPath.color
        eraserPaint.strokeWidth = eraserPath.width / eraserPath.scale
        paintPath.reset()
        paintPath.moveTo(eraserPath.pointFList[0].x, eraserPath.pointFList[0].y)
        for (i in 1 until eraserPath.pointFList.size) {
            val pointF = eraserPath.pointFList[i]
            paintPath.quadTo(pointF.x, pointF.y, pointF.x, pointF.y)
        }
        canvas.drawPath(paintPath, eraserPaint)
    }

    override fun onDown(editImageView: EditImageView, x: Float, y: Float) {
        listPointF.add(PointF(x, y))
        paintPath.moveTo(x, y)
    }

    override fun onMove(editImageView: EditImageView, x: Float, y: Float) {
        val pointF = listPointF[listPointF.size - 1]
        if (abs(x - pointF.x) >= 3 || abs(y - pointF.y) >= 3) {
            listPointF.add(PointF(x, y))
            editImageView.invalidate()
        }
    }

    override fun onUp(editImageView: EditImageView, x: Float, y: Float) {
        if (onNoDraw()) {
            listPointF.clear()
            return
        }
        if (editImageView.isMaxCount) {
            editImageView.onEditImageListener?.onLastCacheMax()
            return
        }
        val newList = ArrayList<PointF>()
        listPointF.forEach { newList.add(editImageView.viewToSourceCoords(it)) }
        editImageView.cacheArrayList.add(createCache(editImageView.state, EraserPath(
                newList,
                eraserPaint.strokeWidth,
                eraserPaint.color,
                editImageView.scale)))
        listPointF.clear()
    }

    override fun onNoDraw(): Boolean {
        return listPointF.isEmpty() || listPointF.size <= 3
    }
}
