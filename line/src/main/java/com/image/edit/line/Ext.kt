package com.image.edit.line

import android.graphics.Color
import com.image.edit.EditImageView
import com.image.edit.EditType
import com.image.edit.action

fun EditImageView.lineAction(pointColor: Int = Color.RED, pointWidth: Float = 20f) = lineAction(LineAction(pointColor, pointWidth))

fun EditImageView.lineAction(editImageAction: LineAction) = action(editImageAction).apply { editType = EditType.ACTION }

fun EditImageView.getLineAction() = onEditImageAction as? LineAction

fun LineAction.setPointColor(color: Int) = also { pointColor = color }

fun LineAction.setPointWidth(width: Float) = also { pointWidth = width }