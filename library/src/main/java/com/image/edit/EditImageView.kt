@file:Suppress("MemberVisibilityCanBePrivate", "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.image.edit

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.image.edit.action.OnEditImageAction
import com.image.edit.cache.EditImageCache
import com.image.edit.config.EditImageConfig
import com.image.edit.simple.text.EditTextType
import com.image.edit.type.EditType
import com.image.edit.x.reset
import java.util.*

/**
 * @author y
 * @create 2018/11/17
 */
class EditImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : SubsamplingScaleImageView(context, attrs) {

    var newBitmap: Bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    lateinit var newBitmapCanvas: Canvas

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
        if (!isReady || supperMatrix == null) {
            return
        }
        canvas.drawBitmap(newBitmap, supperMatrix, null)
        if (editType == EditType.NONE) return
        onEditImageAction?.onDraw(this, canvas)
    }

    override fun onReady() {
        reset()
    }

    var editImageConfig: EditImageConfig = EditImageConfig()

    var editType = EditType.NONE
        set(value) {
            if (value != EditType.NONE && cacheArrayList.size >= editImageConfig.maxCacheCount) {
                onEditImageListener?.onLastCacheMax()
                return
            }
            field = value
            invalidate()
        }

    var editTextType = EditTextType.NONE

    var cacheArrayList: LinkedList<EditImageCache> = LinkedList()
    var onEditImageListener: OnEditImageListener? = null
    var onEditImageAction: OnEditImageAction? = null
}
