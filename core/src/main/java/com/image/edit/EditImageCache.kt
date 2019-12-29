package com.image.edit

import com.davemorrissey.labs.subscaleview.ImageViewState

interface CacheCallback

data class EditImageCache<CACHE : CacheCallback>(
        val imageViewState: ImageViewState?,
        val onEditImageAction: OnEditImageAction<CACHE>,
        val imageCache: CACHE
)