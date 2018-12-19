package com.image.edit.helper

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import com.image.edit.cache.EditImageText

/**
 * @author y
 * @create 2018/11/17
 */
object MatrixAndRectHelper {

    fun scaleRect(rect: RectF, scale: Float) {
        val w = rect.width()
        val h = rect.height()
        val newW = scale * w
        val newH = scale * h
        val dx = (newW - w) / 2
        val dy = (newH - h) / 2
        rect.left -= dx
        rect.top -= dy
        rect.right += dx
        rect.bottom += dy
    }

    fun rotateRect(rect: RectF, centerX: Float, centerY: Float, rotate: Float) {
        val x = rect.centerX()
        val y = rect.centerY()
        val sinA = Math.sin(Math.toRadians(rotate.toDouble())).toFloat()
        val cosA = Math.cos(Math.toRadians(rotate.toDouble())).toFloat()
        val newX = centerX + (x - centerX) * cosA - (y - centerY) * sinA
        val newY = centerY + (y - centerY) * cosA + (x - centerX) * sinA
        val dx = newX - x
        val dy = newY - y
        rect.offset(dx, dy)
    }

    fun rectAddV(srcRect: Rect, addRect: Rect, padding: Int) {
        val left = srcRect.left
        val top = srcRect.top
        var right = srcRect.right
        var bottom = srcRect.bottom
        if (srcRect.width() <= addRect.width()) {
            right = left + addRect.width()
        }
        bottom += padding + Math.max(addRect.height(), 60)
        srcRect.set(left, top, right, bottom)
    }

    fun rectAddV(srcRect: RectF, addRect: Rect, padding: Int, charMinHeight: Int) {
        val left = srcRect.left
        val top = srcRect.top
        var right = srcRect.right
        var bottom = srcRect.bottom
        if (srcRect.width() <= addRect.width()) {
            right = left + addRect.width()
        }
        bottom += padding + Math.max(addRect.height(), charMinHeight)
        srcRect.set(left, top, right, bottom)
    }

    fun refreshRotateAndScale(editImageText: EditImageText, mMoveBoxRect: RectF, textRotateDstRect: RectF, dx: Float, dy: Float) {
        val cX = mMoveBoxRect.centerX()
        val cY = mMoveBoxRect.centerY()
        val x = textRotateDstRect.centerX()
        val y = textRotateDstRect.centerY()
        val nX = x + dx
        val nY = y + dy
        val xa = x - cX
        val ya = y - cY
        val xb = nX - cX
        val yb = nY - cY
        val srcLen = Math.sqrt((xa * xa + ya * ya).toDouble()).toFloat()
        val curLen = Math.sqrt((xb * xb + yb * yb).toDouble()).toFloat()
        val scale = curLen / srcLen
        editImageText.scale = editImageText.scale * scale
        val newWidth = mMoveBoxRect.width() * editImageText.scale
        if (newWidth < 70) {
            editImageText.scale = editImageText.scale / scale
            return
        }
        val cos = (xa * xb + ya * yb) / (srcLen * curLen)
        if (cos > 1 || cos < -1)
            return
        var angle = Math.toDegrees(Math.acos(cos.toDouble())).toFloat()
        val calMatrix = xa * yb - xb * ya
        val flag = (if (calMatrix > 0) 1 else -1).toFloat()
        angle *= flag
        editImageText.rotate = editImageText.rotate + angle
    }

    fun refreshMatrix(canvas: Canvas, matrix: Matrix, callBack: (Any, Any, Any, Any) -> Unit) {
        val data = FloatArray(9)
        matrix.getValues(data)
        val cal = Matrix3(data)
        val inverseMatrix = cal.inverseMatrix()
        val m = Matrix()
        m.setValues(inverseMatrix.values)
        val f = FloatArray(9)
        m.getValues(f)
        val dx = f[Matrix.MTRANS_X].toInt()
        val dy = f[Matrix.MTRANS_Y].toInt()
        val scaleX = f[Matrix.MSCALE_X]
        val scaleY = f[Matrix.MSCALE_Y]
        canvas.save()
        canvas.translate(dx.toFloat(), dy.toFloat())
        canvas.scale(scaleX, scaleY)
        callBack(dx, dy, scaleX, scaleY)
        canvas.restore()
    }
}
