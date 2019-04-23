package com.image.edit.cache

import com.davemorrissey.labs.subscaleview.ImageViewState
import com.image.edit.action.OnEditImageAction

/**
 * @author y
 * @create 2018/11/17
 */

class EditImageCache(var imageViewState: ImageViewState?, var onEditImageAction: OnEditImageAction?, var imageCache: Any) {

    fun reset() {
        imageViewState = null
        onEditImageAction = null
    }

    @Suppress("UNCHECKED_CAST")
    fun <CACHE : Any> transformerCache(): CACHE = imageCache as CACHE

    companion object {
        @JvmStatic
        fun <T : Any> createCache(imageViewState: ImageViewState?, onEditImageAction: OnEditImageAction?, imageCache: T): EditImageCache {
            return EditImageCache(imageViewState, onEditImageAction, imageCache)
        }
    }
}

