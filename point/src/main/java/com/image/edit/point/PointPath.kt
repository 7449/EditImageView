package com.image.edit.point

import android.graphics.Path
import com.image.edit.CacheCallback

data class PointPath(var path: Path, var width: Float, var color: Int) : CacheCallback