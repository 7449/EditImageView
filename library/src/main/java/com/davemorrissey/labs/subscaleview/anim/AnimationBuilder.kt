package com.davemorrissey.labs.subscaleview.anim

import android.graphics.PointF
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.davemorrissey.labs.subscaleview.ViewValues
import com.davemorrissey.labs.subscaleview.api.*
import com.davemorrissey.labs.subscaleview.api.limitedSCenter
import com.davemorrissey.labs.subscaleview.api.limitedScale
import com.davemorrissey.labs.subscaleview.listener.OnAnimationEventListener

/**
 * Builder class used to set additional options for a scale animation. Create an instance using [],
 * then set your options and call [.start].
 */
class AnimationBuilder {

    private val targetScale: Float
    private val targetSCenter: PointF?
    private val vFocus: PointF?
    private var duration: Long = 500
    private var easing = ViewValues.EASE_IN_OUT_QUAD
    private var origin = ViewValues.ORIGIN_ANIM
    private var interruptible = true
    private var panLimited = true
    private var scaleImageView: SubsamplingScaleImageView
    private var listener: OnAnimationEventListener? = null

    constructor(scaleImageView: SubsamplingScaleImageView, sCenter: PointF) {
        this.scaleImageView = scaleImageView
        this.targetScale = scaleImageView.scale
        this.targetSCenter = sCenter
        this.vFocus = null
    }

    constructor(scaleImageView: SubsamplingScaleImageView, scale: Float) {
        this.scaleImageView = scaleImageView
        this.targetScale = scale
        this.targetSCenter = scaleImageView.getCenter()
        this.vFocus = null
    }

    constructor(scaleImageView: SubsamplingScaleImageView, scale: Float, sCenter: PointF) {
        this.scaleImageView = scaleImageView
        this.targetScale = scale
        this.targetSCenter = sCenter
        this.vFocus = null
    }

    constructor(scaleImageView: SubsamplingScaleImageView, scale: Float, sCenter: PointF, vFocus: PointF) {
        this.scaleImageView = scaleImageView
        this.targetScale = scale
        this.targetSCenter = sCenter
        this.vFocus = vFocus
    }

    /**
     * Desired duration of the anim in milliseconds. Default is 500.
     *
     * @param duration duration in milliseconds.
     * @return this builder for method chaining.
     */
    fun withDuration(duration: Long): AnimationBuilder {
        this.duration = duration
        return this
    }

    /**
     * Whether the animation can be interrupted with a touch. Default is true.
     *
     * @param interruptible interruptible flag.
     * @return this builder for method chaining.
     */
    fun withInterruptible(interruptible: Boolean): AnimationBuilder {
        this.interruptible = interruptible
        return this
    }

    /**
     * Set the easing style. See static fields. [ViewValues.EASE_IN_OUT_QUAD] is recommended, and the default.
     *
     * @param easing easing style.
     * @return this builder for method chaining.
     */
    fun withEasing(easing: Int): AnimationBuilder {
        require(ViewValues.VALID_EASING_STYLES.contains(easing)) { "Unknown easing type: $easing" }
        this.easing = easing
        return this
    }

    /**
     * Add an animation event listener.
     *
     * @param listener The listener.
     * @return this builder for method chaining.
     */
    fun withOnAnimationEventListener(listener: OnAnimationEventListener): AnimationBuilder {
        this.listener = listener
        return this
    }

    /**
     * Only for internal use. When set to true, the animation proceeds towards the actual end point - the nearest
     * point to the center allowed by pan limits. When false, animation is in the direction of the requested end
     * point and is stopped when the limit for each axis is reached. The latter behaviour is used for flings but
     * nothing else.
     */
    fun withPanLimited(panLimited: Boolean): AnimationBuilder {
        this.panLimited = panLimited
        return this
    }

    /**
     * Only for internal use. Indicates what caused the animation.
     */
    fun withOrigin(origin: Int): AnimationBuilder {
        this.origin = origin
        return this
    }

    /**
     * Starts the animation.
     */
    fun start() {
        var anim = scaleImageView.anim
        anim?.listener?.onInterruptedByNewAnim()

        val vxCenter = scaleImageView.paddingLeft + (scaleImageView.width - scaleImageView.paddingRight - scaleImageView.paddingLeft) / 2
        val vyCenter = scaleImageView.paddingTop + (scaleImageView.height - scaleImageView.paddingBottom - scaleImageView.paddingTop) / 2
        val targetScale = scaleImageView.limitedScale(this.targetScale)
        val targetSCenter = if (panLimited) scaleImageView.limitedSCenter(
                this.targetSCenter?.x ?: 0F,
                this.targetSCenter?.y ?: 0F,
                targetScale,
                PointF()) else this.targetSCenter
        anim = Anim()
        anim.scaleStart = scaleImageView.scale
        anim.scaleEnd = targetScale
        anim.time = System.currentTimeMillis()
        anim.sCenterEndRequested = targetSCenter
        anim.sCenterStart = scaleImageView.getCenter()
        anim.sCenterEnd = targetSCenter
        anim.vFocusStart = scaleImageView.sourceToViewCoord(targetSCenter ?: PointF())
        anim.vFocusEnd = PointF(vxCenter.toFloat(), vyCenter.toFloat())
        anim.duration = duration
        anim.interruptible = interruptible
        anim.easing = easing
        anim.origin = origin
        anim.time = System.currentTimeMillis()
        anim.listener = listener
        scaleImageView.anim = anim
        vFocus?.let {
            // Calculate where translation will be at the end of the anim
            val vTranslateXEnd = vFocus.x - targetScale * (anim.sCenterStart?.x ?: 0F)
            val vTranslateYEnd = vFocus.y - targetScale * (anim.sCenterStart?.y ?: 0F)
            val satEnd = ScaleAndTranslate(targetScale, PointF(vTranslateXEnd, vTranslateYEnd))
            // Fit the end translation into bounds
            scaleImageView.fitToBounds(true, satEnd)
            // Adjust the position of the focus point at end so image will be in bounds
            anim.vFocusEnd = PointF(vFocus.x + (satEnd.vTranslate.x - vTranslateXEnd), vFocus.y + (satEnd.vTranslate.y - vTranslateYEnd))
        }
        scaleImageView.invalidate()
    }
}

