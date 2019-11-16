package com.image.edit.eraser

import android.graphics.Path
import com.image.edit.CacheCallback

data class EraserPath(var path: Path, var width: Float, var color: Int) : CacheCallback