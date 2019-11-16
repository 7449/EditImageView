package com.image.edit.text

import android.graphics.*
import android.text.TextPaint
import android.text.TextUtils
import android.view.MotionEvent
import com.davemorrissey.labs.subscaleview.api.getState
import com.davemorrissey.labs.subscaleview.api.viewToSourceCoord
import com.image.edit.EditImageView
import com.image.edit.EditType
import com.image.edit.OnEditImageAction
import com.image.edit.EditImageCache
import com.image.edit.createCache
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
        const val PADDING = 30
    }

    private var isInitRect = false
    private val textPointF = PointF()
    private val movePointF = PointF(0f, 0f)
    private val textRect = RectF()
    private val textTempRect = Rect()
    private val textDeleteRect = Rect()
    private val textRotateRect = Rect()
    private val textDeleteDstRect = RectF(0f, 0f, (STICKER_BTN_HALF_SIZE shl 1).toFloat(), (STICKER_BTN_HALF_SIZE shl 1).toFloat())
    private val textRotateDstRect = RectF(0f, 0f, (STICKER_BTN_HALF_SIZE shl 1).toFloat(), (STICKER_BTN_HALF_SIZE shl 1).toFloat())
    private val mMoveBoxRect = RectF()
    private val textContents = ArrayList<String>(2)
    private lateinit var textDeleteBitmap: Bitmap
    private lateinit var textRotateBitmap: Bitmap
    private var framePaint: Paint = Paint()
    var textPaint: TextPaint = TextPaint(Paint.LINEAR_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG)
    lateinit var editImageText: EditImageText
    var editTextType = EditTextType.NONE

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
        if (TextUtils.isEmpty(editImageText.text)) {
            return
        }
        textPaint.textSize = textPaintSize
        textPaint.color = textPaintColor
        onDrawText(editImageText, canvas)
        if (!isTextRotateMode) {
            return
        }
        if (!isInitRect) {
            createRect(editImageView)
            isInitRect = true
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

    override fun onDown(editImageView: EditImageView, x: Float, y: Float) {
        when {
            textDeleteDstRect.contains(x, y) -> {
                editImageView.onEditImageListener?.onDeleteText()
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

    private fun detectInHelpBox(x: Float, y: Float): Boolean {
        movePointF.set(x, y)
        movePointF.rotatePoint(mMoveBoxRect.centerX(), mMoveBoxRect.centerY(), -editImageText.rotate)
        return mMoveBoxRect.contains(movePointF.x, movePointF.y)
    }

    override fun onMove(editImageView: EditImageView, x: Float, y: Float) {
        val editTextType = editTextType
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
    }

    override fun onSaveImageCache(editImageView: EditImageView) {
        editImageView.viewToSourceCoord(editImageText.pointF, editImageText.pointF)
        textPaint.textSize /= editImageView.scale
        onDrawText(editImageText, editImageView.newBitmapCanvas)
        editImageView.cacheArrayList.add(createCache(editImageView.getState(), EditImageText(
                editImageText.pointF,
                editImageText.scale,
                editImageText.rotate,
                editImageText.text,
                textPaint.color,
                textPaint.textSize
        )))
        editTextType = EditTextType.NONE
        editImageView.editType = EditType.NONE
    }

    override fun onLastImageCache(editImageView: EditImageView, editImageCache: EditImageCache<EditImageText>) {
        val imageText = editImageCache.imageCache
        textPaint.color = imageText.color
        textPaint.textSize = imageText.textSize
        onDrawText(imageText, editImageView.newBitmapCanvas)
    }

    private fun onDrawText(editImageText: EditImageText, canvas: Canvas) {
        textContents.clear()
        Collections.addAll(textContents, *editImageText.text.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
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
        textRect.offset(editImageText.pointF.x, editImageText.pointF.y)
        mMoveBoxRect.set(textRect.left - PADDING, textRect.top - PADDING, textRect.right + PADDING, textRect.bottom + PADDING)
        mMoveBoxRect.scaleRect(editImageText.scale)
        canvas.save()
        canvas.scale(editImageText.scale, editImageText.scale, mMoveBoxRect.centerX(), mMoveBoxRect.centerY())
        canvas.rotate(editImageText.rotate, mMoveBoxRect.centerX(), mMoveBoxRect.centerY())
        var drawTextY = editImageText.pointF.y + (charMinHeight shr 1)
        for (textContent in textContents) {
            canvas.drawText(textContent, editImageText.pointF.x, drawTextY, textPaint)
            drawTextY += charMinHeight
        }
        canvas.restore()
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
}
