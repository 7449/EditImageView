package com.image.edit.react

import android.graphics.PointF
import com.image.edit.cache.CacheCallback

data class EditImagePathRect(var startPointF: PointF, var endPointF: PointF, var width: Float, var color: Int) : CacheCallback