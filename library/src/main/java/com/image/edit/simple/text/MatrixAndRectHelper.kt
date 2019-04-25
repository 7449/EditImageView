package com.image.edit.simple.text

import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF


/**
 * @author y
 * @create 2018/11/17
 */
internal object MatrixAndRectHelper {

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
        editImageText.rotate += angle
    }
}

fun RectF.rotateRect(centerX: Float, centerY: Float, rotate: Float) {
    val x = centerX()
    val y = centerY()
    val sinA = Math.sin(Math.toRadians(rotate.toDouble())).toFloat()
    val cosA = Math.cos(Math.toRadians(rotate.toDouble())).toFloat()
    val newX = centerX + (x - centerX) * cosA - (y - centerY) * sinA
    val newY = centerY + (y - centerY) * cosA + (x - centerX) * sinA
    val dx = newX - x
    val dy = newY - y
    offset(dx, dy)
}

fun PointF.rotatePoint(center_x: Float, center_y: Float, rotate: Float) {
    val sinA = Math.sin(Math.toRadians(rotate.toDouble())).toFloat()
    val cosA = Math.cos(Math.toRadians(rotate.toDouble())).toFloat()
    val newX = center_x + (x - center_x) * cosA - (y - center_y) * sinA
    val newY = center_y + (y - center_y) * cosA + (x - center_x) * sinA
    set(newX, newY)
}

fun RectF.scaleRect(scale: Float) {
    val newW = scale * width()
    val newH = scale * height()
    val dx = (newW - width()) / 2
    val dy = (newH - height()) / 2
    left -= dx
    top -= dy
    right += dx
    bottom += dy
}
