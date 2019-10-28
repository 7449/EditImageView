package com.davemorrissey.labs.subscaleview.temp

import android.graphics.PointF

import com.davemorrissey.labs.subscaleview.temp.listener.OnAnimationEventListener
import com.davemorrissey.labs.subscaleview.temp.ViewValues

class Anim {
    var scaleStart: Float = 0.toFloat() // Scale at start of anim
    var scaleEnd: Float = 0.toFloat() // Scale at end of anim (target)
    var sCenterStart: PointF? = null // Source center point at start
    var sCenterEnd: PointF? = null // Source center point at end, adjusted for pan limits
    var sCenterEndRequested: PointF? = null // Source center point that was requested, without adjustment
    var vFocusStart: PointF? = null // View point that was double tapped
    var vFocusEnd: PointF? = null // Where the view focal point should be moved to during the anim
    var duration: Long = 500 // How long the anim takes
    var interruptible = true // Whether the anim can be interrupted by a touch
    var easing = ViewValues.EASE_IN_OUT_QUAD // Easing style
    var origin = ViewValues.ORIGIN_ANIM // Animation origin (API, double tap or fling)
    var time = System.currentTimeMillis() // Start time
    var listener: OnAnimationEventListener? = null // Event listener
}