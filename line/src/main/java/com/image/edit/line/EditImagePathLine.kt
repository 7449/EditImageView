package com.image.edit.line

import android.graphics.PointF
import com.image.edit.cache.CacheCallback

data class EditImagePathLine(var startPointF: PointF, var endPointF: PointF, var width: Float, var color: Int) : CacheCallback