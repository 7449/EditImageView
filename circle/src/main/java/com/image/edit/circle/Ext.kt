package com.image.edit.circle

import com.image.edit.EditImageView
import com.image.edit.EditType
import com.image.edit.action

/**
 * 圆形
 * [CircleAction]
 */
fun EditImageView.circleAction() = circleAction(CircleAction())

/**
 * 圆形
 * [CircleAction]
 */
fun EditImageView.circleAction(editImageAction: CircleAction) = action(editImageAction).apply { editType = EditType.ACTION }