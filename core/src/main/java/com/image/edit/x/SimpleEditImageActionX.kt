@file:Suppress("FunctionName")

package com.image.edit.x

import com.image.edit.EditImageView
import com.image.edit.action.OnEditImageAction
import com.image.edit.type.EditType

/**
 * 退出绘制模式
 */
fun EditImageView.noneAction() = apply { editType = EditType.NONE }

/**
 * 自定义行为
 * [OnEditImageAction]
 */
fun <T : OnEditImageAction> EditImageView.customAction(editImageAction: T) = action(editImageAction).apply { editType = EditType.ACTION }

fun <T : OnEditImageAction> EditImageView.action(editImageAction: T) = run {
    onEditImageAction = editImageAction
    editImageAction
}
