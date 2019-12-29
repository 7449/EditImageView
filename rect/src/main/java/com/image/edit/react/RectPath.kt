package com.image.edit.react

import android.graphics.PointF
import com.image.edit.CacheCallback

data class RectPath(val startPointF: PointF, val endPointF: PointF, val width: Float, val color: Int, val scale: Float) : CacheCallback