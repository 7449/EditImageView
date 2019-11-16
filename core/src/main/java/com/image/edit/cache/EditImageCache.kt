package com.image.edit.cache

import com.davemorrissey.labs.subscaleview.ImageViewState
import com.image.edit.OnEditImageAction

interface CacheCallback

data class EditImageCache<CACHE : CacheCallback>(
        var imageViewState: ImageViewState?,
        var onEditImageAction: OnEditImageAction<CACHE>?,
        var imageCache: CACHE
)

fun <CACHE : CacheCallback> EditImageCache<CACHE>.reset() {
    imageViewState = null
    onEditImageAction = null
}