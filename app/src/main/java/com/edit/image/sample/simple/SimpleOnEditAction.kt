package com.edit.image.sample.simple

import android.graphics.Canvas
import com.image.edit.EditImageCache
import com.image.edit.OnEditImageAction
import com.image.edit.OnEditImageCallback

class SimpleOnEditAction : OnEditImageAction {
    override fun onDraw(callback: OnEditImageCallback, canvas: Canvas) {
    }

    override fun onDrawCache(callback: OnEditImageCallback, canvas: Canvas, editImageCache: EditImageCache) {
    }

    override fun onDrawBitmap(callback: OnEditImageCallback, canvas: Canvas, editImageCache: EditImageCache) {
    }

    override fun onDown(callback: OnEditImageCallback, x: Float, y: Float) {
    }

    override fun onMove(callback: OnEditImageCallback, x: Float, y: Float) {
    }

    override fun onUp(callback: OnEditImageCallback, x: Float, y: Float) {
    }

    override fun copy(): OnEditImageAction {
        return SimpleOnEditAction()
    }
}