package com.davemorrissey.labs.subscaleview.listener

import android.graphics.PointF

/**
 * An event listener, allowing activities to be notified of pan and zoom events. Initialisation
 * and calls made by your code do not trigger events; touch events and animations do. Methods in
 * this listener will be called on the UI thread and may be called very frequently - your
 * implementation should return quickly.
 */
interface OnStateChangedListener {

    /**
     * The scale has changed. Use with [.getMaxScale] and [.getMinScale] to determine
     * whether the image is fully zoomed in or out.
     *
     * @param newScale The new scale.
     * @param origin   Where the event originated from - one of [.ORIGIN_ANIM], [.ORIGIN_TOUCH].
     */
    fun onScaleChanged(newScale: Float, origin: Int)

    /**
     * The source center has been changed. This can be a result of panning or zooming.
     *
     * @param newCenter The new source center point.
     * @param origin    Where the event originated from - one of [.ORIGIN_ANIM], [.ORIGIN_TOUCH].
     */
    fun onCenterChanged(newCenter: PointF, origin: Int)


    /**
     * Default implementation of [OnStateChangedListener]. This does nothing in any method.
     */
    open class DefaultOnStateChangedListener : OnStateChangedListener {

        override fun onCenterChanged(newCenter: PointF, origin: Int) {}

        override fun onScaleChanged(newScale: Float, origin: Int) {}

    }

}