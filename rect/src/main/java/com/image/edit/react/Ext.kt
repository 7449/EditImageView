package com.image.edit.react

import android.graphics.Color
import com.image.edit.EditImageView
import com.image.edit.EditType
import com.image.edit.action

fun EditImageView.rectAction(pointColor: Int = Color.RED, pointWidth: Float = 20f) = rectAction(RectAction(pointColor, pointWidth))

fun EditImageView.rectAction(editImageAction: RectAction) = action(editImageAction).apply { editType = EditType.ACTION }

fun EditImageView.getRectAction() = onEditImageAction as? RectAction

fun RectAction.setPointColor(color: Int) = also { pointColor = color }

fun RectAction.setPointWidth(width: Float) = also { pointWidth = width }