package com.davemorrissey.labs.subscaleview.anim

import android.graphics.PointF
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.davemorrissey.labs.subscaleview.api.isReady

/**
 * Creates a panning animation builder, that when started will animate the image to place the given coordinates of
 * the image in the center of the screen. If doing this would move the image beyond the edges of the screen, the
 * image is instead animated to move the center point as near to the center of the screen as is allowed - it's
 * guaranteed to be on screen.
 *
 * @param sCenter Target center point
 * @return [AnimationBuilder] instance. Call [AnimationBuilder.start] to start the anim.
 */
fun SubsamplingScaleImageView.animateCenter(sCenter: PointF): AnimationBuilder? {
    return if (!isReady()) {
        null
    } else AnimationBuilder(this, sCenter)
}

/**
 * Creates a scale animation builder, that when started will animate a zoom in or out. If this would move the image
 * beyond the panning limits, the image is automatically panned during the animation.
 *
 * @param scale Target scale.
 * @return [AnimationBuilder] instance. Call [AnimationBuilder.start] to start the anim.
 */
fun SubsamplingScaleImageView.animateScale(scale: Float): AnimationBuilder? {
    return if (!isReady()) {
        null
    } else AnimationBuilder(this, scale)
}

/**
 * Creates a scale animation builder, that when started will animate a zoom in or out. If this would move the image
 * beyond the panning limits, the image is automatically panned during the animation.
 *
 * @param scale   Target scale.
 * @param sCenter Target source center.
 * @return [AnimationBuilder] instance. Call [AnimationBuilder.start] to start the anim.
 */
fun SubsamplingScaleImageView.animateScaleAndCenter(scale: Float, sCenter: PointF): AnimationBuilder? {
    return if (!isReady()) {
        null
    } else AnimationBuilder(this, scale, sCenter)
}