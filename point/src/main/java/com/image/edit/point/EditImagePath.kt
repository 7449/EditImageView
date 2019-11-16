package com.image.edit.point

import android.graphics.Path
import com.image.edit.cache.CacheCallback

data class EditImagePath(var path: Path, var width: Float, var color: Int) : CacheCallback