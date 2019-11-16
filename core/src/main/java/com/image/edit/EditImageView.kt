package com.image.edit

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.davemorrissey.labs.subscaleview.api.getSupportMatrix
import com.davemorrissey.labs.subscaleview.api.isReady
import com.image.edit.cache.CacheCallback
import com.image.edit.cache.EditImageCache
import com.image.edit.config.EditImageConfig
import com.image.edit.listener.OnEditImageListener
import java.util.*

/**
 * @author y
 * @create 2018/11/17
 */
open class EditImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : SubsamplingScaleImageView(context, attrs) {

    val newBitmapCanvas: Canvas = Canvas()
    var newBitmap: Bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    var editImageConfig: EditImageConfig = EditImageConfig()

    var cacheArrayList: LinkedList<EditImageCache<CacheCallback>> = LinkedList()
    var onEditImageAction: OnEditImageAction<out CacheCallback>? = null
    var onEditImageListener: OnEditImageListener? = null

    var editType = EditType.NONE
        set(value) {
            if (value != EditType.NONE && cacheArrayList.size >= editImageConfig.maxCacheCount) {
                onEditImageListener?.onLastCacheMax()
                return
            }
            field = value
            invalidate()
        }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val onTouchEvent = onEditImageAction?.onTouchEvent(this, event) ?: false
        if (editType == EditType.NONE || !isReady() || !onTouchEvent) {
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
        if (!isReady() || getSupportMatrix() == null) {
            return
        }
        getSupportMatrix()?.let { canvas.drawBitmap(newBitmap, it, null) }
        if (editType == EditType.NONE) return
        onEditImageAction?.onDraw(this, canvas)
    }

    override fun onReady() {
        reset()
    }

    fun reset() {
        recycleDrawBitmap()
        newBitmap = Bitmap.createBitmap(sWidth, sHeight, Bitmap.Config.ARGB_8888)
        newBitmapCanvas.setBitmap(newBitmap)
    }
}
