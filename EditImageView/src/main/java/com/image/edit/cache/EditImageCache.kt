package com.image.edit.cache

import android.graphics.Path
import android.graphics.PointF
import com.davemorrissey.labs.subscaleview.ImageViewState
import com.image.edit.EditType
import com.image.edit.action.OnEditImageBaseActionListener

/**
 * @author y
 * @create 2018/11/17
 */
class EditImagePath(var path: Path, var width: Float, var color: Int)

class EditImagePathCircle(var startPointF: PointF, var endPointF: PointF, var radius: Float, var width: Float, var color: Int)

class EditImagePathLine(var startPointF: PointF, var endPointF: PointF, var width: Float, var color: Int)

class EditImagePathRect(var startPointF: PointF, var endPointF: PointF, var width: Float, var color: Int)

class EditImageText(var pointF: PointF, var scale: Float, var rotate: Float, var text: String, var color: Int, var textSize: Float){
    override fun toString(): String {
        return "EditImageText(pointF=$pointF, scale=$scale, rotate=$rotate, text='$text', color=$color, textSize=$textSize)"
    }
}

@Suppress("MemberVisibilityCanBePrivate")
class EditImageCache {
    var imageViewState: ImageViewState? = null
    var onEditImageBaseActionListener: OnEditImageBaseActionListener? = null
    var editType: EditType

    lateinit var editImagePath: EditImagePath
    lateinit var editImagePathLine: EditImagePathLine
    lateinit var editImagePathRect: EditImagePathRect
    lateinit var editImagePathCircle: EditImagePathCircle
    lateinit var editImageText: EditImageText

    fun reset() {
        imageViewState = null
        onEditImageBaseActionListener = null
    }

    companion object {

        fun createPointCache(imageViewState: ImageViewState?, onEditImageBaseActionListener: OnEditImageBaseActionListener, editImagePath: EditImagePath): EditImageCache {
            return EditImageCache(imageViewState, onEditImageBaseActionListener, EditType.PAINT, editImagePath)
        }

        fun createPointLineCache(imageViewState: ImageViewState?, onEditImageBaseActionListener: OnEditImageBaseActionListener, editImagePathLine: EditImagePathLine): EditImageCache {
            return EditImageCache(imageViewState, onEditImageBaseActionListener, EditType.PAINT, editImagePathLine)
        }

        fun createPointRectCache(imageViewState: ImageViewState?, onEditImageBaseActionListener: OnEditImageBaseActionListener, editImagePathRect: EditImagePathRect): EditImageCache {
            return EditImageCache(imageViewState, onEditImageBaseActionListener, EditType.PAINT, editImagePathRect)
        }

        fun createPointCircleCache(imageViewState: ImageViewState?, onEditImageBaseActionListener: OnEditImageBaseActionListener, editImagePathCircle: EditImagePathCircle): EditImageCache {
            return EditImageCache(imageViewState, onEditImageBaseActionListener, EditType.PAINT, editImagePathCircle)
        }

        fun createEraserPointCache(imageViewState: ImageViewState?, onEditImageBaseActionListener: OnEditImageBaseActionListener, editImagePath: EditImagePath): EditImageCache {
            return EditImageCache(imageViewState, onEditImageBaseActionListener, EditType.ERASER, editImagePath)
        }

        fun createTextCache(imageViewState: ImageViewState?, onEditImageBaseActionListener: OnEditImageBaseActionListener, editImageText: EditImageText): EditImageCache {
            return EditImageCache(imageViewState, onEditImageBaseActionListener, EditType.TEXT, editImageText)
        }
    }

    private constructor(imageViewState: ImageViewState?, onEditImageBaseActionListener: OnEditImageBaseActionListener, editType: EditType, editImagePath: EditImagePath) {
        this.imageViewState = imageViewState
        this.editType = editType
        this.editImagePath = editImagePath
        this.onEditImageBaseActionListener = onEditImageBaseActionListener
    }

    private constructor(imageViewState: ImageViewState?, onEditImageBaseActionListener: OnEditImageBaseActionListener, editType: EditType, editImageText: EditImageText) {
        this.imageViewState = imageViewState
        this.editType = editType
        this.editImageText = editImageText
        this.onEditImageBaseActionListener = onEditImageBaseActionListener
    }

    private constructor(imageViewState: ImageViewState?, onEditImageBaseActionListener: OnEditImageBaseActionListener, editType: EditType, editImagePathLine: EditImagePathLine) {
        this.imageViewState = imageViewState
        this.editType = editType
        this.editImagePathLine = editImagePathLine
        this.onEditImageBaseActionListener = onEditImageBaseActionListener
    }

    private constructor(imageViewState: ImageViewState?, onEditImageBaseActionListener: OnEditImageBaseActionListener, editType: EditType, editImagePathRect: EditImagePathRect) {
        this.imageViewState = imageViewState
        this.editType = editType
        this.editImagePathRect = editImagePathRect
        this.onEditImageBaseActionListener = onEditImageBaseActionListener
    }

    private constructor(imageViewState: ImageViewState?, onEditImageBaseActionListener: OnEditImageBaseActionListener, editType: EditType, editImagePathCircle: EditImagePathCircle) {
        this.imageViewState = imageViewState
        this.editType = editType
        this.editImagePathCircle = editImagePathCircle
        this.onEditImageBaseActionListener = onEditImageBaseActionListener
    }
}
