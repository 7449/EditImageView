package com.image.edit.eraser

import android.graphics.Path
import com.image.edit.cache.CacheCallback

data class EditImageEraserPath(var path: Path, var width: Float, var color: Int) : CacheCallback