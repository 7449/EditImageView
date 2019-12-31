package com.image.edit.circle

import android.graphics.Color
import com.image.edit.OnEditImageCallback
import com.image.edit.action

fun OnEditImageCallback.circleAction(pointColor: Int = Color.RED, pointWidth: Float = 20f) = circleAction(CircleAction(pointColor, pointWidth))

fun OnEditImageCallback.circleAction(editImageAction: CircleAction) = action(editImageAction)

fun OnEditImageCallback.getCircleAction() = onEditImageAction as? CircleAction

fun CircleAction.setPointColor(color: Int) = also { pointColor = color }

fun CircleAction.setPointWidth(width: Float) = also { pointWidth = width }