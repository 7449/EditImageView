package com.image.edit.circle

import android.graphics.PointF
import com.image.edit.cache.CacheCallback

data class EditImagePathCircle(var startPointF: PointF, var endPointF: PointF, var radius: Float, var width: Float, var color: Int) : CacheCallback