package com.image.edit.impl

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.image.edit.EditImageCache
import com.image.edit.EditType
import com.image.edit.OnEditImageAction
import com.image.edit.OnEditImageListener
import com.image.edit.virtual.OnEditImageCallback
import java.util.*


/**
 * @author y
 * @create 2018/11/17
 */
class EditSubsamplingScaleImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null)
    : SubsamplingScaleImageView(context, attrs), OnEditImageCallback {

    var defaultIntelligent = false
    var defaultMaxCacheCount = 5
    var editType = EditType.NONE
    var defaultEditImageListener: OnEditImageListener? = null
    var defaultEditImageAction: OnEditImageAction? = null
    private val cacheList = LinkedList<EditImageCache>()
    private val newCanvas = Canvas()
    private var newBitmap: Bitmap? = null

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
        newBitmap?.let { bitmap ->
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
            newBitmap = Bitmap.createBitmap(sWidth, sHeight, Bitmap.Config.ARGB_8888)
            newCanvas.setBitmap(newBitmap)
        }
    }

    override val viewContext: Context
        get() = context

    override val viewScale: Float
        get() = scale

    override val viewBitmap: Bitmap?
        get() = privateBitmap

    override val supportCanvas: Canvas?
        get() = newCanvas

    override val intelligent: Boolean
        get() = defaultIntelligent

    override val bitmapHeightAndHeight: Point
        get() = Point(sWidth, sHeight)

    override val isMaxCacheCount: Boolean
        get() = cacheList.size >= defaultMaxCacheCount

    override val isCacheEmpty: Boolean
        get() = cacheList.isEmpty()

    override val viewEditType: EditType
        get() = editType

    override val onEditImageListener: OnEditImageListener?
        get() = defaultEditImageListener

    override val onEditImageAction: OnEditImageAction?
        get() = defaultEditImageAction

    override val obj1: Any?
        get() = state

    override fun updateAction(action: OnEditImageAction) {
        this.defaultEditImageAction = action
    }

    override fun noneAction(): OnEditImageCallback {
        editType = EditType.NONE
        onInvalidate()
        return this
    }

    override fun editTypeAction(): OnEditImageCallback {
        editType = EditType.ACTION
        onInvalidate()
        return this
    }

    override fun onInvalidate() {
        invalidate()
    }

    override fun onCanvasBitmap(canvas: Canvas) {
        cacheList.forEach { it.onEditImageAction.onDrawBitmap(this, canvas, it) }
    }

    override fun onAddCache(cache: EditImageCache) {
        cacheList.add(cache)
        if (isMaxCacheCount) {
            noneAction()
            onEditImageListener?.onLastCacheMax()
        }
    }

    override fun removeAllCache() {
        cacheList.clear()
    }

    override fun removeLastCache(): EditImageCache {
        return cacheList.removeLast()
    }

    override fun onSourceToViewCoord(source: PointF, target: PointF) {
        sourceToViewCoord(source, target)
    }

    override fun onViewToSourceCoord(source: PointF): PointF {
        return viewToSourceCoord(source) ?: throw KotlinNullPointerException("PointF == null")
    }

    override fun onViewToSourceCoord(source: PointF, target: PointF) {
        viewToSourceCoord(source, target)
    }

    private val privateMatrix: Matrix?
        get() = findPrivateField<Matrix>("matrix")

    private val privateBitmap: Bitmap?
        get() = findPrivateField<Bitmap>("bitmap")

    private inline fun <reified T> findPrivateField(name: String): T? {
        return runCatching {
            val declaredField = SubsamplingScaleImageView::class.java.getDeclaredField(name)
            declaredField.isAccessible = true
            declaredField.get(this) as T?
        }.getOrNull()
    }
}
