package com.image.edit.impl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Point
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.image.edit.*
import java.util.*


/**
 * @author y
 * @create 2018/11/17
 */
class EditSubsamplingScaleImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null)
    : SubsamplingScaleImageView(context, attrs), OnEditImageCallback {

    private val cacheList = LinkedList<EditImageCache>()
    private var defaultMaxCacheCount = 1000
    private var defaultIntelligent = false
    private var editType = EditType.NONE
    private var defaultEditImageListener: OnEditImageListener? = null
    private var defaultEditImageAction: OnEditImageAction? = null
    private val newCanvas = Canvas()
    private var bitmap: Bitmap? = null

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val onTouchEvent = onEditImageAction?.onTouchEvent(this, event) ?: false
        if (viewEditType == EditType.NONE || !isReady || !onTouchEvent) {
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
        bitmap?.let { bitmap ->
            privateMatrix?.let { matrix ->
                canvas.drawBitmap(bitmap, matrix, null)
            }
        }
        cacheList.forEach { it.onEditImageAction.onDrawCache(this, canvas, it) }
        onEditImageAction?.onDraw(this, canvas)
    }

    override fun onReady() {
        if (!intelligent) {
            return
        }
        privateBitmap?.let {
            bitmap = Bitmap.createBitmap(sWidth, sHeight, Bitmap.Config.ARGB_8888)
            newCanvas.setBitmap(bitmap)
        }
    }

    override val viewContext: Context
        get() = context

    override val viewScale: Float
        get() = scale

    override val bitmapHeightAndHeight: Point
        get() = Point(sWidth, sHeight)

    override val drawBitmap: Boolean
        get() = bitmap != null

    override var intelligent: Boolean
        get() = defaultIntelligent
        set(value) {
            defaultIntelligent = value
        }

    override val supportCanvas: Canvas?
        get() = newCanvas

    override val supportBitmap: Bitmap?
        get() = privateBitmap

    override var viewEditType: EditType
        get() = editType
        set(value) {
            editType = value
            invalidate()
        }

    override var maxCacheCount: Int
        get() = defaultMaxCacheCount
        set(value) {
            defaultMaxCacheCount = value
        }

    override var onEditImageAction: OnEditImageAction?
        get() = defaultEditImageAction
        set(value) {
            defaultEditImageAction = value
        }

    override var onEditImageListener: OnEditImageListener?
        get() = defaultEditImageListener
        set(value) {
            defaultEditImageListener = value
        }

    override val isMaxCacheCount: Boolean
        get() = cacheList.size >= maxCacheCount

    override val isCacheEmpty: Boolean
        get() = cacheList.isEmpty()

    override val obj1: Any?
        get() = state

    override fun onInvalidate() {
        invalidate()
    }

    override fun onCanvasBitmap(canvas: Canvas) {
        cacheList.forEach { it.onEditImageAction.onDrawBitmap(this, canvas, it) }
    }

    override fun onSourceToViewCoord(x: Float, y: Float, target: PointF) {
        sourceToViewCoord(x, y, target)
    }

    override fun onSourceToViewCoord(source: PointF): PointF {
        return sourceToViewCoord(source) ?: throw KotlinNullPointerException("PointF == null")
    }

    override fun onSourceToViewCoord(source: PointF, target: PointF) {
        sourceToViewCoord(source, target)
    }

    override fun onSourceToViewCoord(x: Float, y: Float): PointF {
        return sourceToViewCoord(x, y) ?: throw KotlinNullPointerException("PointF == null")
    }

    override fun onViewToSourceCoord(x: Float, y: Float, target: PointF) {
        viewToSourceCoord(x, y, target)
    }

    override fun onViewToSourceCoord(source: PointF): PointF {
        return viewToSourceCoord(source) ?: throw KotlinNullPointerException("PointF == null")
    }

    override fun onViewToSourceCoord(x: Float, y: Float): PointF {
        return viewToSourceCoord(x, y) ?: throw KotlinNullPointerException("PointF == null")
    }

    override fun onViewToSourceCoord(source: PointF, target: PointF) {
        viewToSourceCoord(source, target)
    }

    override fun onAddCacheAndCheck(cache: EditImageCache) {
        cacheList.add(cache)
        if (isMaxCacheCount) {
            noneAction()
            onEditImageListener?.onLastCacheMax()
        }
    }

    override fun onAddCacheAnd(cache: EditImageCache) {
        cacheList.add(cache)
    }

    override fun removeAllCache() {
        cacheList.clear()
    }

    override fun removeLastCache(): EditImageCache? {
        return cacheList.removeLast()
    }
}
