package com.image.edit.point

import com.image.edit.EditImageView
import com.image.edit.type.EditType
import com.image.edit.x.action

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