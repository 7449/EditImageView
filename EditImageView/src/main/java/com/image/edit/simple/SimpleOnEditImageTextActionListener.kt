package com.image.edit.simple

import android.graphics.*
import android.text.TextUtils
import com.image.edit.EditImageView
import com.image.edit.EditTextType
import com.image.edit.EditType
import com.image.edit.action.OnEditImageTextActionListener
import com.image.edit.cache.EditImageCache
import com.image.edit.cache.EditImageText
import com.image.edit.helper.MatrixAndRectHelper
import java.util.*

/**
 * @author y
 * @create 2018/11/20
 */
class SimpleOnEditImageTextActionListener : OnEditImageTextActionListener {

    companion object {
        private const val STICKER_BTN_HALF_SIZE = 30
        private const val PADDING = 30
    }

    private var isInitRect = false
    private val textPointF = PointF()
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

    private fun createRect(editImageView: EditImageView) {
        textDeleteBitmap = BitmapFactory.decodeResource(editImageView.resources, editImageView.editImageConfig.textDeleteDrawableId)
        textRotateBitmap = BitmapFactory.decodeResource(editImageView.resources, editImageView.editImageConfig.textRotateDrawableId)
        textDeleteRect.set(0, 0, textDeleteBitmap.width, textDeleteBitmap.height)
        textRotateRect.set(0, 0, textRotateBitmap.width, textRotateBitmap.height)
    }

    override fun onDraw(editImageView: EditImageView, canvas: Canvas) {
        val editImageText = editImageView.editImageText
        if (editImageView.editType != EditType.TEXT) {
            return
        }
        if (TextUtils.isEmpty(editImageText.text)) {
            return
        }
        onDrawText(editImageView, editImageView.editImageText, canvas)
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
        MatrixAndRectHelper.rotateRect(textDeleteDstRect, mMoveBoxRect.centerX(), mMoveBoxRect.centerY(), editImageText.rotate)
        MatrixAndRectHelper.rotateRect(textRotateDstRect, mMoveBoxRect.centerX(), mMoveBoxRect.centerY(), editImageText.rotate)
        if (editImageView.editImageConfig.showTextMoveBox) {
            canvas.save()
            canvas.rotate(editImageText.rotate, mMoveBoxRect.centerX(), mMoveBoxRect.centerY())
            canvas.drawRoundRect(mMoveBoxRect, 10f, 10f, editImageView.framePaint)
            canvas.restore()
        }
        canvas.drawBitmap(textDeleteBitmap, textDeleteRect, textDeleteDstRect, null)
        canvas.drawBitmap(textRotateBitmap, textRotateRect, textRotateDstRect, null)
    }

    override fun onDown(editImageView: EditImageView, x: Float, y: Float) {
        when {
            textDeleteDstRect.contains(x, y) -> {
                editImageView.onEditImageListener.onDeleteText()
                editImageView.editTextType = EditTextType.NONE
                editImageView.editType = EditType.NONE
            }
            textRotateDstRect.contains(x, y) -> {
                editImageView.editTextType = EditTextType.ROTATE
                textPointF.set(textRotateDstRect.centerX(), textRotateDstRect.centerY())
            }
            mMoveBoxRect.contains(x, y) -> {
                editImageView.editTextType = EditTextType.MOVE
                textPointF.set(x, y)
            }
            else -> editImageView.editTextType = EditTextType.NONE
        }
    }

    override fun onMove(editImageView: EditImageView, x: Float, y: Float) {
        val editTextType = editImageView.editTextType
        val editImageText = editImageView.editImageText
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
        val supperMatrix = editImageView.supperMatrix
        val editImageText = editImageView.editImageText
        MatrixAndRectHelper.refreshMatrix(newBitmapCanvas, supperMatrix!!) { _, _, _, _ -> onDrawText(editImageView, editImageText, newBitmapCanvas) }
        editImageView.viewToSourceCoord(editImageText.pointF, editImageText.pointF)
        editImageText.textSize = editImageText.textSize / editImageView.scale
        editImageView.cacheArrayList.add(EditImageCache.createTextCache(editImageView.state, this, editImageText))
        editImageView.editTextType = EditTextType.NONE
        editImageView.editType = EditType.NONE
    }

    override fun onLastImageCache(editImageView: EditImageView, editImageCache: EditImageCache) {
        val textPaint = editImageView.textPaint
        val editImageText = editImageCache.editImageText
        textPaint.color = editImageText.color
        textPaint.textSize = editImageText.textSize
        onDrawText(editImageView, editImageText, editImageView.newBitmapCanvas)
    }

    override fun onDrawText(editImageView: EditImageView, editImageText: EditImageText, canvas: Canvas) {
        textContents.clear()
        val splits = editImageText.text.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        Collections.addAll(textContents, *splits)
        if (textContents.isEmpty()) return
        textRect.set(0.toFloat(), 0.toFloat(), 0.toFloat(), 0.toFloat())
        val fontMetrics = editImageView.textPaint.fontMetricsInt
        val charMinHeight = Math.abs(fontMetrics.top) + Math.abs(fontMetrics.bottom)
        for (textContent in textContents) {
            editImageView.textPaint.getTextBounds(textContent, 0, textContent.length, textTempRect)
            if (textTempRect.height() <= 0) {
                textTempRect.set(0, 0, 0, charMinHeight)
            }
            MatrixAndRectHelper.rectAddV(textRect, textTempRect, 0, charMinHeight)
        }
        textRect.offset(editImageText.pointF.x, editImageText.pointF.y)
        mMoveBoxRect.set((textRect.left - PADDING), (textRect.top - PADDING), (textRect.right + PADDING), (textRect.bottom + PADDING))
        MatrixAndRectHelper.scaleRect(mMoveBoxRect, editImageText.scale)
        canvas.save()
        canvas.scale(editImageText.scale, editImageText.scale, mMoveBoxRect.centerX(), mMoveBoxRect.centerY())
        canvas.rotate(editImageText.rotate, mMoveBoxRect.centerX(), mMoveBoxRect.centerY())
        var drawTextY = editImageText.pointF.y + (charMinHeight shr 1) + PADDING
        for (textContent in textContents) {
            canvas.drawText(textContent, editImageText.pointF.x, drawTextY, editImageView.textPaint)
            drawTextY += charMinHeight
        }
        canvas.restore()
    }
}
