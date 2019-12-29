package com.image.edit.line

import android.graphics.PointF
import com.image.edit.CacheCallback

data class LinePath(val startPointF: PointF, val endPointF: PointF, val width: Float, val color: Int, val scale: Float) : CacheCallback