package com.image.edit

import android.graphics.Canvas
import android.view.MotionEvent
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView

/**
 * @author y
 * @create 2018/11/20
 */
interface OnEditImageAction<CACHE : CacheCallback> {

    companion object {
        const val INIT_X_Y = -10F
    }

    /**
     * 绘制
     *
     * @param editImageView [EditImageView]
     * @param canvas        [Canvas]
     */
    fun onDraw(editImageView: EditImageView, canvas: Canvas)

    /**
     * 绘制缓存
     *
     * @param editImageView [EditImageView]
     * @param canvas        [Canvas]
     * @param editImageCache        [EditImageCache]
     */
    fun onDrawCache(editImageView: EditImageView, canvas: Canvas, editImageCache: EditImageCache<CACHE>)

    /**
     * 绘制空白 New Bitmap
     *
     * @param editImageView  [EditImageView]
     * @param canvas        [Canvas]
     * @param editImageCache [EditImageCache]
     */
    fun onDrawBitmap(editImageView: EditImageView, canvas: Canvas, editImageCache: EditImageCache<CACHE>)

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
     * 是否绘制或添加缓存的判断
     */
    fun onNoDraw(): Boolean = true

    /**
     * 拦截滑动,文字可处理冲突
     *
     *  @param editImageView  [EditImageView]
     *  @param touchEvent [MotionEvent]
     *  @return false 滑动交由[SubsamplingScaleImageView]处理,默认为true
     */
    fun onTouchEvent(editImageView: EditImageView, touchEvent: MotionEvent): Boolean = true
}

