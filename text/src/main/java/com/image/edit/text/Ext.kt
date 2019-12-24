package com.image.edit.text

import android.graphics.PointF
import com.image.edit.EditImageView
import com.image.edit.EditType
import com.image.edit.action

fun EditImageView.textAction(text: String) = textAction(text, TextAction())

fun EditImageView.textAction(imageText: EditImageText) = textAction(imageText, TextAction())

fun EditImageView.textAction(text: String, editImageAction: TextAction) = run {
    val pointF = PointF((resources.displayMetrics.widthPixels / 2).toFloat(), (resources.displayMetrics.widthPixels / 2).toFloat())
    val editImageText = EditImageText(viewToSourceCoord(pointF, pointF)
            ?: pointF, 1f, 0f, text, editImageAction.textPaint.color, editImageAction.textPaint.textSize)
    textAction(editImageText, editImageAction)
}

fun EditImageView.textAction(imageText: EditImageText, editImageAction: TextAction) = action(editImageAction).apply { this.editImageText = imageText }.apply { editType = EditType.ACTION }.apply { this.editTextType = EditTextType.MOVE }

fun EditImageView.getTextAction() = onEditImageAction as? TextAction

fun EditImageView.hasTextAction(): Boolean {
    if (onEditImageAction is TextAction) {
        return (onEditImageAction as TextAction).editTextType != EditTextType.NONE
    }
    return false
}

fun EditImageView.saveText() = onEditImageAction?.onSaveImageCache(this)

fun TextAction.setPaintColor(paintColor: Int) = also { textPaintColor = paintColor }

fun TextAction.setRotateMode(rotateMode: Boolean) = also { isTextRotateMode = rotateMode }

fun TextAction.setPaintSize(paintSize: Float) = also { textPaintSize = paintSize }

fun TextAction.setFramePaintColor(framePaintColor: Int) = also { textFramePaintColor = framePaintColor }

fun TextAction.setFramePaintWidth(framePaintWidth: Float) = also { textFramePaintWidth = framePaintWidth }

fun TextAction.setMoveBox(moveBox: Boolean) = also { showTextMoveBox = moveBox }

fun TextAction.setDeleteDrawableId(deleteDrawableId: Int) = also { textDeleteDrawableId = deleteDrawableId }

fun TextAction.setRotateDrawableId(rotateDrawableId: Int) = also { textRotateDrawableId = rotateDrawableId }