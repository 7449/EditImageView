package com.image.edit.line

import com.image.edit.EditImageView
import com.image.edit.type.EditType
import com.image.edit.x.action

/**
 * 直线
 * [LineAction]
 */
fun EditImageView.lineAction() = lineAction(LineAction())

/**
 * 直线
 * [LineAction]
 */
fun EditImageView.lineAction(editImageAction: LineAction) = action(editImageAction).apply { editType = EditType.ACTION }
