package com.image.edit.line

import android.graphics.Color
import com.image.edit.virtual.OnEditImageCallback

fun OnEditImageCallback.lineAction(pointColor: Int = Color.RED, pointWidth: Float = 20f) = lineAction(LineAction(pointColor, pointWidth))

fun OnEditImageCallback.lineAction(editImageAction: LineAction) = action(editImageAction)

fun OnEditImageCallback.getLineAction() = onEditImageAction as? LineAction

fun LineAction.setPointColor(color: Int) = also { pointColor = color }

fun LineAction.setPointWidth(width: Float) = also { pointWidth = width }