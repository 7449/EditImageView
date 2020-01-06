package com.image.edit

import android.graphics.Canvas
import android.view.MotionEvent

/**
 * @author y
 * @create 2018/11/20
 */
interface OnEditImageAction : Cloneable {

    companion object {
        const val INIT_X_Y = -10F
    }

    /**
     * 绘制
     *
     * @param callback      [OnEditImageCallback]
     * @param canvas        [Canvas]
     */
    fun onDraw(callback: OnEditImageCallback, canvas: Canvas)

    /**
     * 绘制缓存
     *
     * @param callback              [OnEditImageCallback]
     * @param canvas                [Canvas]
     * @param editImageCache        [EditImageCache]
     */
    fun onDrawCache(callback: OnEditImageCallback, canvas: Canvas, editImageCache: EditImageCache)

    /**
     * 绘制空白 New Bitmap
     *
     * @param callback          [OnEditImageCallback]
     * @param canvas            [Canvas]
     * @param editImageCache    [EditImageCache]
     */
    fun onDrawBitmap(callback: OnEditImageCallback, canvas: Canvas, editImageCache: EditImageCache)

    /**
     * 按下
     *
     * @param callback          [OnEditImageCallback]
     * @param x                 [MotionEvent.getX]
     * @param y                 [MotionEvent.getY]
     */
    fun onDown(callback: OnEditImageCallback, x: Float, y: Float)

    /**
     * 移动
     *
     * @param callback      [OnEditImageCallback]
     * @param x             [MotionEvent.getX]
     * @param y             [MotionEvent.getY]
     */
    fun onMove(callback: OnEditImageCallback, x: Float, y: Float)

    /**
     * 抬起
     *
     * @param callback      [OnEditImageCallback]
     * @param x             [MotionEvent.getX]
     * @param y             [MotionEvent.getY]
     */
    fun onUp(callback: OnEditImageCallback, x: Float, y: Float)

    /**
     * 是否绘制或添加缓存的判断
     */
    fun onNoDraw(): Boolean = true

    /**
     * 复制一个完整的Action
     *
     * object#clone()为浅拷贝
     *
     */
//    @Deprecated("see clone()")
    fun copy(): OnEditImageAction

    /**
     * 拦截滑动
     *
     *  @param callback     [OnEditImageCallback]
     *  @param touchEvent   [MotionEvent]
     */
    fun onTouchEvent(callback: OnEditImageCallback, touchEvent: MotionEvent): Boolean = true

    @Throws(CloneNotSupportedException::class)
    public override fun clone(): OnEditImageAction {
        return super.clone() as OnEditImageAction
    }
}

