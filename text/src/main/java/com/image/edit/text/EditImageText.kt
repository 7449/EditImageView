package com.image.edit.text

import android.graphics.PointF
import com.image.edit.CacheCallback

data class EditImageText(var pointF: PointF, var scale: Float, var rotate: Float, var text: String, var color: Int, var textSize: Float) : CacheCallback