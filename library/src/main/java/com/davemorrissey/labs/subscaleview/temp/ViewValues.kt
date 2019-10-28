package com.davemorrissey.labs.subscaleview.temp

object ViewValues {
    /**
     * Attempt to use EXIF information on the image to rotate it. Works for external files only.
     */
    const val ORIENTATION_USE_EXIF = -1
    /**
     * Display the image file in its native orientation.
     */
    const val ORIENTATION_0 = 0
    /**
     * Rotate the image 90 degrees clockwise.
     */
    const val ORIENTATION_90 = 90
    /**
     * Rotate the image 180 degrees.
     */
    const val ORIENTATION_180 = 180
    /**
     * Rotate the image 270 degrees clockwise.
     */
    const val ORIENTATION_270 = 270
    /**
     * During zoom animation, keep the point of the image that was tapped in the same place, and scale the image around it.
     */
    const val ZOOM_FOCUS_FIXED = 1
    /**
     * During zoom animation, move the point of the image that was tapped to the center of the screen.
     */
    const val ZOOM_FOCUS_CENTER = 2
    /**
     * Zoom in to and center the tapped point immediately without animating.
     */
    const val ZOOM_FOCUS_CENTER_IMMEDIATE = 3
    /**
     * Quadratic ease out. Not recommended for scale animation, but good for panning.
     */
    const val EASE_OUT_QUAD = 1
    /**
     * Quadratic ease in and out.
     */
    const val EASE_IN_OUT_QUAD = 2
    /**
     * Don't allow the image to be panned off screen. As much of the image as possible is always displayed, centered in the view when it is smaller. This is the best option for galleries.
     */
    const val PAN_LIMIT_INSIDE = 1
    /**
     * Allows the image to be panned until it is just off screen, but no further. The edge of the image will stop when it is flush with the screen edge.
     */
    const val PAN_LIMIT_OUTSIDE = 2
    /**
     * Allows the image to be panned until a corner reaches the center of the screen but no further. Useful when you want to pan any spot on the image to the exact center of the screen.
     */
    const val PAN_LIMIT_CENTER = 3
    /**
     * Scale the image so that both dimensions of the image will be equal to or less than the corresponding dimension of the view. The image is then centered in the view. This is the default behaviour and best for galleries.
     */
    const val SCALE_TYPE_CENTER_INSIDE = 1
    /**
     * Scale the image uniformly so that both dimensions of the image will be equal to or larger than the corresponding dimension of the view. The image is then centered in the view.
     */
    const val SCALE_TYPE_CENTER_CROP = 2
    /**
     * Scale the image so that both dimensions of the image will be equal to or less than the maxScale and equal to or larger than minScale. The image is then centered in the view.
     */
    const val SCALE_TYPE_CUSTOM = 3
    /**
     * Scale the image so that both dimensions of the image will be equal to or larger than the corresponding dimension of the view. The top left is shown.
     */
    const val SCALE_TYPE_START = 4
    /**
     * State change originated from animation.
     */
    const val ORIGIN_ANIM = 1
    /**
     * State change originated from touch gesture.
     */
    const val ORIGIN_TOUCH = 2
    /**
     * State change originated from a fling momentum anim.
     */
    const val ORIGIN_FLING = 3
    /**
     * State change originated from a double tap zoom anim.
     */
    const val ORIGIN_DOUBLE_TAP_ZOOM = 4

    val VALID_ORIENTATIONS = listOf(ORIENTATION_0, ORIENTATION_90, ORIENTATION_180, ORIENTATION_270, ORIENTATION_USE_EXIF)
    val VALID_ZOOM_STYLES = listOf(ZOOM_FOCUS_FIXED, ZOOM_FOCUS_CENTER, ZOOM_FOCUS_CENTER_IMMEDIATE)
    val VALID_EASING_STYLES = listOf(EASE_IN_OUT_QUAD, EASE_OUT_QUAD)
    val VALID_PAN_LIMITS = listOf(PAN_LIMIT_INSIDE, PAN_LIMIT_OUTSIDE, PAN_LIMIT_CENTER)
    val VALID_SCALE_TYPES = listOf(SCALE_TYPE_CENTER_CROP, SCALE_TYPE_CENTER_INSIDE, SCALE_TYPE_CUSTOM, SCALE_TYPE_START)
}