package com.davemorrissey.labs.subscaleview.gesture

import android.graphics.PointF
import android.view.GestureDetector
import android.view.MotionEvent
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.davemorrissey.labs.subscaleview.ViewValues
import com.davemorrissey.labs.subscaleview.anim.AnimationBuilder
import com.davemorrissey.labs.subscaleview.api.doubleTapZoom
import com.davemorrissey.labs.subscaleview.api.setGestureDetector
import com.davemorrissey.labs.subscaleview.api.viewToSourceCoord
import kotlin.math.abs

class SingleDetectorListener(private val scaleImageView: SubsamplingScaleImageView) : GestureDetector.SimpleOnGestureListener() {
    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
        scaleImageView.performClick()
        return true
    }
}

class DetectorListener(private val scaleImageView: SubsamplingScaleImageView) : GestureDetector.SimpleOnGestureListener() {

    override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
        if (scaleImageView.panEnabled &&
                scaleImageView.readySent &&
                scaleImageView.vTranslate != null &&
                e1 != null &&
                e2 != null &&
                (abs(e1.x - e2.x) > 50 || abs(e1.y - e2.y) > 50) &&
                (abs(velocityX) > 500 || abs(velocityY) > 500) &&
                !scaleImageView.isZooming) {

            val vTranslateEnd = PointF(
                    scaleImageView.vTranslate?.x ?: 0 + velocityX * 0.25f,
                    scaleImageView.vTranslate?.y ?: 0 + velocityY * 0.25f)
            val sCenterXEnd = (scaleImageView.width / 2 - vTranslateEnd.x) / scaleImageView.scale
            val sCenterYEnd = (scaleImageView.height / 2 - vTranslateEnd.y) / scaleImageView.scale
            AnimationBuilder(scaleImageView, PointF(sCenterXEnd, sCenterYEnd)).withEasing(ViewValues.EASE_OUT_QUAD).withPanLimited(false).withOrigin(ViewValues.ORIGIN_FLING).start()
            return true
        }
        return super.onFling(e1, e2, velocityX, velocityY)
    }

    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
        scaleImageView.performClick()
        return true
    }

    override fun onDoubleTap(e: MotionEvent): Boolean {
        if (scaleImageView.zoomEnabled && scaleImageView.readySent && scaleImageView.vTranslate != null) {
            // Hacky solution for #15 - after a double tap the GestureDetector gets in a state
            // where the next fling is ignored, so here we replace it with a new one.
            scaleImageView.setGestureDetector(scaleImageView.context)
            if (scaleImageView.quickScaleEnabled) {
                // Store quick scale params. This will become either a double tap zoom or a
                // quick scale depending on whether the user swipes.
                scaleImageView.vCenterStart = PointF(e.x, e.y)
                scaleImageView.vTranslateStart = PointF(
                        scaleImageView.vTranslate?.x ?: 0F,
                        scaleImageView.vTranslate?.y ?: 0F)
                scaleImageView.scaleStart = scaleImageView.scale
                scaleImageView.isQuickScaling = true
                scaleImageView.isZooming = true
                scaleImageView.quickScaleLastDistance = -1f
                scaleImageView.quickScaleSCenter = scaleImageView.viewToSourceCoord(scaleImageView.vCenterStart
                        ?: PointF())
                scaleImageView.quickScaleVStart = PointF(e.x, e.y)
                scaleImageView.quickScaleVLastPoint = PointF(
                        scaleImageView.quickScaleSCenter?.x ?: 0F,
                        scaleImageView.quickScaleSCenter?.y ?: 0F)
                scaleImageView.quickScaleMoved = false
                // We need to get events in onTouchEvent after this.
                return false
            } else {
                // Start double tap zoom animation.
                scaleImageView.viewToSourceCoord(PointF(e.x, e.y))?.let { scaleImageView.doubleTapZoom(it, PointF(e.x, e.y)) }
                return true
            }
        }
        return super.onDoubleTapEvent(e)
    }

}