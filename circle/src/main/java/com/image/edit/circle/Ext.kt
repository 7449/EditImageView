package com.image.edit.circle

import android.graphics.Color
import com.image.edit.EditImageView
import com.image.edit.EditType
import com.image.edit.action

fun EditImageView.circleAction(pointColor: Int = Color.RED, pointWidth: Float = 20f) = circleAction(CircleAction(pointColor, pointWidth))

fun EditImageView.circleAction(editImageAction: CircleAction) = action(editImageAction).apply { editType = EditType.ACTION }

fun EditImageView.getCircleAction() = onEditImageAction as? CircleAction

fun CircleAction.setPointColor(color: Int) = also { pointColor = color }

fun CircleAction.setPointWidth(width: Float) = also { pointWidth = width }