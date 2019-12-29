package com.image.edit.circle

import android.graphics.PointF
import com.image.edit.CacheCallback

data class CirclePath(val startPointF: PointF, val endPointF: PointF, val radius: Float, val width: Float, val color: Int, val scale: Float) : CacheCallback