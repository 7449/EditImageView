package com.image.edit.text

import android.graphics.*
import android.text.TextPaint
import android.text.TextUtils
import android.view.MotionEvent
import com.image.edit.*
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
        var textRotateDrawableId: Int = R.drawable.ic_edit_image_rotate
) : OnEditImageAction<EditImageText> {

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
    var editTextType = EditTextType.NONE
    private lateinit var textDeleteBitmap: Bitmap
    private lateinit var textRotateBitmap: Bitmap
    lateinit var editImageText: EditImageText

    init {
        framePaint.style = Paint.Style.STROKE
        framePaint.isAntiAlias = true
        textPaint.isAntiAlias = true
    }

    private fun createRect(editImageView: EditImageView) {
        textDeleteBitmap = BitmapFactory.decodeResource(editImageView.resources, textDeleteDrawableId)
        textRotateBitmap = BitmapFactory.decodeResource(editImageView.resources, textRotateDrawableId)
        textDeleteRect.set(0, 0, textDeleteBitmap.width, textDeleteBitmap.height)
        textRotateRect.set(0, 0, textRotateBitmap.width, textRotateBitmap.height)
    }

    override fun onDraw(editImageView: EditImageView, canvas: Canvas) {
        textPaint.textSize = textPaintSize
        textPaint.color = textPaintColor
        if (TextUtils.isEmpty(editImageText.text)
                || editTextType == EditTextType.NONE) {
            return
        }

        val text = editImageText.text
        val scale = editImageText.scale
        val rotate = editImageText.rotate
        val pointF = editImageText.pointF

        if (saveText) {
            saveText(editImageView, pointF)
            return
        }

        onDrawText(text, scale, pointF, rotate, canvas)

        if (!isTextRotateMode) {
            return
        }
        if (!::textDeleteBitmap.isInitialized) {
            createRect(editImageView)
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

    override fun onDrawCache(editImageView: EditImageView, canvas: Canvas, editImageCache: EditImageCache<EditImageText>) {
        val editImageText = editImageCache.imageCache
        val text = editImageText.text
        val scale = editImageText.scale
        val rotate = editImageText.rotate
        val pointF = editImageText.pointF

        val textSize = when {
            editImageText.editScale == editImageView.scale -> {
                editImageText.textSize
            }
            editImageText.editScale > editImageView.scale -> {
                editImageText.textSize / (editImageText.editScale / editImageView.scale)
            }
            else -> {
                editImageText.textSize * (editImageView.scale / editImageText.editScale)
            }
        }
        textPaint.color = editImageText.color
        textPaint.textSize = textSize
        editImageView.sourceToViewCoord(pointF, cacheTextPointF)
        onDrawText(text, scale, cacheTextPointF, rotate, canvas)
    }

    override fun onDrawBitmap(editImageView: EditImageView, canvas: Canvas, editImageCache: EditImageCache<EditImageText>) {
        val editImageText = editImageCache.imageCache
        val text = editImageText.text
        val scale = editImageText.scale
        val rotate = editImageText.rotate
        val pointF = editImageText.pointF
        textPaint.color = editImageText.color
        textPaint.textSize = editImageText.textSize / editImageText.editScale
        onDrawText(text, scale, pointF, rotate, canvas)
    }

    override fun onDown(editImageView: EditImageView, x: Float, y: Float) {
        when {
            textDeleteDstRect.contains(x, y) -> {
                editTextType = EditTextType.NONE
                editImageView.editType = EditType.NONE
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

    override fun onMove(editImageView: EditImageView, x: Float, y: Float) {
        if (editTextType === EditTextType.MOVE) {
            editImageText.pointF.x += x - textPointF.x
            editImageText.pointF.y += y - textPointF.y
        } else if (editTextType === EditTextType.ROTATE) {
            MatrixAndRectHelper.refreshRotateAndScale(editImageText, mMoveBoxRect, textRotateDstRect, x - textPointF.x, y - textPointF.y)
        }
        editImageView.invalidate()
        textPointF.set(x, y)
    }

    override fun onUp(editImageView: EditImageView, x: Float, y: Float) {
        //
    }

    override fun onTouchEvent(editImageView: EditImageView, touchEvent: MotionEvent): Boolean {
        if (editImageView.editType == EditType.NONE) {
            return true
        }
        when (touchEvent.action) {
            MotionEvent.ACTION_UP -> {
                if (editTextType == EditTextType.NONE) {
                    editTextType = EditTextType.MOVE
                }
            }
        }
        return editImageView.hasTextAction()
    }

    fun saveText(editImageView: EditImageView) {
        saveText = true
        editImageView.invalidate()
    }

    private fun saveText(editImageView: EditImageView, pointF: PointF) {
        if (editImageView.isMaxCount) {
            editImageView.onEditImageListener?.onLastCacheMax()
            return
        }
        editImageView.cacheArrayList.add(createCache(editImageView.state, EditImageText(
                editImageView.viewToSourceCoords(pointF),
                editImageText.scale,
                editImageText.rotate,
                editImageText.text,
                textPaint.color,
                textPaint.textSize,
                editImageView.scale
        )))
        editTextType = EditTextType.NONE
        editImageView.editType = EditType.NONE
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
