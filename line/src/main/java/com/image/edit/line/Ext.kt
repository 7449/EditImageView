package com.image.edit.line

import com.image.edit.EditImageView
import com.image.edit.EditType
import com.image.edit.action

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
