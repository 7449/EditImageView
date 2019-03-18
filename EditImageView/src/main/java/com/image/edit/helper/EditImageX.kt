@file:Suppress("FunctionName")

package com.image.edit.helper

import android.graphics.*

fun Bitmap?.supportRecycle() {
    if (this?.isRecycled == false) {
        recycle()
    }
}

fun <A, B> AllNotNull(first: A?, second: B?, block: (A, B) -> Unit) {
    if (first != null && second != null) block(first, second)
}

fun <A, B, C> AllNotNull(first: A?, second: B?, third: C?, block: (A, B, C) -> Unit) {
    if (first != null && second != null && third != null) block(first, second, third)
}

internal fun RectF.scaleRect(scale: Float) {
    val newW = scale * width()
    val newH = scale * height()
    val dx = (newW - width()) / 2
    val dy = (newH - height()) / 2
    left -= dx
    top -= dy
    right += dx
    bottom += dy
}

internal fun RectF.rotateRect(centerX: Float, centerY: Float, rotate: Float) {
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

internal fun PointF.rotatePoint(center_x: Float, center_y: Float, rotate: Float) {
    val sinA = Math.sin(Math.toRadians(rotate.toDouble())).toFloat()
    val cosA = Math.cos(Math.toRadians(rotate.toDouble())).toFloat()
    val newX = center_x + (x - center_x) * cosA - (y - center_y) * sinA
    val newY = center_y + (y - center_y) * cosA + (x - center_x) * sinA
    set(newX, newY)
}

internal fun Canvas.refreshMatrix(matrix: Matrix, callBack: (Any, Any, Any, Any) -> Unit) {
    val data = FloatArray(9)
    matrix.getValues(data)
    val cal = Matrix3(data)
    val inverseMatrix = cal.inverseMatrix()
    val m = Matrix()
    m.setValues(inverseMatrix.values)
    val f = FloatArray(9)
    m.getValues(f)
    val dx = f[Matrix.MTRANS_X]
    val dy = f[Matrix.MTRANS_Y]
    val scaleX = f[Matrix.MSCALE_X]
    val scaleY = f[Matrix.MSCALE_Y]
    save()
    translate(dx, dy)
    scale(scaleX, scaleY)
    callBack(dx, dy, scaleX, scaleY)
    restore()
}

