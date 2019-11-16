package com.image.edit.point

import android.graphics.Color
import com.image.edit.EditImageView
import com.image.edit.EditType
import com.image.edit.action

fun EditImageView.pointAction(pointColor: Int = Color.RED, pointWidth: Float = 20f) = pointAction(PointAction(pointColor, pointWidth))

fun EditImageView.pointAction(editImageAction: PointAction) = action(editImageAction).apply { editType = EditType.ACTION }

fun EditImageView.getPointAction() = onEditImageAction as? PointAction

fun PointAction.setPointColor(color: Int) = also { pointColor = color }

fun PointAction.setPointWidth(width: Float) = also { pointWidth = width }