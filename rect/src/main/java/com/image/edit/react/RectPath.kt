package com.image.edit.react

import android.graphics.PointF
import com.image.edit.CacheCallback

data class RectPath(var startPointF: PointF, var endPointF: PointF, var width: Float, var color: Int) : CacheCallback