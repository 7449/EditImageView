@file:Suppress("FunctionName")

package com.image.edit.x

import android.graphics.PointF
import com.davemorrissey.labs.subscaleview.api.getSupportMatrix
import com.davemorrissey.labs.subscaleview.api.viewToSourceCoord
import com.image.edit.EditImageView
import com.image.edit.action.OnEditImageAction
import com.image.edit.simple.*
import com.image.edit.simple.text.EditImageText
import com.image.edit.simple.text.EditTextType
import com.image.edit.simple.text.SimpleOnEditImageTextAction
import com.image.edit.type.EditType

/**
 * @author y
 * @create 2019/3/18
 */

/**
 * 退出绘制模式
 */
fun EditImageView.noneAction() = apply { editType = EditType.NONE }

/**
 * 圆形
 * [SimpleOnEditImageCircleAction]
 */
fun EditImageView.circleAction() = circleAction(SimpleOnEditImageCircleAction())

/**
 * 圆形
 * [SimpleOnEditImageCircleAction]
 */
fun EditImageView.circleAction(editImageAction: SimpleOnEditImageCircleAction) = action(editImageAction).apply { editType = EditType.ACTION }

/**
 * 直线
 * [SimpleOnEditImageLineAction]
 */
fun EditImageView.lineAction() = lineAction(SimpleOnEditImageLineAction())

/**
 * 直线
 * [SimpleOnEditImageLineAction]
 */
fun EditImageView.lineAction(editImageAction: SimpleOnEditImageLineAction) = action(editImageAction).apply { editType = EditType.ACTION }

/**
 * 普通画笔
 * [SimpleOnEditImagePointAction]
 */
fun EditImageView.pointAction() = pointAction(SimpleOnEditImagePointAction())

/**
 * 普通画笔
 * [SimpleOnEditImagePointAction]
 */
fun EditImageView.pointAction(editImageAction: SimpleOnEditImagePointAction) = action(editImageAction).apply { editType = EditType.ACTION }

/**
 * 方形
 * [SimpleOnEditImageRectAction]
 */
fun EditImageView.rectAction() = rectAction(SimpleOnEditImageRectAction())

/**
 * 方形
 * [SimpleOnEditImageRectAction]
 */
fun EditImageView.rectAction(editImageAction: SimpleOnEditImageRectAction) = action(editImageAction).apply { editType = EditType.ACTION }

/**
 * 橡皮擦
 * [SimpleOnEditImageEraserAction]
 */
fun EditImageView.eraserAction() = eraserAction(SimpleOnEditImageEraserAction())

/**
 * 橡皮擦
 * [SimpleOnEditImageEraserAction]
 */
fun EditImageView.eraserAction(editImageAction: SimpleOnEditImageEraserAction) = action(editImageAction).apply { editType = EditType.ACTION }

/**
 * 文字
 * [SimpleOnEditImageTextAction]
 */
fun EditImageView.textAction(text: String) = textAction(text, SimpleOnEditImageTextAction())

/**
 * 文字
 * [SimpleOnEditImageTextAction]
 */
fun EditImageView.textAction(imageText: EditImageText) = textAction(imageText, SimpleOnEditImageTextAction())

/**
 * 文字
 * [SimpleOnEditImageTextAction]
 */
fun EditImageView.textAction(text: String, editImageAction: SimpleOnEditImageTextAction) = run {
    val pointF = PointF((resources.displayMetrics.widthPixels / 2).toFloat(), (resources.displayMetrics.widthPixels / 2).toFloat())
    val editImageText = EditImageText(viewToSourceCoord(pointF, pointF)
            ?: pointF, 1f, 0f, text, editImageAction.textPaint.color, editImageAction.textPaint.textSize)
    textAction(editImageText, editImageAction)
}

/**
 * 文字
 * [SimpleOnEditImageTextAction]
 */
fun EditImageView.textAction(imageText: EditImageText, editImageAction: SimpleOnEditImageTextAction) = action(editImageAction).apply { editImageText = imageText }.apply { editType = EditType.ACTION }.apply { editTextType = EditTextType.MOVE }

/**
 * 文字状态
 * [EditTextType]
 */
fun EditImageView.hasTextAction() = editTextType != EditTextType.NONE

/**
 * 保存文字
 * [SimpleOnEditImageTextAction]
 */
fun EditImageView.saveText() = getSupportMatrix()?.let { onEditImageAction?.onSaveImageCache(this) }

/**
 * 自定义行为
 * [OnEditImageAction]
 */
fun <T : OnEditImageAction> EditImageView.customAction(editImageAction: T) = action(editImageAction).apply { editType = EditType.ACTION }

internal fun <T : OnEditImageAction> EditImageView.action(editImageAction: T) = run {
    onEditImageAction = editImageAction
    editImageAction
}
