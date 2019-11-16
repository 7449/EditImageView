package com.image.edit

import com.davemorrissey.labs.subscaleview.ImageViewState

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