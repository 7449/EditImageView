package com.image.edit.circle

import android.graphics.PointF
import com.image.edit.CacheCallback

data class CirclePath(var startPointF: PointF, var endPointF: PointF, var radius: Float, var width: Float, var color: Int) : CacheCallback