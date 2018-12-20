package com.edit.image.sample

import android.graphics.Paint
import android.text.TextPaint
import com.image.edit.EditImageView
import com.image.edit.OnEditImageInitializeListener

/**
 * @author y
 * @create 2018/12/20
 */
class CustomPaint : OnEditImageInitializeListener {
    override fun initPointPaint(editImageView: EditImageView): Paint {
        return Paint()
    }

    override fun initEraserPaint(editImageView: EditImageView): Paint {
        return Paint()
    }

    override fun initTextPaint(editImageView: EditImageView): TextPaint {
        return TextPaint()
    }

    override fun initTextFramePaint(editImageView: EditImageView): Paint {
        return Paint()
    }
}
