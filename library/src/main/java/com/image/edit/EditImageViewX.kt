@file:Suppress("FunctionName")

package com.image.edit

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.PointF
import com.image.edit.action.OnEditImageAction
import com.image.edit.simple.*
import com.image.edit.simple.text.EditTextType
import com.image.edit.simple.text.Matrix3
import com.image.edit.type.EditType

/**
 * @author y
 * @create 2019/3/18
 */

fun EditImageView.recycleDrawBitmap() = newBitmap.supportRecycle()

fun EditImageView.newCanvasBitmap(): Bitmap {
    val bitmap = Bitmap.createBitmap(sWidth, sHeight, Bitmap.Config.ARGB_8888)
    val defaultBitmap = getBitmap()
    val canvas = Canvas(bitmap)
    if (defaultBitmap != null) {
        canvas.drawBitmap(defaultBitmap, 0f, 0f, null)
    }
    canvas.drawBitmap(newBitmap, 0f, 0f, null)
    canvas.save()
    return bitmap
}

fun EditImageView.reset() {
    recycleDrawBitmap()
    newBitmap = Bitmap.createBitmap(sWidth, sHeight, Bitmap.Config.ARGB_8888)
    newBitmapCanvas = Canvas(newBitmap)
}

fun EditImageView.clearImage() {
    if (cacheArrayList.isEmpty()) {
        onEditImageListener?.onLastImageEmpty()
        return
    }
    for (editImageCache in cacheArrayList) {
        editImageCache.reset()
    }
    cacheArrayList.clear()
    reset()
    editType = EditType.NONE
}

fun EditImageView.lastImage() {
    if (cacheArrayList.isEmpty()) {
        onEditImageListener?.onLastImageEmpty()
        return
    }
    cacheArrayList.removeLast().reset()
    reset()
    for (editImageCache in cacheArrayList) {
        editImageCache.onEditImageAction?.onLastImageCache(this, editImageCache)
    }
    editType = EditType.NONE
}

fun EditImageView.refresh() = invalidate()

fun EditImageView.paint() = paint(SimpleOnEditImageInitializeListener())

fun EditImageView.paint(editImageInitializeListener: SimpleOnEditImageInitializeListener) = apply { onEditImageInitializeListener = editImageInitializeListener }

fun EditImageView.noneAction() = let { editType = EditType.NONE }

fun EditImageView.circleAction() = circleAction(SimpleOnEditImageCircleAction())

fun EditImageView.circleAction(editImageAction: OnEditImageAction) = action(editImageAction).let { editType = EditType.ACTION }

fun EditImageView.lineAction() = lineAction(SimpleOnEditImageLineAction())

fun EditImageView.lineAction(editImageAction: OnEditImageAction) = action(editImageAction).let { editType = EditType.ACTION }

fun EditImageView.pointAction() = pointAction(SimpleOnEditImagePointAction())

fun EditImageView.pointAction(editImageAction: OnEditImageAction) = action(editImageAction).let { editType = EditType.ACTION }

fun EditImageView.rectAction() = rectAction(SimpleOnEditImageRectAction())

fun EditImageView.rectAction(editImageAction: OnEditImageAction) = action(editImageAction).let { editType = EditType.ACTION }

fun EditImageView.eraserAction() = eraserAction(SimpleOnEditImageEraserAction())

fun EditImageView.eraserAction(editImageAction: OnEditImageAction) = action(editImageAction).let { editType = EditType.ACTION }

fun EditImageView.textAction(text: String) = textAction(text, SimpleOnEditImageTextAction())

fun EditImageView.textAction(imageText: EditImageText) = textAction(imageText, SimpleOnEditImageTextAction())

fun EditImageView.textAction(text: String, editImageAction: OnEditImageAction) = let {
    val pointF = PointF((resources.displayMetrics.widthPixels / 2).toFloat(), (resources.displayMetrics.widthPixels / 2).toFloat())
    val editImageText = EditImageText(viewToSourceCoord(pointF, pointF)
            ?: pointF, 1f, 0f, text, textPaint.color, textPaint.textSize)
    textAction(editImageText, editImageAction)
}

fun EditImageView.textAction(imageText: EditImageText, editImageAction: OnEditImageAction) = action(editImageAction).apply { editImageText = imageText }.apply { editTextType = EditTextType.MOVE }.let { editType = EditType.ACTION }

fun EditImageView.hasTextAction() = editTextType != EditTextType.NONE

fun EditImageView.saveText() = supperMatrix?.let { onEditImageAction?.onSaveImageCache(this) }

fun EditImageView.customAction(editImageAction: OnEditImageAction) = action(editImageAction).let { editType = EditType.ACTION }

internal fun EditImageView.action(editImageAction: OnEditImageAction) = apply { onEditImageAction = editImageAction }

internal fun EditImageView.booleanVar(var1: Any, var2: Any) = var1 == var2

internal fun EditImageView.refreshConfig() {
    pointPaint.color = editImageConfig.pointColor
    pointPaint.strokeWidth = editImageConfig.pointWidth
    eraserPaint.strokeWidth = editImageConfig.eraserPointWidth
    textPaint.textAlign = editImageConfig.textPaintAlign
    textPaint.textSize = editImageConfig.textPaintSize
    textPaint.color = editImageConfig.textPaintColor
    framePaint.strokeWidth = editImageConfig.textFramePaintWidth
    framePaint.color = editImageConfig.textFramePaintColor
}

fun Bitmap?.supportRecycle() {
    if (this?.isRecycled == false) {
        recycle()
    }
}

inline fun <A, B> AllNotNull(first: A?, second: B?, block: (A, B) -> Unit) {
    if (first != null && second != null) block(first, second)
}

inline fun <A, B, C> AllNotNull(first: A?, second: B?, third: C?, block: (A, B, C) -> Unit) {
    if (first != null && second != null && third != null) block(first, second, third)
}

fun Canvas.refreshMatrix(matrix: Matrix, callBack: (Any, Any, Any, Any) -> Unit) {
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
