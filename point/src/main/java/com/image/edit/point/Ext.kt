package com.image.edit.point

import android.graphics.Color
import com.image.edit.OnEditImageCallback
import com.image.edit.action

fun OnEditImageCallback.pointAction(pointColor: Int = Color.RED, pointWidth: Float = 20f) = pointAction(PointAction(pointColor, pointWidth))

fun OnEditImageCallback.pointAction(editImageAction: PointAction) = action(editImageAction)

fun OnEditImageCallback.getPointAction() = onEditImageAction as? PointAction

fun PointAction.setPointColor(color: Int) = also { pointColor = color }

fun PointAction.setPointWidth(width: Float) = also { pointWidth = width }