package com.image.edit

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PointF
import com.image.edit.action.OnEditImageActionListener
import com.image.edit.helper.supportRecycle
import com.image.edit.simple.*

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
        editImageCache.onEditImageActionListener?.onLastImageCache(this, editImageCache)
    }
    editType = EditType.NONE
}

fun EditImageView.refresh() = invalidate()

fun EditImageView.paint() = paint(SimpleOnEditImageInitializeListener())

fun EditImageView.paint(editImageInitializeListener: SimpleOnEditImageInitializeListener) = apply { onEditImageInitializeListener = editImageInitializeListener }

fun EditImageView.noneAction() = let { editType = EditType.NONE }

fun EditImageView.circleAction() = circleAction(SimpleOnEditImageCircleActionListener())

fun EditImageView.circleAction(editImageActionListener: OnEditImageActionListener) = action(editImageActionListener).let { editType = EditType.ACTION }

fun EditImageView.lineAction() = lineAction(SimpleOnEditImageLineActionListener())

fun EditImageView.lineAction(editImageActionListener: OnEditImageActionListener) = action(editImageActionListener).let { editType = EditType.ACTION }

fun EditImageView.pointAction() = pointAction(SimpleOnEditImagePointActionListener())

fun EditImageView.pointAction(editImageActionListener: OnEditImageActionListener) = action(editImageActionListener).let { editType = EditType.ACTION }

fun EditImageView.rectAction() = rectAction(SimpleOnEditImageRectActionListener())

fun EditImageView.rectAction(editImageActionListener: OnEditImageActionListener) = action(editImageActionListener).let { editType = EditType.ACTION }

fun EditImageView.eraserAction() = eraserAction(SimpleOnEditImageEraserActionListener())

fun EditImageView.eraserAction(editImageActionListener: OnEditImageActionListener) = action(editImageActionListener).let { editType = EditType.ACTION }

fun EditImageView.textAction(text: String) = textAction(text, SimpleOnEditImageTextActionListener())

fun EditImageView.textAction(imageText: EditImageText) = textAction(imageText, SimpleOnEditImageTextActionListener())

fun EditImageView.textAction(text: String, editImageActionListener: OnEditImageActionListener) = let {
    val pointF = PointF((resources.displayMetrics.widthPixels / 2).toFloat(), (resources.displayMetrics.widthPixels / 2).toFloat())
    val editImageText = EditImageText(viewToSourceCoord(pointF, pointF)
            ?: pointF, 1f, 0f, text, textPaint.color, textPaint.textSize)
    textAction(editImageText, editImageActionListener)
}

fun EditImageView.textAction(imageText: EditImageText, editImageActionListener: OnEditImageActionListener) = action(editImageActionListener).apply { editImageText = imageText }.apply { editTextType = EditTextType.MOVE }.let { editType = EditType.ACTION }

fun EditImageView.hasTextAction() = editTextType != EditTextType.NONE

fun EditImageView.saveText() = supperMatrix?.let { onEditImageActionListener?.onSaveImageCache(this) }

fun EditImageView.customAction(editImageActionListener: OnEditImageActionListener) = action(editImageActionListener).let { editType = EditType.ACTION }

internal fun EditImageView.action(editImageActionListener: OnEditImageActionListener) = apply { onEditImageActionListener = editImageActionListener }

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