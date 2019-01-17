package com.image.edit

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.image.edit.action.OnEditImageCustomActionListener
import com.image.edit.action.OnEditImageEraserActionListener
import com.image.edit.action.OnEditImagePointActionListener
import com.image.edit.action.OnEditImageTextActionListener
import com.image.edit.cache.EditImageCache
import com.image.edit.cache.EditImageText
import com.image.edit.simple.SimpleOnEditImageEraserActionListener
import com.image.edit.simple.SimpleOnEditImageListener
import com.image.edit.simple.SimpleOnEditImagePointActionListener
import com.image.edit.simple.SimpleOnEditImageTextActionListener
import com.image.edit.x.supportRecycle
import java.util.*

/**
 * @author y
 * @create 2018/11/17
 */
class EditImageView : SubsamplingScaleImageView {

    var cacheArrayList = LinkedList<EditImageCache>()

    var editTextType = EditTextType.NONE
    var onEditImageListener: OnEditImageListener = SimpleOnEditImageListener()

    var onEditImagePointActionListener: OnEditImagePointActionListener = SimpleOnEditImagePointActionListener()
    var onEditImageEraserActionListener: OnEditImageEraserActionListener = SimpleOnEditImageEraserActionListener()
    var onEditImageTextActionListener: OnEditImageTextActionListener = SimpleOnEditImageTextActionListener()

    var onEditImageCustomActionListener: OnEditImageCustomActionListener? = null

    var editImageConfig: EditImageConfig = EditImageConfig()
        set(value) {
            field = value
            refreshConfig()
        }

    var editType = EditType.NONE
        set(value) {
            if (value != EditType.NONE && cacheArrayList.size >= editImageConfig.maxCacheCount) {
                onEditImageListener.onLastCacheMax()
                return
            }
            field = value
            refresh()
        }

    lateinit var editImageText: EditImageText

    lateinit var pointPaint: Paint
    lateinit var eraserPaint: Paint

    lateinit var textPaint: TextPaint
    lateinit var framePaint: Paint

    var newBitmap: Bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    lateinit var newBitmapCanvas: Canvas

    constructor(context: Context, attr: AttributeSet) : super(context, attr)

    constructor(context: Context) : super(context)

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (editType == EditType.NONE || !isReady) {
            return super.onTouchEvent(event)
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> eventDown(event.x, event.y)
            MotionEvent.ACTION_MOVE -> eventMove(event.x, event.y)
            MotionEvent.ACTION_UP -> eventUp(event.x, event.y)
        }
        return if (editType != EditType.TEXT || editTextType != EditTextType.NONE) {
            true
        } else super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!isReady) {
            return
        }
        supperMatrix?.let {
            canvas.drawBitmap(newBitmap, it, null)
            when (editType) {
                EditType.PAINT -> onEditImagePointActionListener.onDraw(this, canvas)
                EditType.ERASER -> onEditImageEraserActionListener.onDraw(this, canvas)
                EditType.TEXT -> onEditImageTextActionListener.onDraw(this, canvas)
                EditType.CUSTOM -> onEditImageCustomActionListener?.onDraw(this, canvas)
                else -> {
                }
            }
        }
    }

    private fun eventDown(x: Float, y: Float) {
        when (editType) {
            EditType.PAINT -> onEditImagePointActionListener.onDown(this, x, y)
            EditType.ERASER -> onEditImageEraserActionListener.onDown(this, x, y)
            EditType.TEXT -> onEditImageTextActionListener.onDown(this, x, y)
            EditType.CUSTOM -> onEditImageCustomActionListener?.onDown(this, x, y)
            else -> {
            }
        }
    }

    private fun eventMove(x: Float, y: Float) {
        when (editType) {
            EditType.PAINT -> onEditImagePointActionListener.onMove(this, x, y)
            EditType.ERASER -> onEditImageEraserActionListener.onMove(this, x, y)
            EditType.TEXT -> onEditImageTextActionListener.onMove(this, x, y)
            EditType.CUSTOM -> onEditImageCustomActionListener?.onMove(this, x, y)
            else -> {
            }
        }
    }

    private fun eventUp(x: Float, y: Float) {
        when (editType) {
            EditType.PAINT -> onEditImagePointActionListener.onUp(this, x, y)
            EditType.ERASER -> onEditImageEraserActionListener.onUp(this, x, y)
            EditType.TEXT -> onEditImageTextActionListener.onUp(this, x, y)
            EditType.CUSTOM -> onEditImageCustomActionListener?.onUp(this, x, y)
            else -> {
            }
        }
    }

    fun setOnEditImageInitializeListener(onEditImageInitializeListener: OnEditImageInitializeListener) {
        this.pointPaint = onEditImageInitializeListener.initPointPaint(this)
        this.eraserPaint = onEditImageInitializeListener.initEraserPaint(this)
        this.textPaint = onEditImageInitializeListener.initTextPaint(this)
        this.framePaint = onEditImageInitializeListener.initTextFramePaint(this)
        refreshConfig()
    }

    fun refresh() {
        invalidate()
    }

    fun setText(text: String) {
        val widthPixels = resources.displayMetrics.widthPixels
        val pointF = PointF((widthPixels / 2).toFloat(), (widthPixels / 2).toFloat())
        editImageText = EditImageText(viewToSourceCoord(pointF, pointF)
                ?: pointF, 1f, 0f, text, textPaint.color, textPaint.textSize)
    }

    fun saveText() {
        if (editType == EditType.TEXT && supperMatrix != null) {
            onEditImageTextActionListener.onSaveImageCache(this)
        }
    }

    fun recycleDrawBitmap() {
        newBitmap.supportRecycle()
    }

    fun getNewCanvasBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(sWidth, sHeight, Bitmap.Config.ARGB_8888)
        val defaultBitmap = getBitmap()
        val canvas = Canvas(bitmap)
        if (defaultBitmap != null) {
            canvas.drawBitmap(defaultBitmap, 0f, 0f, null)
        }
        canvas.drawBitmap(newBitmap, 0f, 0f, null)
        canvas.save()
        return bitmap
    }

    fun clearImage() {
        if (cacheArrayList.isEmpty()) {
            onEditImageListener.onLastImageEmpty()
            return
        }
        for (editImageCache in cacheArrayList) {
            editImageCache.reset()
        }
        cacheArrayList.clear()
        reset()
        refreshConfig()
        editType = EditType.NONE
    }

    fun lastImage() {
        if (cacheArrayList.isEmpty()) {
            onEditImageListener.onLastImageEmpty()
            return
        }
        cacheArrayList.removeLast().reset()
        reset()
        for (editImageCache in cacheArrayList) {
            editImageCache.onEditImageBaseActionListener?.onLastImageCache(this, editImageCache)
        }
        refreshConfig()
        editType = EditType.NONE
    }

    override fun onReady() {
        reset()
    }

    open fun reset() {
        recycleDrawBitmap()
        newBitmap = Bitmap.createBitmap(sWidth, sHeight, Bitmap.Config.ARGB_8888)
        newBitmapCanvas = Canvas(newBitmap)
    }

    private fun refreshConfig() {
        pointPaint.color = editImageConfig.pointColor
        pointPaint.strokeWidth = editImageConfig.pointWidth
        eraserPaint.strokeWidth = editImageConfig.eraserPointWidth
        textPaint.textAlign = editImageConfig.textPaintAlign
        textPaint.textSize = editImageConfig.textPaintSize
        textPaint.color = editImageConfig.textPaintColor
        framePaint.strokeWidth = editImageConfig.textFramePaintWidth
        framePaint.color = editImageConfig.textFramePaintColor
    }
}
