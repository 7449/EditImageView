package com.image.edit.action

import android.graphics.Canvas
import android.graphics.PointF

import com.image.edit.EditImageView
import com.image.edit.cache.EditImageCache
import com.image.edit.cache.EditImageText

/**
 * @author y
 * @create 2018/11/20
 */

const val DEFAULT_X = -11F
const val DEFAULT_Y = -11F

fun initPointF(): PointF = PointF(DEFAULT_X, DEFAULT_Y)

interface OnEditImageTextActionListener : OnEditImageBaseActionListener {
    fun onDrawText(editImageView: EditImageView, editImageText: EditImageText, canvas: Canvas)
}

interface OnEditImageEraserActionListener : OnEditImageBaseActionListener

interface OnEditImagePointActionListener : OnEditImageBaseActionListener

interface OnEditImageCustomActionListener : OnEditImageBaseActionListener

interface OnEditImageBaseActionListener {

    /**
     * 绘制
     *
     * @param editImageView [EditImageView]
     * @param canvas        [Canvas]
     */
    fun onDraw(editImageView: EditImageView, canvas: Canvas)

    /**
     * 按下
     *
     * @param editImageView [EditImageView]
     * @param x             x
     * @param y             y
     */
    fun onDown(editImageView: EditImageView, x: Float, y: Float)

    /**
     * 移动
     *
     * @param editImageView [EditImageView]
     * @param x             x
     * @param y             y
     */
    fun onMove(editImageView: EditImageView, x: Float, y: Float)

    /**
     * 抬起
     *
     * @param editImageView [EditImageView]
     * @param x             x
     * @param y             y
     */
    fun onUp(editImageView: EditImageView, x: Float, y: Float)

    /**
     * 保存缓存
     *
     * @param editImageView [EditImageView]
     */
    fun onSaveImageCache(editImageView: EditImageView)

    /**
     * 回退到上一步
     *
     * @param editImageView  [EditImageView]
     * @param editImageCache [EditImageCache]
     */
    fun onLastImageCache(editImageView: EditImageView, editImageCache: EditImageCache)
}
