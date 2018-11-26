package com.image.edit.simple

import android.graphics.Canvas

import com.image.edit.EditImageView
import com.image.edit.action.OnEditImageBaseActionListener
import com.image.edit.cache.EditImageCache

/**
 * @author y
 * @create 2018/11/20
 */
class SimpleOnEditImageActionListener : OnEditImageBaseActionListener {

    override fun onDraw(editImageView: EditImageView, canvas: Canvas) {

    }

    override fun onDown(editImageView: EditImageView, x: Float, y: Float) {

    }

    override fun onMove(editImageView: EditImageView, x: Float, y: Float) {

    }

    override fun onUp(editImageView: EditImageView, x: Float, y: Float) {

    }

    override fun onSaveImageCache(editImageView: EditImageView) {

    }

    override fun onLastImageCache(editImageView: EditImageView, editImageCache: EditImageCache) {

    }
}
