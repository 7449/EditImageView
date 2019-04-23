package com.image.edit

import android.graphics.Paint
import android.text.TextPaint

/**
 * @author y
 * @create 2018/11/20
 */
interface OnEditImageInitializeListener {
    /**
     * 初始化画笔
     *
     * @param editImageView [EditImageView]
     * @return [Paint]
     */
    fun initPointPaint(editImageView: EditImageView): Paint

    /**
     * 初始化橡皮擦
     *
     * @param editImageView [EditImageView]
     * @return [Paint]
     */
    fun initEraserPaint(editImageView: EditImageView): Paint

    /**
     * 初始化文字画笔
     *
     * @param editImageView [EditImageView]
     * @return [Paint]
     */
    fun initTextPaint(editImageView: EditImageView): TextPaint

    /**
     * 初始化文字框画笔
     *
     * @param editImageView [EditImageView]
     * @return [Paint]
     */
    fun initTextFramePaint(editImageView: EditImageView): Paint
}
