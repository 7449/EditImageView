package com.image.edit.x

import android.graphics.Rect
import android.graphics.RectF
import com.image.edit.cache.EditImageText


/**
 * @author y
 * @create 2018/11/17
 */
object MatrixAndRectHelper {

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
