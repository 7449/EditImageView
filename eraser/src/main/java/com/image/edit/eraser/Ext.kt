package com.image.edit.eraser

import com.image.edit.OnEditImageCallback
import com.image.edit.action

fun OnEditImageCallback.eraserAction(pointWidth: Float = 25f, isSave: Boolean = true) = eraserAction(EraserAction(pointWidth, isSave))

fun OnEditImageCallback.eraserAction(editImageAction: EraserAction) = action(editImageAction)

fun OnEditImageCallback.getEraserAction() = onEditImageAction as? EraserAction

fun EraserAction.setSave(save: Boolean) = also { isSave = save }

fun EraserAction.setPointWidth(width: Float) = also { pointWidth = width }