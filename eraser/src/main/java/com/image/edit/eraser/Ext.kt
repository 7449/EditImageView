package com.image.edit.eraser

import com.image.edit.EditImageView
import com.image.edit.EditType
import com.image.edit.action

fun EditImageView.eraserAction(pointWidth: Float = 25f, isSave: Boolean = true) = eraserAction(EraserAction(pointWidth, isSave))

fun EditImageView.eraserAction(editImageAction: EraserAction) = action(editImageAction).apply { editType = EditType.ACTION }

fun EditImageView.getEraserAction() = onEditImageAction as? EraserAction

fun EraserAction.setSave(save: Boolean) = also { isSave = save }

fun EraserAction.setPointWidth(width: Float) = also { pointWidth = width }