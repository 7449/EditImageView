package com.edit.image.sample

import android.graphics.Canvas
import android.util.Log

import com.image.edit.EditImageView
import com.image.edit.action.OnEditImageCustomActionListener
import com.image.edit.cache.EditImageCache

/**
 * @author y
 * @create 2018/12/3
 */
const val TAG: String = "EditImage"

class SimpleOnEditImageCustomActionListener : OnEditImageCustomActionListener {

    override fun onDraw(editImageView: EditImageView, canvas: Canvas) {
        Log.d(TAG, "onDraw")
    }

    override fun onDown(editImageView: EditImageView, x: Float, y: Float) {
        Log.d(TAG, "onDown")
    }

    override fun onMove(editImageView: EditImageView, x: Float, y: Float) {
        Log.d(TAG, "onMove")
        editImageView.refresh()
    }

    override fun onUp(editImageView: EditImageView, x: Float, y: Float) {
        Log.d(TAG, "onUp")
    }

    override fun onSaveImageCache(editImageView: EditImageView) {

    }

    override fun onLastImageCache(editImageView: EditImageView, editImageCache: EditImageCache) {

    }
}
