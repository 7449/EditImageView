package com.image.edit.react

import com.image.edit.EditImageView
import com.image.edit.EditType
import com.image.edit.action

/**
 * 方形
 * [RectAction]
 */
fun EditImageView.rectAction() = rectAction(RectAction())

/**
 * 方形
 * [RectAction]
 */
fun EditImageView.rectAction(editImageAction: RectAction) = action(editImageAction).apply { editType = EditType.ACTION }
