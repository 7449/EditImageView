package com.image.edit.cache

import com.davemorrissey.labs.subscaleview.ImageViewState
import com.image.edit.action.OnEditImageActionListener

/**
 * @author y
 * @create 2018/11/17
 */

interface EditImageCacheCallback

class EditImageCache(var imageViewState: ImageViewState?, var onEditImageActionListener: OnEditImageActionListener?, var imageCache: EditImageCacheCallback) {

    fun reset() {
        imageViewState = null
        onEditImageActionListener = null
    }

    companion object {
        fun <T : EditImageCacheCallback> createCache(imageViewState: ImageViewState?, onEditImageActionListener: OnEditImageActionListener?, imageCache: T): EditImageCache {
            return EditImageCache(imageViewState, onEditImageActionListener, imageCache)
        }
    }
}

