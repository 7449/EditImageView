package com.image.edit.text

import android.graphics.PointF

data class EditImageText(val pointF: PointF, var scale: Float, var rotate: Float, val text: String, val color: Int, val textSize: Float, val editScale: Float)