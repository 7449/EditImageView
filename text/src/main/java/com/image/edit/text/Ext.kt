package com.image.edit.text

import android.graphics.PointF
import com.davemorrissey.labs.subscaleview.api.getSupportMatrix
import com.davemorrissey.labs.subscaleview.api.viewToSourceCoord
import com.image.edit.EditImageView
import com.image.edit.type.EditType
import com.image.edit.x.action

/**
 * 文字
 * [TextAction]
 */
fun EditImageView.textAction(text: String) = textAction(text, TextAction())

/**
 * 文字
 * [TextAction]
 */
fun EditImageView.textAction(imageText: EditImageText) = textAction(imageText, TextAction())

/**
 * 文字
 * [TextAction]
 */
fun EditImageView.textAction(text: String, editImageAction: TextAction) = run {
    val pointF = PointF((resources.displayMetrics.widthPixels / 2).toFloat(), (resources.displayMetrics.widthPixels / 2).toFloat())
    val editImageText = EditImageText(viewToSourceCoord(pointF, pointF)
            ?: pointF, 1f, 0f, text, editImageAction.textPaint.color, editImageAction.textPaint.textSize)
    textAction(editImageText, editImageAction)
}

/**
 * 文字
 * [TextAction]
 */
fun EditImageView.textAction(imageText: EditImageText, editImageAction: TextAction) = action(editImageAction).apply { editImageText = imageText }.apply { editType = EditType.ACTION }.apply { editTextType = EditTextType.MOVE }

/**
 * 文字状态
 * [EditTextType]
 */
fun EditImageView.hasTextAction(): Boolean {
    if (onEditImageAction is TextAction) {
        return (onEditImageAction as TextAction).editTextType != EditTextType.NONE
    }
    return false
}

/**
 * 保存文字
 * [TextAction]
 */
fun EditImageView.saveText() = getSupportMatrix()?.let { onEditImageAction?.onSaveImageCache(this) }