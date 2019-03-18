@file:Suppress("MemberVisibilityCanBePrivate", "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.image.edit

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.image.edit.action.OnEditImageActionListener
import com.image.edit.cache.EditImageCache
import com.image.edit.simple.EditImageText
import java.util.*

/**
 * @author y
 * @create 2018/11/17
 */
class EditImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : SubsamplingScaleImageView(context, attrs), OnEditImageCallback {

    var cacheArrayList: LinkedList<EditImageCache> = LinkedList()

    var editTextType = EditTextType.NONE

    var onEditImageListener: OnEditImageListener? = null
    var onEditImageActionListener: OnEditImageActionListener? = null

    internal var onEditImageInitializeListener: OnEditImageInitializeListener? = null
        set(value) {
            value?.let {
                this.pointPaint = it.initPointPaint(this)
                this.eraserPaint = it.initEraserPaint(this)
                this.textPaint = it.initTextPaint(this)
                this.framePaint = it.initTextFramePaint(this)
                refreshConfig()
            }
            field = value
        }

    var editImageConfig: EditImageConfig = EditImageConfig()
        set(value) {
            field = value
            refreshConfig()
        }

    var editType = EditType.NONE
        set(value) {
            if (value != EditType.NONE && cacheArrayList.size >= editImageConfig.maxCacheCount) {
                onEditImageListener?.onLastCacheMax()
                return
            }
            field = value
            refresh()
        }

    lateinit var editImageText: EditImageText

    internal lateinit var pointPaint: Paint
    internal lateinit var eraserPaint: Paint

    internal lateinit var textPaint: TextPaint
    internal lateinit var framePaint: Paint

    var newBitmap: Bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    lateinit var newBitmapCanvas: Canvas

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val onTouchEvent = onEditImageActionListener?.onTouchEvent(this, event) ?: false
        if (editType == EditType.NONE || !isReady || !onTouchEvent) {
            return super.onTouchEvent(event)
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> onEditImageActionListener?.onDown(this, event.x, event.y)
            MotionEvent.ACTION_MOVE -> onEditImageActionListener?.onMove(this, event.x, event.y)
            MotionEvent.ACTION_UP -> onEditImageActionListener?.onUp(this, event.x, event.y)
        }
        return onTouchEvent
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!isReady || supperMatrix == null) {
            return
        }
        canvas.drawBitmap(newBitmap, supperMatrix, null)
        if (editType == EditType.NONE) return
        onEditImageActionListener?.onDraw(this, canvas)
    }

    override fun onReady() {
        reset()
    }
}
