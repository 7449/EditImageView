package com.image.edit.simple

import android.graphics.*
import android.text.TextPaint
import com.image.edit.EditImageView
import com.image.edit.OnEditImageInitializeListener

/**
 * @author y
 * @create 2018/11/20
 */
class SimpleOnEditImageInitializeListener : OnEditImageInitializeListener {

    override fun initPointPaint(editImageView: EditImageView): Paint {
        val paint = Paint()
        paint.flags = Paint.ANTI_ALIAS_FLAG
        paint.isAntiAlias = true
        paint.isDither = true
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND
        paint.pathEffect = PathEffect()
        paint.style = Paint.Style.STROKE
        return paint
    }

    override fun initEraserPaint(editImageView: EditImageView): Paint {
        val eraserPaint = Paint()
        eraserPaint.alpha = 0
        eraserPaint.color = Color.TRANSPARENT
        eraserPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        eraserPaint.isAntiAlias = true
        eraserPaint.isDither = true
        eraserPaint.style = Paint.Style.STROKE
        eraserPaint.strokeJoin = Paint.Join.ROUND
        eraserPaint.strokeCap = Paint.Cap.ROUND
        eraserPaint.pathEffect = PathEffect()
        return eraserPaint
    }

    override fun initTextPaint(editImageView: EditImageView): TextPaint {
        val textPaint = TextPaint()
        textPaint.isAntiAlias = true
        return textPaint
    }

    override fun initTextFramePaint(editImageView: EditImageView): Paint {
        val paint = Paint()
        paint.style = Paint.Style.STROKE
        paint.isAntiAlias = true
        return paint
    }
}
