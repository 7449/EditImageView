package com.image.edit

import android.content.Context
import android.graphics.Canvas
import android.graphics.Point
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import java.util.*

/**
 * @author y
 * @create 2018/11/17
 */
class EditImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : SubsamplingScaleImageView(context, attrs) {

    val cacheArrayList: LinkedList<EditImageCache<CacheCallback>> = LinkedList()
    var onEditImageAction: OnEditImageAction<CacheCallback>? = null
    var onEditImageListener: OnEditImageListener? = null
    var maxCacheCount = 1000

    var editType = EditType.NONE
        set(value) {
            if (value != EditType.NONE && isMaxCount) {
                onEditImageListener?.onLastCacheMax()
                return
            }
            field = value
            invalidate()
        }

    val isMaxCount
        get() = cacheArrayList.size >= maxCacheCount

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val onTouchEvent = onEditImageAction?.onTouchEvent(this, event) ?: false
        if (editType == EditType.NONE || !isReady || !onTouchEvent) {
            return super.onTouchEvent(event)
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> onEditImageAction?.onDown(this, event.x, event.y)
            MotionEvent.ACTION_MOVE -> onEditImageAction?.onMove(this, event.x, event.y)
            MotionEvent.ACTION_UP -> onEditImageAction?.onUp(this, event.x, event.y)
        }
        return onTouchEvent
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!isReady) {
            return
        }
        Log.e("onDraw", canvas.maximumBitmapHeight.toString() + "  " + canvas.maximumBitmapWidth)
        cacheArrayList.forEach { it.onEditImageAction.onDrawCache(this, canvas, it) }
        onEditImageAction?.onDraw(this, canvas)
    }

    /**
     * 计算一下,如果bitmap的尺寸小于该尺寸,则开启橡皮擦
     */
    private fun getMaxBitmapDimensions(canvas: Canvas): Point? {
        return Point(canvas.maximumBitmapWidth, canvas.maximumBitmapHeight)
    }

    fun clearImage() {
        if (cacheArrayList.isEmpty()) {
            onEditImageListener?.onLastImageEmpty()
            return
        }
        cacheArrayList.clear()
        editType = EditType.NONE
    }

    fun lastImage() {
        if (cacheArrayList.isEmpty()) {
            onEditImageListener?.onLastImageEmpty()
            return
        }
        cacheArrayList.removeLast()
        editType = EditType.NONE
    }
}
