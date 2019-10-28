package com.davemorrissey.labs.subscaleview.temp

import android.graphics.PointF
import java.io.Serializable

/**
 * Wraps the scale, center and orientation of a displayed image for easy restoration on screen rotate.
 */
class ImageViewState(var scale: Float, var center: PointF, var orientation: Int) : Serializable
