package com.image.edit.simple.text

import android.graphics.*
import android.text.TextPaint
import android.text.TextUtils
import android.view.MotionEvent
import com.image.edit.EditImageView
import com.image.edit.action.OnEditImageAction
import com.image.edit.cache.EditImageCache
import com.image.edit.cache.createCache
import com.image.edit.type.EditType
import com.image.edit.x.hasTextAction
import com.image.edit.x.refresh
import java.util.*


/**
 * @author y
 * @create 2018/11/20
 */

data class EditImageText(var pointF: PointF, var scale: Float, var rotate: Float, var text: String, var color: Int, var textSize: Float)

class SimpleOnEditImageTextAction : OnEditImageAction {

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

    init {
        framePaint.style = Paint.Style.STROKE
        framePaint.isAntiAlias = true
        textPaint.isAntiAlias = true
    }

    private fun createRect(editImageView: EditImageView) {
        textDeleteBitmap = BitmapFactory.decodeResource(editImageView.resources, editImageView.editImageConfig.textDeleteDrawableId)
        textRotateBitmap = BitmapFactory.decodeResource(editImageView.resources, editImageView.editImageConfig.textRotateDrawableId)
        textDeleteRect.set(0, 0, textDeleteBitmap.width, textDeleteBitmap.height)
        textRotateRect.set(0, 0, textRotateBitmap.width, textRotateBitmap.height)
    }

    override fun onDraw(editImageView: EditImageView, canvas: Canvas) {
        if (TextUtils.isEmpty(editImageText.text)) {
            return
        }
        textPaint.textSize = editImageView.editImageConfig.textPaintSize
        textPaint.color = editImageView.editImageConfig.textPaintColor
        onDrawText(editImageText, canvas)
        if (!editImageView.editImageConfig.isTextRotateMode) {
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
        if (editImageView.editImageConfig.showTextMoveBox) {
            canvas.save()
            canvas.rotate(editImageText.rotate, mMoveBoxRect.centerX(), mMoveBoxRect.centerY())
            framePaint.strokeWidth = editImageView.editImageConfig.textFramePaintWidth
            framePaint.color = editImageView.editImageConfig.textFramePaintColor
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
                editImageView.editTextType = EditTextType.NONE
                editImageView.editType = EditType.NONE
            }
            textRotateDstRect.contains(x, y) -> {
                editImageView.editTextType = EditTextType.ROTATE
                textPointF.set(textRotateDstRect.centerX(), textRotateDstRect.centerY())
            }
            detectInHelpBox(x, y) -> {
                editImageView.editTextType = EditTextType.MOVE
                textPointF.set(x, y)
            }
            else -> editImageView.editTextType = EditTextType.NONE
        }
    }

    private fun detectInHelpBox(x: Float, y: Float): Boolean {
        movePointF.set(x, y)
        movePointF.rotatePoint(mMoveBoxRect.centerX(), mMoveBoxRect.centerY(), -editImageText.rotate)
        return mMoveBoxRect.contains(movePointF.x, movePointF.y)
    }

    override fun onMove(editImageView: EditImageView, x: Float, y: Float) {
        val editTextType = editImageView.editTextType
        if (editTextType === EditTextType.MOVE) {
            editImageText.pointF.x += x - textPointF.x
            editImageText.pointF.y += y - textPointF.y
        } else if (editTextType === EditTextType.ROTATE) {
            MatrixAndRectHelper.refreshRotateAndScale(editImageText, mMoveBoxRect, textRotateDstRect, x - textPointF.x, y - textPointF.y)
        }
        textPointF.set(x, y)
        editImageView.refresh()
    }

    override fun onUp(editImageView: EditImageView, x: Float, y: Float) {
    }

    override fun onSaveImageCache(editImageView: EditImageView) {
        val newBitmapCanvas = editImageView.newBitmapCanvas
        editImageView.viewToSourceCoord(editImageText.pointF, editImageText.pointF)
        textPaint.textSize /= editImageView.scale
        onDrawText(editImageText, newBitmapCanvas)
        editImageView.cacheArrayList.add(createCache(editImageView.state, EditImageText(
                editImageText.pointF,
                editImageText.scale,
                editImageText.rotate,
                editImageText.text,
                textPaint.color,
                textPaint.textSize
        )))
        editImageView.editTextType = EditTextType.NONE
        editImageView.editType = EditType.NONE
    }

    override fun onLastImageCache(editImageView: EditImageView, editImageCache: EditImageCache) {
        val imageText = editImageCache.transformerCache<EditImageText>()
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
        val fontMetrics = textPaint.fontMetricsInt
        val charMinHeight = Math.abs(fontMetrics.top) + Math.abs(fontMetrics.bottom)
        for (textContent in textContents) {
            textPaint.getTextBounds(textContent, 0, textContent.length, textTempRect)
            if (textTempRect.height() <= 0) {
                textTempRect.set(0, 0, 0, charMinHeight)
            }
            MatrixAndRectHelper.rectAddV(textRect, textTempRect, 0, charMinHeight)
        }
        textRect.offset(editImageText.pointF.x, editImageText.pointF.y)
        mMoveBoxRect.set(textRect.left - PADDING, textRect.top - (PADDING * 2), textRect.right + PADDING, textRect.bottom + PADDING)
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
                if (editImageView.editTextType == EditTextType.NONE) {
                    editImageView.editTextType = EditTextType.MOVE
                }
            }
        }
        return editImageView.hasTextAction()
    }
}
