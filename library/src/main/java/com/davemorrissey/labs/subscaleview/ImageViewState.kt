package com.davemorrissey.labs.subscaleview

import android.graphics.PointF
import java.io.Serializable

/**
 * Wraps the scale, center and orientation of a displayed image for easy restoration on screen rotate.
 */
class ImageViewState : Serializable {

    private var scale: Float = 0F

    private var centerX: Float = 0F

    private var centerY: Float = 0F

    private var orientation: Int = 0

    constructor(scale: Float, center: PointF, orientation: Int) {
        this.scale = scale
        this.centerX = center.x
        this.centerY = center.y
        this.orientation = orientation
    }

    fun getScale(): Float = scale

    fun getCenter(): PointF = PointF(centerX, centerY)

    fun getOrientation(): Int = orientation
}
