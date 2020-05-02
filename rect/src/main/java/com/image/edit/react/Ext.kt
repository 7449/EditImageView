package com.image.edit.react

import android.graphics.Color
import com.image.edit.OnEditImageCallback

fun OnEditImageCallback.rectAction(pointColor: Int = Color.RED, pointWidth: Float = 20f) = rectAction(RectAction(pointColor, pointWidth))

fun OnEditImageCallback.rectAction(editImageAction: RectAction) = action(editImageAction)

fun OnEditImageCallback.getRectAction() = onEditImageAction as? RectAction

fun RectAction.setPointColor(color: Int) = also { pointColor = color }

fun RectAction.setPointWidth(width: Float) = also { pointWidth = width }