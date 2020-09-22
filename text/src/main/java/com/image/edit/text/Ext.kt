package com.image.edit.text

import android.graphics.PointF
import com.image.edit.virtual.OnEditImageCallback

fun OnEditImageCallback.textAction(text: String) = textAction(text, TextAction())

fun OnEditImageCallback.textAction(imageText: EditImageText) = textAction(imageText, TextAction())

fun OnEditImageCallback.textAction(text: String, editImageAction: TextAction) = textAction(EditImageText(
        PointF(300f, 300f),
        1f,
        0f,
        text,
        editImageAction.textPaint.color,
        editImageAction.textPaint.textSize,
        viewScale), editImageAction)

fun OnEditImageCallback.textAction(imageText: EditImageText, editImageAction: TextAction) = apply {
    editImageAction.saveText = false
    editImageAction.editImageText = imageText
    editImageAction.editTextType = EditTextType.MOVE
}.action(editImageAction)

fun OnEditImageCallback.getTextAction() = onEditImageAction as? TextAction

fun OnEditImageCallback.hasTextAction(): Boolean {
    return (getTextAction() ?: return false).editTextType != EditTextType.NONE
}

fun OnEditImageCallback.noneTextAction() {
    getTextAction()?.editTextType = EditTextType.NONE
}

fun OnEditImageCallback.saveText() = getTextAction()?.saveText(this)

fun TextAction.setPaintColor(paintColor: Int) = also { textPaintColor = paintColor }

fun TextAction.setRotateMode(rotateMode: Boolean) = also { isTextRotateMode = rotateMode }

fun TextAction.setPaintSize(paintSize: Float) = also { textPaintSize = paintSize }

fun TextAction.setFramePaintColor(framePaintColor: Int) = also { textFramePaintColor = framePaintColor }

fun TextAction.setFramePaintWidth(framePaintWidth: Float) = also { textFramePaintWidth = framePaintWidth }

fun TextAction.setMoveBox(moveBox: Boolean) = also { showTextMoveBox = moveBox }

fun TextAction.setDeleteDrawableId(deleteDrawableId: Int) = also { textDeleteDrawableId = deleteDrawableId }

fun TextAction.setRotateDrawableId(rotateDrawableId: Int) = also { textRotateDrawableId = rotateDrawableId }