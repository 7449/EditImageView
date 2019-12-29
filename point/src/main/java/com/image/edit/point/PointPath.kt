package com.image.edit.point

import android.graphics.PointF
import com.image.edit.CacheCallback

data class PointPath(val pointFList: List<PointF>, val width: Float, val color: Int, val scale: Float) : CacheCallback