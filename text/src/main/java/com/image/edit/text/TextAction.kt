package com.image.edit.text

import android.graphics.*
import android.text.TextPaint
import android.text.TextUtils
import android.view.MotionEvent
import com.image.edit.EditImageCache
import com.image.edit.EditType
import com.image.edit.OnEditImageAction
import com.image.edit.virtual.OnEditImageCallback
import java.util.*
import kotlin.math.abs

/**
 * @author y
 * @create 2018/11/20
 */
class TextAction(
        var textPaintColor: Int = Color.RED,
        var isTextRotateMode: Boolean = true,
        var textPaintSize: Float = 60f,
        var textFramePaintColor: Int = Color.BLACK,
        var textFramePaintWidth: Float = 4f,
        var showTextMoveBox: Boolean = true,
        var textDeleteDrawableId: Int = R.drawable.ic_edit_image_delete,
        var textRotateDrawableId: Int = R.drawable.ic_edit_image_rotate,
) : OnEditImageAction {

    companion object {
        private const val STICKER_BTN_HALF_SIZE = 30
        private const val PADDING = 30
    }

    internal var saveText = false
    private val textPointF = PointF()
    private val cacheTextPointF = PointF()
    private val movePointF = PointF(0f, 0f)
    private val textRect = RectF()
    private val textTempRect = Rect()
    private val textDeleteRect = Rect()
    private val textRotateRect = Rect()
    private val textDeleteDstRect = RectF(0f, 0f, (STICKER_BTN_HALF_SIZE shl 1).toFloat(), (STICKER_BTN_HALF_SIZE shl 1).toFloat())
    private val textRotateDstRect = RectF(0f, 0f, (STICKER_BTN_HALF_SIZE shl 1).toFloat(), (STICKER_BTN_HALF_SIZE shl 1).toFloat())
    private val mMoveBoxRect = RectF()
    private val textContents = ArrayList<String>(2)
    private val framePaint: Paint = Paint()
    val textPaint: TextPaint = TextPaint(Paint.LINEAR_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG)
    var editTextType = EditTextType.MOVE
    private lateinit var textDeleteBitmap: Bitmap
    private lateinit var textRotateBitmap: Bitmap
    lateinit var editImageText: EditImageText

    init {
        framePaint.style = Paint.Style.STROKE
        framePaint.isAntiAlias = true
        textPaint.isAntiAlias = true
    }

    private fun createRect(callback: OnEditImageCallback) {
        textDeleteBitmap = BitmapFactory.decodeResource(callback.viewContext.resources, textDeleteDrawableId)
        textRotateBitmap = BitmapFactory.decodeResource(callback.viewContext.resources, textRotateDrawableId)
        textDeleteRect.set(0, 0, textDeleteBitmap.width, textDeleteBitmap.height)
        textRotateRect.set(0, 0, textRotateBitmap.width, textRotateBitmap.height)
    }

    override fun onDraw(callback: OnEditImageCallback, canvas: Canvas) {
        textPaint.textSize = textPaintSize
        textPaint.color = textPaintColor
        if (!::editImageText.isInitialized || TextUtils.isEmpty(editImageText.text) || editTextType == EditTextType.NONE) {
            return
        }
        val text = editImageText.text
        val scale = editImageText.scale
        val rotate = editImageText.rotate
        val pointF = editImageText.pointF

        onDrawText(text, scale, pointF, rotate, canvas)
        if (saveText) {
            saveText(callback, pointF)
            callback.noneAction()
            return
        }
        if (!isTextRotateMode) {
            return
        }
        if (!::textDeleteBitmap.isInitialized) {
            createRect(callback)
        }
        val offsetValue = textDeleteDstRect.width().toInt() shr 1
        textDeleteDstRect.offsetTo(mMoveBoxRect.left - offsetValue, mMoveBoxRect.top - offsetValue)
        textRotateDstRect.offsetTo(mMoveBoxRect.right - offsetValue, mMoveBoxRect.bottom - offsetValue)
        textDeleteDstRect.rotateRect(mMoveBoxRect.centerX(), mMoveBoxRect.centerY(), editImageText.rotate)
        textRotateDstRect.rotateRect(mMoveBoxRect.centerX(), mMoveBoxRect.centerY(), editImageText.rotate)
        if (showTextMoveBox) {
            canvas.save()
            canvas.rotate(editImageText.rotate, mMoveBoxRect.centerX(), mMoveBoxRect.centerY())
            framePaint.strokeWidth = textFramePaintWidth
            framePaint.color = textFramePaintColor
            canvas.drawRoundRect(mMoveBoxRect, 10f, 10f, framePaint)
            canvas.restore()
        }
        canvas.drawBitmap(textDeleteBitmap, textDeleteRect, textDeleteDstRect, null)
        canvas.drawBitmap(textRotateBitmap, textRotateRect, textRotateDstRect, null)
    }

    override fun onDrawCache(callback: OnEditImageCallback, canvas: Canvas, editImageCache: EditImageCache) {
        val editImageText = editImageCache.findCache<EditImageText>()
        val text = editImageText.text
        val scale = editImageText.scale
        val rotate = editImageText.rotate
        val pointF = editImageText.pointF

        val textSize = when {
            editImageText.editScale == callback.viewScale -> {
                editImageText.textSize
            }
            editImageText.editScale > callback.viewScale -> {
                editImageText.textSize / (editImageText.editScale / callback.viewScale)
            }
            else -> {
                editImageText.textSize * (callback.viewScale / editImageText.editScale)
            }
        }
        textPaint.color = editImageText.color
        textPaint.textSize = textSize
        callback.onSourceToViewCoord(pointF, cacheTextPointF)
        onDrawText(text, scale, cacheTextPointF, rotate, canvas)
    }

    override fun onDrawBitmap(callback: OnEditImageCallback, canvas: Canvas, editImageCache: EditImageCache) {
        val editImageText = editImageCache.findCache<EditImageText>()
        val text = editImageText.text
        val scale = editImageText.scale
        val rotate = editImageText.rotate
        val pointF = editImageText.pointF
        textPaint.color = editImageText.color
        textPaint.textSize = editImageText.textSize / editImageText.editScale
        onDrawText(text, scale, pointF, rotate, canvas)
    }

    override fun onDown(callback: OnEditImageCallback, x: Float, y: Float) {
        when {
            textDeleteDstRect.contains(x, y) -> {
                editTextType = EditTextType.NONE
                callback.noneAction()
            }
            textRotateDstRect.contains(x, y) -> {
                editTextType = EditTextType.ROTATE
                textPointF.set(textRotateDstRect.centerX(), textRotateDstRect.centerY())
            }
            detectInHelpBox(x, y) -> {
                editTextType = EditTextType.MOVE
                textPointF.set(x, y)
            }
            else -> editTextType = EditTextType.NONE
        }
    }

    override fun onMove(callback: OnEditImageCallback, x: Float, y: Float) {
        if (editTextType === EditTextType.MOVE) {
            editImageText.pointF.x += x - textPointF.x
            editImageText.pointF.y += y - textPointF.y
        } else if (editTextType === EditTextType.ROTATE) {
            MatrixAndRectHelper.refreshRotateAndScale(editImageText, mMoveBoxRect, textRotateDstRect, x - textPointF.x, y - textPointF.y)
        }
        callback.onInvalidate()
        textPointF.set(x, y)
    }

    override fun onUp(callback: OnEditImageCallback, x: Float, y: Float) {
        //
    }

    override fun copy(): OnEditImageAction {
        return TextAction(textPaintColor, isTextRotateMode, textPaintSize, textFramePaintColor, textFramePaintWidth, showTextMoveBox, textDeleteDrawableId, textRotateDrawableId)
    }

    override fun onTouchEvent(callback: OnEditImageCallback, touchEvent: MotionEvent): Boolean {
        if (callback.viewEditType == EditType.NONE) {
            return true
        }
        when (touchEvent.action) {
            MotionEvent.ACTION_UP -> {
                if (editTextType == EditTextType.NONE) {
                    editTextType = EditTextType.MOVE
                }
            }
        }
        return editTextType != EditTextType.NONE
    }

    fun saveText(callback: OnEditImageCallback) {
        saveText = true
        callback.onInvalidate()
    }

    private fun saveText(callback: OnEditImageCallback, pointF: PointF) {
        callback.onAddCache(createCache(callback, EditImageText(
                callback.onViewToSourceCoord(pointF),
                editImageText.scale,
                editImageText.rotate,
                editImageText.text,
                textPaint.color,
                textPaint.textSize,
                callback.viewScale
        )))
        editTextType = EditTextType.NONE
    }

    private fun detectInHelpBox(x: Float, y: Float): Boolean {
        movePointF.set(x, y)
        movePointF.rotatePoint(mMoveBoxRect.centerX(), mMoveBoxRect.centerY(), -editImageText.rotate)
        return mMoveBoxRect.contains(movePointF.x, movePointF.y)
    }

    private fun onDrawText(text: String, scale: Float, pointF: PointF, rotate: Float, canvas: Canvas) {
        textContents.clear()
        Collections.addAll(textContents, *text.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
        if (textContents.isEmpty()) {
            return
        }
        textRect.setEmpty()
        textTempRect.setEmpty()
        val fontMetrics = textPaint.fontMetricsInt
        val charMinHeight = abs(fontMetrics.top) + abs(fontMetrics.bottom)
        for (textContent in textContents) {
            textPaint.getTextBounds(textContent, 0, textContent.length, textTempRect)
            if (textTempRect.height() <= 0) {
                textTempRect.set(0, 0, 0, charMinHeight)
            }
            MatrixAndRectHelper.rectAddV(textRect, textTempRect, 0, charMinHeight)
        }
        textRect.offset(pointF.x, pointF.y)
        mMoveBoxRect.set(textRect.left - PADDING, textRect.top - PADDING, textRect.right + PADDING, textRect.bottom + PADDING)
        mMoveBoxRect.scaleRect(scale)
        canvas.save()
        canvas.scale(scale, scale, mMoveBoxRect.centerX(), mMoveBoxRect.centerY())
        canvas.rotate(rotate, mMoveBoxRect.centerX(), mMoveBoxRect.centerY())
        var drawTextY = pointF.y + (charMinHeight shr 1)
        for (textContent in textContents) {
            canvas.drawText(textContent, pointF.x, drawTextY, textPaint)
            drawTextY += charMinHeight
        }
        canvas.restore()
    }
}
