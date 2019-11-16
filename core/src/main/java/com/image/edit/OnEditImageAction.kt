package com.image.edit

import android.graphics.Canvas
import android.view.MotionEvent
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView

/**
 * @author y
 * @create 2018/11/20
 */
interface OnEditImageAction<CACHE : CacheCallback> {
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
     * @param x              [MotionEvent.getX]
     * @param y              [MotionEvent.getY]
     */
    fun onDown(editImageView: EditImageView, x: Float, y: Float)

    /**
     * 移动
     *
     * @param editImageView [EditImageView]
     * @param x             [MotionEvent.getX]
     * @param y             [MotionEvent.getY]
     */
    fun onMove(editImageView: EditImageView, x: Float, y: Float)

    /**
     * 抬起
     *
     * @param editImageView [EditImageView]
     * @param x             [MotionEvent.getX]
     * @param y             [MotionEvent.getY]
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
    fun onLastImageCache(editImageView: EditImageView, editImageCache: EditImageCache<CACHE>)

    /**
     * 拦截滑动,文字可处理冲突
     *
     *  @param editImageView  [EditImageView]
     *  @param touchEvent [MotionEvent]
     *  @return false 滑动交由[SubsamplingScaleImageView]处理,默认为true
     */
    fun onTouchEvent(editImageView: EditImageView, touchEvent: MotionEvent): Boolean = true
}

