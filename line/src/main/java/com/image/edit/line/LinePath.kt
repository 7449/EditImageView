package com.image.edit.line

import android.graphics.PointF
import com.image.edit.CacheCallback

data class LinePath(var startPointF: PointF, var endPointF: PointF, var width: Float, var color: Int) : CacheCallback