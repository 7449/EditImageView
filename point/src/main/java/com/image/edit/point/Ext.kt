package com.image.edit.point

import com.image.edit.EditImageView
import com.image.edit.EditType
import com.image.edit.action

/**
 * 普通画笔
 * [PointAction]
 */
fun EditImageView.pointAction() = pointAction(PointAction())

/**
 * 普通画笔
 * [PointAction]
 */
fun EditImageView.pointAction(editImageAction: PointAction) = action(editImageAction).apply { editType = EditType.ACTION }