package com.image.edit.eraser

import com.image.edit.OnEditImageCallback

fun OnEditImageCallback.eraserAction(pointWidth: Float = 20f) = eraserAction(EraserAction(pointWidth))

fun OnEditImageCallback.eraserAction(editImageAction: EraserAction) = action(editImageAction)

fun OnEditImageCallback.getEraserAction() = onEditImageAction as? EraserAction

fun EraserAction.setPointWidth(width: Float) = also { pointWidth = width }