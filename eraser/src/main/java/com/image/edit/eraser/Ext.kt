package com.image.edit.eraser

import com.image.edit.EditImageView
import com.image.edit.EditType
import com.image.edit.action

/**
 * 橡皮擦
 * [EraserAction]
 */
fun EditImageView.eraserAction() = eraserAction(EraserAction())

/**
 * 橡皮擦
 * [EraserAction]
 */
fun EditImageView.eraserAction(editImageAction: EraserAction) = action(editImageAction).apply { editType = EditType.ACTION }
