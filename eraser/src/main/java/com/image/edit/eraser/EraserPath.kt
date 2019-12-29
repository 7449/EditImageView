package com.image.edit.eraser

import android.graphics.PointF
import com.image.edit.CacheCallback

data class EraserPath(val pointFList: List<PointF>, val width: Float, val color: Int, val scale: Float) : CacheCallback