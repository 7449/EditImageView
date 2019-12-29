package com.image.edit.text

import android.graphics.PointF
import com.image.edit.EditImageView
import com.image.edit.EditType
import com.image.edit.action

fun EditImageView.textAction(text: String) = textAction(text, TextAction())

fun EditImageView.textAction(imageText: EditImageText) = textAction(imageText, TextAction())

fun EditImageView.textAction(text: String, editImageAction: TextAction) = textAction(EditImageText(
        PointF((width / 2).toFloat(), (height / 2).toFloat()),
        1f,
        0f,
        text,
        editImageAction.textPaint.color,
        editImageAction.textPaint.textSize,
        scale), editImageAction)

fun EditImageView.textAction(imageText: EditImageText, editImageAction: TextAction) = action(editImageAction).apply {
    if (isMaxCount) {
        onEditImageListener?.onLastCacheMax()
        return@apply
    }
    editImageAction.saveText = false
    this.editImageText = imageText
    this.editTextType = EditTextType.MOVE
    editType = EditType.ACTION
}

fun EditImageView.getTextAction() = onEditImageAction as? TextAction

fun EditImageView.hasTextAction(): Boolean {
    return (getTextAction() ?: return false).editTextType != EditTextType.NONE
}

fun EditImageView.saveText() = getTextAction()?.saveText(this)

fun TextAction.setPaintColor(paintColor: Int) = also { textPaintColor = paintColor }

fun TextAction.setRotateMode(rotateMode: Boolean) = also { isTextRotateMode = rotateMode }

fun TextAction.setPaintSize(paintSize: Float) = also { textPaintSize = paintSize }

fun TextAction.setFramePaintColor(framePaintColor: Int) = also { textFramePaintColor = framePaintColor }

fun TextAction.setFramePaintWidth(framePaintWidth: Float) = also { textFramePaintWidth = framePaintWidth }

fun TextAction.setMoveBox(moveBox: Boolean) = also { showTextMoveBox = moveBox }

fun TextAction.setDeleteDrawableId(deleteDrawableId: Int) = also { textDeleteDrawableId = deleteDrawableId }

fun TextAction.setRotateDrawableId(rotateDrawableId: Int) = also { textRotateDrawableId = rotateDrawableId }