package com.davemorrissey.labs.subscaleview.temp

import android.view.MotionEvent
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView.Companion.MESSAGE_LONG_CLICK
import com.davemorrissey.labs.subscaleview.task.safeLet
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Suppress("DEPRECATION")
internal fun SubsamplingScaleImageView.onTouchEventInternal(event: MotionEvent): Boolean {
    val touchCount = event.pointerCount
    when (event.action) {
        MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_1_DOWN, MotionEvent.ACTION_POINTER_2_DOWN -> {
            anim = null
            requestDisallowInterceptTouchEvent(true)
            maxTouchCount = max(maxTouchCount, touchCount)
            if (touchCount >= 2) {
                if (zoomEnabled) {
                    // Start pinch to zoom. Calculate distance between touch points and center point of the pinch.
                    val distance = distance(event.getX(0), event.getX(1), event.getY(0), event.getY(1))
                    scaleStart = scale
                    vDistStart = distance
                    vTranslate?.let { vTranslate -> vTranslateStart?.set(vTranslate.x, vTranslate.y) }
                    vCenterStart?.set((event.getX(0) + event.getX(1)) / 2, (event.getY(0) + event.getY(1)) / 2)
                } else {
                    // Abort all gestures on second touch
                    maxTouchCount = 0
                }
                // Cancel long click timer
                clickHandler.removeMessages(MESSAGE_LONG_CLICK)
            } else if (!isQuickScaling) {
                // Start one-finger pan
                vTranslate?.let { vTranslate -> vTranslateStart?.set(vTranslate.x, vTranslate.y) }
                vCenterStart?.set(event.x, event.y)

                // Start long click timer
                clickHandler.sendEmptyMessageDelayed(MESSAGE_LONG_CLICK, 600)
            }
            return true
        }
        MotionEvent.ACTION_MOVE -> {
            var consumed = false
            if (maxTouchCount > 0) {
                if (touchCount >= 2) {
                    // Calculate new distance between touch points, to scale and pan relative to start values.
                    val vDistEnd = distance(event.getX(0), event.getX(1), event.getY(0), event.getY(1))
                    val vCenterEndX = (event.getX(0) + event.getX(1)) / 2
                    val vCenterEndY = (event.getY(0) + event.getY(1)) / 2

                    vCenterStart?.let { vCenterStart ->
                        if (zoomEnabled && (distance(vCenterStart.x, vCenterEndX, vCenterStart.y, vCenterEndY) > 5 || abs(vDistEnd - vDistStart) > 5 || isPanning)) {
                            isZooming = true
                            isPanning = true
                            consumed = true

                            val previousScale = scale.toDouble()
                            scale = min(maxScale, vDistEnd / vDistStart * scaleStart)

                            if (scale <= minScale()) {
                                // Minimum scale reached so don't pan. Adjust start settings so any expand will zoom in.
                                vDistStart = vDistEnd
                                scaleStart = minScale()
                                vCenterStart.set(vCenterEndX, vCenterEndY)
                                vTranslate?.let { vTranslate -> vTranslateStart?.set(vTranslate) }
                            } else if (panEnabled) {
                                // Translate to place the source image coordinate that was at the center of the pinch at the start
                                // at the center of the pinch now, to give simultaneous pan + zoom.
                                val vLeftStart = vCenterStart.x - (vTranslateStart?.x ?: 0F)
                                val vTopStart = vCenterStart.y - (vTranslateStart?.y ?: 0F)
                                val vLeftNow = vLeftStart * (scale / scaleStart)
                                val vTopNow = vTopStart * (scale / scaleStart)
                                vTranslate?.x = vCenterEndX - vLeftNow
                                vTranslate?.y = vCenterEndY - vTopNow
                                if (previousScale * sHeight() < height && scale * sHeight() >= height || previousScale * sWidth() < width && scale * sWidth() >= width) {
                                    fitToBounds(true)
                                    vCenterStart.set(vCenterEndX, vCenterEndY)
                                    vTranslate?.let { vTranslate -> vTranslateStart?.set(vTranslate) }
                                    scaleStart = scale
                                    vDistStart = vDistEnd
                                }
                            } else if (sRequestedCenter != null) {
                                // With a center specified from code, zoom around that point.
                                vTranslate?.x = width / 2 - scale * (sRequestedCenter?.x ?: 0F)
                                vTranslate?.y = height / 2 - scale * (sRequestedCenter?.y ?: 0F)
                            } else {
                                // With no requested center, scale around the image center.
                                vTranslate?.x = width / 2 - scale * (sWidth() / 2)
                                vTranslate?.y = height / 2 - scale * (sHeight() / 2)
                            }

                            fitToBounds(true)
                            refreshRequiredTiles(eagerLoadingEnabled)
                        }
                    }
                } else if (isQuickScaling) {

                    safeLet(quickScaleVStart,
                            quickScaleVLastPoint,
                            vCenterStart,
                            vTranslateStart,
                            quickScaleSCenter,
                            vTranslate) { quickScaleVStart, quickScaleVLastPoint, vCenterStart, vTranslateStart, quickScaleSCenter, vTranslate ->

                        // One finger zoom
                        // Stole Google's Magical Formula™ to make sure it feels the exact same
                        var dist = abs(quickScaleVStart.y - event.y) * 2 + quickScaleThreshold

                        if (quickScaleLastDistance == -1f) {
                            quickScaleLastDistance = dist
                        }
                        val isUpwards = event.y > quickScaleVLastPoint.y
                        quickScaleVLastPoint.set(0f, event.y)

                        val spanDiff = abs(1 - dist / quickScaleLastDistance) * 0.5f

                        if (spanDiff > 0.03f || quickScaleMoved) {
                            quickScaleMoved = true

                            var multiplier = 1f
                            if (quickScaleLastDistance > 0) {
                                multiplier = if (isUpwards) 1 + spanDiff else 1 - spanDiff
                            }

                            val previousScale = scale.toDouble()
                            scale = max(minScale(), min(maxScale, scale * multiplier))

                            if (panEnabled) {
                                val vLeftStart = vCenterStart.x - vTranslateStart.x
                                val vTopStart = vCenterStart.y - vTranslateStart.y
                                val vLeftNow = vLeftStart * (scale / scaleStart)
                                val vTopNow = vTopStart * (scale / scaleStart)
                                vTranslate.x = vCenterStart.x - vLeftNow
                                vTranslate.y = vCenterStart.y - vTopNow
                                if (previousScale * sHeight() < height && scale * sHeight() >= height || previousScale * sWidth() < width && scale * sWidth() >= width) {
                                    fitToBounds(true)
                                    sourceToViewCoord(quickScaleSCenter)?.let { vCenterStart.set(it) }
                                    vTranslateStart.set(vTranslate)
                                    scaleStart = scale
                                    dist = 0f
                                }
                            } else if (sRequestedCenter != null) {
                                // With a center specified from code, zoom around that point.
                                vTranslate.x = width / 2 - scale * (sRequestedCenter?.x ?: 0F)
                                vTranslate.y = height / 2 - scale * (sRequestedCenter?.y ?: 0F)
                            } else {
                                // With no requested center, scale around the image center.
                                vTranslate.x = width / 2 - scale * (sWidth() / 2)
                                vTranslate.y = height / 2 - scale * (sHeight() / 2)
                            }
                        }
                        quickScaleLastDistance = dist
                    }
                    fitToBounds(true)
                    refreshRequiredTiles(eagerLoadingEnabled)
                    consumed = true
                } else if (!isZooming) {

                    safeLet(vCenterStart, vTranslate, vTranslateStart) { vCenterStart, vTranslate, vTranslateStart ->
                        // One finger pan - translate the image. We do this calculation even with pan disabled so click
                        // and long click behaviour is preserved.
                        val dx = abs(event.x - vCenterStart.x)
                        val dy = abs(event.y - vCenterStart.y)

                        //On the Samsung S6 long click event does not work, because the dx > 5 usually true
                        val offset = density * 5
                        if (dx > offset || dy > offset || isPanning) {
                            consumed = true
                            vTranslate.x = vTranslateStart.x + (event.x - vCenterStart.x)
                            vTranslate.y = vTranslateStart.y + (event.y - vCenterStart.y)

                            val lastX = vTranslate.x
                            val lastY = vTranslate.y
                            fitToBounds(true)
                            val atXEdge = lastX != vTranslate.x
                            val atYEdge = lastY != vTranslate.y
                            val edgeXSwipe = atXEdge && dx > dy && !isPanning
                            val edgeYSwipe = atYEdge && dy > dx && !isPanning
                            val yPan = lastY == vTranslate.y && dy > offset * 3
                            if (!edgeXSwipe && !edgeYSwipe && (!atXEdge || !atYEdge || yPan || isPanning)) {
                                isPanning = true
                            } else if (dx > offset || dy > offset) {
                                // Haven't panned the image, and we're at the left or right edge. Switch to page swipe.
                                maxTouchCount = 0
                                clickHandler.removeMessages(MESSAGE_LONG_CLICK)
                                requestDisallowInterceptTouchEvent(false)
                            }
                            if (!panEnabled) {
                                vTranslate.x = vTranslateStart.x
                                vTranslate.y = vTranslateStart.y
                                requestDisallowInterceptTouchEvent(false)
                            }
                        }
                        refreshRequiredTiles(eagerLoadingEnabled)
                    }
                }
            }
            if (consumed) {
                clickHandler.removeMessages(MESSAGE_LONG_CLICK)
                invalidate()
                return true
            }
        }
        MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_POINTER_2_UP -> {
            clickHandler.removeMessages(MESSAGE_LONG_CLICK)
            if (isQuickScaling) {
                isQuickScaling = false
                if (!quickScaleMoved) {
                    safeLet(quickScaleSCenter, vCenterStart) { quickScaleSCenter, vCenterStart -> doubleTapZoom(quickScaleSCenter, vCenterStart) }
                }
            }
            if (maxTouchCount > 0 && (isZooming || isPanning)) {
                if (isZooming && touchCount == 2) {
                    // Convert from zoom to pan with remaining touch
                    isPanning = true
                    vTranslate?.let { vTranslate -> vTranslateStart?.set(vTranslate.x, vTranslate.y) }
                    if (event.actionIndex == 1) {
                        vCenterStart?.set(event.getX(0), event.getY(0))
                    } else {
                        vCenterStart?.set(event.getX(1), event.getY(1))
                    }
                }
                if (touchCount < 3) {
                    // End zooming when only one touch point
                    isZooming = false
                }
                if (touchCount < 2) {
                    // End panning when no touch points
                    isPanning = false
                    maxTouchCount = 0
                }
                // Trigger load of tiles now required
                refreshRequiredTiles(true)
                return true
            }
            if (touchCount == 1) {
                isZooming = false
                isPanning = false
                maxTouchCount = 0
            }
            return true
        }
    }
    return false
}
