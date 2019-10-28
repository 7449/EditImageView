package com.davemorrissey.labs.subscaleview.core;

import android.graphics.PointF;
import android.util.Log;

import androidx.annotation.NonNull;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageViewXKt;
import com.davemorrissey.labs.subscaleview.listener.OnAnimationEventListener;

import static com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView.TAG;

/**
 * Builder class used to set additional options for a scale animation. Create an instance using {@link SubsamplingScaleImageView#animateScale(float)},
 * then set your options and call {@link #start()}.
 */
public final class AnimationBuilder {

    private final float targetScale;
    private final PointF targetSCenter;
    private final PointF vFocus;
    private long duration = 500;
    private int easing = ViewValues.EASE_IN_OUT_QUAD;
    private int origin = ViewValues.ORIGIN_ANIM;
    private boolean interruptible = true;
    private boolean panLimited = true;
    private OnAnimationEventListener listener;
    private SubsamplingScaleImageView scaleImageView;

    public AnimationBuilder(SubsamplingScaleImageView scaleImageView, PointF sCenter) {
        this.scaleImageView = scaleImageView;
        this.targetScale = scaleImageView.scale;
        this.targetSCenter = sCenter;
        this.vFocus = null;
    }

    public AnimationBuilder(SubsamplingScaleImageView scaleImageView, float scale) {
        this.scaleImageView = scaleImageView;
        this.targetScale = scale;
        this.targetSCenter = SubsamplingScaleImageViewXKt.getCenter(scaleImageView);
        this.vFocus = null;
    }

    public AnimationBuilder(SubsamplingScaleImageView scaleImageView, float scale, PointF sCenter) {
        this.scaleImageView = scaleImageView;
        this.targetScale = scale;
        this.targetSCenter = sCenter;
        this.vFocus = null;
    }

    public AnimationBuilder(SubsamplingScaleImageView scaleImageView, float scale, PointF sCenter, PointF vFocus) {
        this.scaleImageView = scaleImageView;
        this.targetScale = scale;
        this.targetSCenter = sCenter;
        this.vFocus = vFocus;
    }

    /**
     * Desired duration of the anim in milliseconds. Default is 500.
     *
     * @param duration duration in milliseconds.
     * @return this builder for method chaining.
     */
    @NonNull
    public AnimationBuilder withDuration(long duration) {
        this.duration = duration;
        return this;
    }

    /**
     * Whether the animation can be interrupted with a touch. Default is true.
     *
     * @param interruptible interruptible flag.
     * @return this builder for method chaining.
     */
    @NonNull
    public AnimationBuilder withInterruptible(boolean interruptible) {
        this.interruptible = interruptible;
        return this;
    }

    /**
     * Set the easing style. See static fields. {@link ViewValues#EASE_IN_OUT_QUAD} is recommended, and the default.
     *
     * @param easing easing style.
     * @return this builder for method chaining.
     */
    @NonNull
    public AnimationBuilder withEasing(int easing) {
        if (!ViewValues.INSTANCE.getVALID_EASING_STYLES().contains(easing)) {
            throw new IllegalArgumentException("Unknown easing type: " + easing);
        }
        this.easing = easing;
        return this;
    }

    /**
     * Add an animation event listener.
     *
     * @param listener The listener.
     * @return this builder for method chaining.
     */
    @NonNull
    public AnimationBuilder withOnAnimationEventListener(OnAnimationEventListener listener) {
        this.listener = listener;
        return this;
    }

    /**
     * Only for internal use. When set to true, the animation proceeds towards the actual end point - the nearest
     * point to the center allowed by pan limits. When false, animation is in the direction of the requested end
     * point and is stopped when the limit for each axis is reached. The latter behaviour is used for flings but
     * nothing else.
     */
    @NonNull
    public AnimationBuilder withPanLimited(boolean panLimited) {
        this.panLimited = panLimited;
        return this;
    }

    /**
     * Only for internal use. Indicates what caused the animation.
     */
    @NonNull
    public AnimationBuilder withOrigin(int origin) {
        this.origin = origin;
        return this;
    }

    /**
     * Starts the animation.
     */
    public void start() {
        Anim anim = scaleImageView.anim;
        if (anim != null && anim.getListener() != null) {
            try {
                anim.getListener().onInterruptedByNewAnim();
            } catch (Exception e) {
                Log.w(TAG, "Error thrown by animation listener", e);
            }
        }
        int vxCenter = scaleImageView.getPaddingLeft() + (scaleImageView.getWidth() - scaleImageView.getPaddingRight() - scaleImageView.getPaddingLeft()) / 2;
        int vyCenter = scaleImageView.getPaddingTop() + (scaleImageView.getHeight() - scaleImageView.getPaddingBottom() - scaleImageView.getPaddingTop()) / 2;
        float targetScale = scaleImageView.limitedScale(this.targetScale);
        PointF targetSCenter = panLimited ? scaleImageView.limitedSCenter(this.targetSCenter.x, this.targetSCenter.y, targetScale, new PointF()) : this.targetSCenter;
        anim = new Anim();
        anim.setScaleStart(scaleImageView.scale);
        anim.setScaleEnd(targetScale);
        anim.setTime(System.currentTimeMillis());
        anim.setSCenterEndRequested(targetSCenter);
        anim.setSCenterStart(SubsamplingScaleImageViewXKt.getCenter(scaleImageView));
        anim.setSCenterEnd(targetSCenter);
        anim.setVFocusStart(SubsamplingScaleImageViewXKt.sourceToViewCoord(scaleImageView, targetSCenter));
        anim.setVFocusEnd(new PointF(
                vxCenter,
                vyCenter
        ));
        anim.setDuration(duration);
        anim.setInterruptible(interruptible);
        anim.setEasing(easing);
        anim.setOrigin(origin);
        anim.setTime(System.currentTimeMillis());
        anim.setListener(listener);
        scaleImageView.anim = anim;
        if (vFocus != null) {
            // Calculate where translation will be at the end of the anim
            float vTranslateXEnd = vFocus.x - (targetScale * anim.getSCenterStart().x);
            float vTranslateYEnd = vFocus.y - (targetScale * anim.getSCenterStart().y);
            ScaleAndTranslate satEnd = new ScaleAndTranslate(targetScale, new PointF(vTranslateXEnd, vTranslateYEnd));
            // Fit the end translation into bounds
            scaleImageView.fitToBounds(true, satEnd);
            // Adjust the position of the focus point at end so image will be in bounds
            anim.setVFocusEnd(new PointF(
                    vFocus.x + (satEnd.getVTranslate().x - vTranslateXEnd),
                    vFocus.y + (satEnd.getVTranslate().y - vTranslateYEnd)
            ));
        }
        scaleImageView.invalidate();
    }

}

