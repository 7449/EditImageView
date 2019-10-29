package com.davemorrissey.labs.subscaleview.api

import android.content.Context
import android.graphics.*
import android.os.AsyncTask
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.annotation.AnyThread
import com.davemorrissey.labs.subscaleview.ImageViewState
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.davemorrissey.labs.subscaleview.ViewValues
import com.davemorrissey.labs.subscaleview.anim.AnimationBuilder
import com.davemorrissey.labs.subscaleview.anim.ScaleAndTranslate
import com.davemorrissey.labs.subscaleview.decoder.ImageRegionDecoder
import com.davemorrissey.labs.subscaleview.gesture.DetectorListener
import com.davemorrissey.labs.subscaleview.gesture.SingleDetectorListener
import com.davemorrissey.labs.subscaleview.task.BitmapLoadTask
import com.davemorrissey.labs.subscaleview.task.Tile
import com.davemorrissey.labs.subscaleview.task.TileLoadTask
import com.davemorrissey.labs.subscaleview.task.safeLet
import kotlin.math.*

/**
 * Reset all state before setting/changing image or setting new rotation.
 */
internal fun SubsamplingScaleImageView.reset(newImage: Boolean) {
    debug("reset newImage=$newImage")
    scale = 0f
    scaleStart = 0f
    vTranslate = null
    vTranslateStart = null
    vTranslateBefore = null
    pendingScale = 0f
    sPendingCenter = null
    sRequestedCenter = null
    isZooming = false
    isPanning = false
    isQuickScaling = false
    maxTouchCount = 0
    fullImageSampleSize = 0
    vCenterStart = null
    vDistStart = 0f
    quickScaleLastDistance = 0f
    quickScaleMoved = false
    quickScaleSCenter = null
    quickScaleVLastPoint = null
    quickScaleVStart = null
    anim = null
    satTemp = null
    supportMatrix = null
    sRect = null
    if (newImage) {
        uri = null
        decoderLock.writeLock().lock()
        try {
            decoder?.recycle()
            decoder = null
        } finally {
            decoderLock.writeLock().unlock()
        }
        if (bitmap != null && !bitmapIsCached) {
            bitmap?.recycle()
        }
        if (bitmap != null && bitmapIsCached && onImageEventListener != null) {
            onImageEventListener?.onPreviewReleased()
        }
        sWidth = 0
        sHeight = 0
        sOrientation = 0
        sRegion = null
        pRegion = null
        readySent = false
        imageLoadedSent = false
        bitmap = null
        bitmapIsPreview = false
        bitmapIsCached = false
    }
    tileMap?.let {
        for (tileMapEntry in it) {
            for (tile in tileMapEntry.value) {
                tile.visible = false
                tile.bitmap?.recycle()
                tile.bitmap = null
            }
        }
        tileMap = null
    }
    setGestureDetector(context)
}

internal fun SubsamplingScaleImageView.setGestureDetector(context: Context) {
    detector = GestureDetector(context, DetectorListener(this))
    singleDetector = GestureDetector(context, SingleDetectorListener(this))
}

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
                clickHandler.removeMessages(SubsamplingScaleImageView.MESSAGE_LONG_CLICK)
            } else if (!isQuickScaling) {
                // Start one-finger pan
                vTranslate?.let { vTranslate -> vTranslateStart?.set(vTranslate.x, vTranslate.y) }
                vCenterStart?.set(event.x, event.y)

                // Start long click timer
                clickHandler.sendEmptyMessageDelayed(SubsamplingScaleImageView.MESSAGE_LONG_CLICK, 600)
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
                                clickHandler.removeMessages(SubsamplingScaleImageView.MESSAGE_LONG_CLICK)
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
                clickHandler.removeMessages(SubsamplingScaleImageView.MESSAGE_LONG_CLICK)
                invalidate()
                return true
            }
        }
        MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_POINTER_2_UP -> {
            clickHandler.removeMessages(SubsamplingScaleImageView.MESSAGE_LONG_CLICK)
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

internal fun SubsamplingScaleImageView.requestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
    parent?.requestDisallowInterceptTouchEvent(disallowIntercept)
}

/**
 * Double tap zoom handler triggered from gesture detector or on touch, depending on whether
 * quick scale is enabled.
 */
internal fun SubsamplingScaleImageView.doubleTapZoom(sCenter: PointF, vFocus: PointF) {
    if (!panEnabled) {
        if (sRequestedCenter != null) {
            // With a center specified from code, zoom around that point.
            sCenter.x = sRequestedCenter?.x ?: 0F
            sCenter.y = sRequestedCenter?.y ?: 0F
        } else {
            // With no requested center, scale around the image center.
            sCenter.x = (sWidth() / 2).toFloat()
            sCenter.y = (sHeight() / 2).toFloat()
        }
    }
    val doubleTapZoomScale = min(maxScale, doubleTapZoomScale)
    val zoomIn = scale <= doubleTapZoomScale * 0.9 || scale == minScale
    val targetScale = if (zoomIn) doubleTapZoomScale else minScale()
    if (doubleTapZoomStyle == ViewValues.ZOOM_FOCUS_CENTER_IMMEDIATE) {
        setScaleAndCenter(targetScale, sCenter)
    } else if (doubleTapZoomStyle == ViewValues.ZOOM_FOCUS_CENTER || !zoomIn || !panEnabled) {
        AnimationBuilder(this, targetScale, sCenter).withInterruptible(false).withDuration(doubleTapZoomDuration.toLong()).withOrigin(ViewValues.ORIGIN_DOUBLE_TAP_ZOOM).start()
    } else if (doubleTapZoomStyle == ViewValues.ZOOM_FOCUS_FIXED) {
        AnimationBuilder(this, targetScale, sCenter, vFocus).withInterruptible(false).withDuration(doubleTapZoomDuration.toLong()).withOrigin(ViewValues.ORIGIN_DOUBLE_TAP_ZOOM).start()
    }
    invalidate()
}

/**
 * Helper method for setting the values of a tile matrix array.
 */
internal fun setMatrixArray(array: FloatArray, f0: Float, f1: Float, f2: Float, f3: Float, f4: Float, f5: Float, f6: Float, f7: Float) {
    array[0] = f0
    array[1] = f1
    array[2] = f2
    array[3] = f3
    array[4] = f4
    array[5] = f5
    array[6] = f6
    array[7] = f7
}

/**
 * Checks whether the base layer of tiles or full size bitmap is ready.
 */
internal fun SubsamplingScaleImageView.isBaseLayerReady(): Boolean {
    if (bitmap != null && !bitmapIsPreview) {
        return true
    } else {
        tileMap?.let {
            var baseLayerReady = true
            for (tileMapEntry in it.entries) {
                if (tileMapEntry.key == fullImageSampleSize) {
                    for (tile in tileMapEntry.value) {
                        if (tile.loading || tile.bitmap == null) {
                            baseLayerReady = false
                        }
                    }
                }
            }
            return baseLayerReady
        }
    }
    return false
}

/**
 * Check whether view and image dimensions are known and either a preview, full size image or
 * base layer tiles are loaded. First time, send ready event to listener. The next draw will
 * display an image.
 */
internal fun SubsamplingScaleImageView.checkReady(): Boolean {
    val ready = width > 0 && height > 0 && sWidth > 0 && sHeight > 0 && (bitmap != null || isBaseLayerReady())
    if (!readySent && ready) {
        preDraw()
        readySent = true
        onReady()
        onImageEventListener?.onReady()
    }
    return ready
}

/**
 * Check whether either the full size bitmap or base layer tiles are loaded. First time, send image
 * loaded event to listener.
 */
internal fun SubsamplingScaleImageView.checkImageLoaded(): Boolean {
    val imageLoaded = isBaseLayerReady()
    if (!imageLoadedSent && imageLoaded) {
        preDraw()
        imageLoadedSent = true
        onImageLoaded()
        if (onImageEventListener != null) {
            onImageEventListener?.onImageLoaded()
        }
    }
    return imageLoaded
}

/**
 * Creates Paint objects once when first needed.
 */
internal fun SubsamplingScaleImageView.createPaints() {
    if (bitmapPaint == null) {
        bitmapPaint = Paint()
        bitmapPaint?.isAntiAlias = true
        bitmapPaint?.isFilterBitmap = true
        bitmapPaint?.isDither = true
    }
    if ((debugTextPaint == null || debugLinePaint == null) && debug) {
        debugTextPaint = Paint()
        debugTextPaint?.textSize = px(12).toFloat()
        debugTextPaint?.color = Color.MAGENTA
        debugTextPaint?.style = Paint.Style.FILL
        debugLinePaint = Paint()
        debugLinePaint?.color = Color.MAGENTA
        debugLinePaint?.style = Paint.Style.STROKE
        debugLinePaint?.strokeWidth = px(1).toFloat()
    }
}

/**
 * Called on first draw when the view has dimensions. Calculates the initial sample size and starts async loading of
 * the base layer image - the whole source subsampled as necessary.
 */
@Synchronized
internal fun SubsamplingScaleImageView.initialiseBaseLayer(maxTileDimensions: Point) {
    debug("initialiseBaseLayer maxTileDimensions=%dx%d", maxTileDimensions.x, maxTileDimensions.y)

    satTemp = ScaleAndTranslate(0f, PointF(0f, 0f))
    satTemp?.let {
        fitToBounds(true, it)
        // Load double resolution - next level will be split into four tiles and at the center all four are required,
        // so don't bother with tiling until the next level 16 tiles are needed.
        fullImageSampleSize = calculateInSampleSize(it.scale)
    }

    if (fullImageSampleSize > 1) {
        fullImageSampleSize /= 2
    }

    if (fullImageSampleSize == 1 && sRegion == null && sWidth() < maxTileDimensions.x && sHeight() < maxTileDimensions.y) {

        // Whole image is required at native resolution, and is smaller than the canvas max bitmap size.
        // Use BitmapDecoder for better image support.
        decoder?.recycle()
        decoder = null
        uri?.let {
            val task = BitmapLoadTask(this, context, bitmapDecoderFactory, it, false)
            execute(task)
        }
    } else {

        initialiseTileMap(maxTileDimensions)

        tileMap?.let { it ->
            it[fullImageSampleSize]?.let {
                for (baseTile in it) {
                    decoder?.let { decoder ->
                        val task = TileLoadTask(this, decoder, baseTile)
                        execute(task)
                    }
                }
            }
        }

        refreshRequiredTiles(true)
    }

}

/**
 * Loads the optimum tiles for display at the current scale and translate, so the screen can be filled with tiles
 * that are at least as high resolution as the screen. Frees up bitmaps that are now off the screen.
 *
 * @param load Whether to load the new tiles needed. Use false while scrolling/panning for performance.
 */
internal fun SubsamplingScaleImageView.refreshRequiredTiles(load: Boolean) {
    if (decoder == null || tileMap == null) {
        return
    }

    val sampleSize = min(fullImageSampleSize, calculateInSampleSize(scale))

    // Load tiles of the correct sample size that are on screen. Discard tiles off screen, and those that are higher
    // resolution than required, or lower res than required but not the base layer, so the base layer is always present.
    tileMap?.let { it ->
        for (tileMapEntry in it.entries) {
            for (tile in tileMapEntry.value) {
                if (tile.sampleSize < sampleSize || tile.sampleSize > sampleSize && tile.sampleSize != fullImageSampleSize) {
                    tile.visible = false
                    tile.bitmap?.recycle()
                    tile.bitmap = null
                }
                if (tile.sampleSize == sampleSize) {
                    if (tileVisible(tile)) {
                        tile.visible = true
                        if (!tile.loading && tile.bitmap == null && load) {
                            decoder?.let {
                                val task = TileLoadTask(this, it, tile)
                                execute(task)
                            }
                        }
                    } else if (tile.sampleSize != fullImageSampleSize) {
                        tile.visible = false
                        tile.bitmap?.recycle()
                        tile.bitmap = null
                    }
                } else if (tile.sampleSize == fullImageSampleSize) {
                    tile.visible = true
                }
            }
        }
    }
}

/**
 * Determine whether tile is visible.
 */
internal fun SubsamplingScaleImageView.tileVisible(tile: Tile): Boolean {
    val sVisLeft = viewToSourceX(0f)
    val sVisRight = viewToSourceX(width.toFloat())
    val sVisTop = viewToSourceY(0f)
    val sVisBottom = viewToSourceY(height.toFloat())
    return !(sVisLeft > tile.sRect?.right ?: 0 || tile.sRect?.left ?: 0 > sVisRight || sVisTop > tile.sRect?.bottom ?: 0 || tile.sRect?.top ?: 0 > sVisBottom)
}

/**
 * Sets scale and translate ready for the next draw.
 */
internal fun SubsamplingScaleImageView.preDraw() {
    if (width == 0 || height == 0 || sWidth <= 0 || sHeight <= 0) {
        return
    }
    // If waiting to translate to new center position, set translate now

    if (sPendingCenter != null && pendingScale != null) {
        pendingScale?.let {
            scale = it
        }
        if (vTranslate == null) {
            vTranslate = PointF()
        }
        vTranslate?.x = width / 2 - scale * (sPendingCenter?.x ?: 0F)
        vTranslate?.y = height / 2 - scale * (sPendingCenter?.y ?: 0F)
        sPendingCenter = null
        pendingScale = null
        fitToBounds(true)
        refreshRequiredTiles(true)
    }

    // On first display of base image set up position, and in other cases make sure scale is correct.
    fitToBounds(false)
}

/**
 * Calculates sample size to fit the source image in given bounds.
 */
internal fun SubsamplingScaleImageView.calculateInSampleSize(scale: Float): Int {
    var newScale = scale
    if (minimumTileDpi > 0) {
        val metrics = resources.displayMetrics
        val averageDpi = (metrics.xdpi + metrics.ydpi) / 2
        newScale *= minimumTileDpi / averageDpi
    }

    val reqWidth = (sWidth() * newScale).toInt()
    val reqHeight = (sHeight() * newScale).toInt()

    // Raw height and width of image
    var inSampleSize = 1
    if (reqWidth == 0 || reqHeight == 0) {
        return 32
    }

    if (sHeight() > reqHeight || sWidth() > reqWidth) {

        // Calculate ratios of height and width to requested height and width
        val heightRatio = (sHeight().toFloat() / reqHeight.toFloat()).roundToInt()
        val widthRatio = (sWidth().toFloat() / reqWidth.toFloat()).roundToInt()

        // Choose the smallest ratio as inSampleSize value, this will guarantee
        // a final image with both dimensions larger than or equal to the
        // requested height and width.
        inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
    }

    // We want the actual sample size that will be used, so round down to nearest power of 2.
    var power = 1
    while (power * 2 < inSampleSize) {
        power *= 2
    }

    return power
}

/**
 * Adjusts hypothetical future scale and translate values to keep scale within the allowed range and the image on screen. Minimum scale
 * is set so one dimension fills the view and the image is centered on the other dimension. Used to calculate what the target of an
 * animation should be.
 *
 * @param center Whether the image should be centered in the dimension it's too small to fill. While animating this can be false to avoid changes in direction as bounds are reached.
 * @param sat    The scale we want and the translation we're aiming for. The values are adjusted to be valid.
 */
internal fun SubsamplingScaleImageView.fitToBounds(center: Boolean, sat: ScaleAndTranslate) {
    var newCenter = center
    if (panLimit == ViewValues.PAN_LIMIT_OUTSIDE && isReady()) {
        newCenter = false
    }

    val vTranslate = sat.vTranslate
    val scale = limitedScale(sat.scale)
    val scaleWidth = scale * sWidth()
    val scaleHeight = scale * sHeight()

    if (panLimit == ViewValues.PAN_LIMIT_CENTER && isReady()) {
        vTranslate.x = vTranslate.x.coerceAtLeast(width / 2 - scaleWidth)
        vTranslate.y = vTranslate.y.coerceAtLeast(height / 2 - scaleHeight)
    } else if (newCenter) {
        vTranslate.x = vTranslate.x.coerceAtLeast(width - scaleWidth)
        vTranslate.y = vTranslate.y.coerceAtLeast(height - scaleHeight)
    } else {
        vTranslate.x = vTranslate.x.coerceAtLeast(-scaleWidth)
        vTranslate.y = vTranslate.y.coerceAtLeast(-scaleHeight)
    }

    // Asymmetric padding adjustments
    val xPaddingRatio = if (paddingLeft > 0 || paddingRight > 0) paddingLeft / (paddingLeft + paddingRight).toFloat() else 0.5f
    val yPaddingRatio = if (paddingTop > 0 || paddingBottom > 0) paddingTop / (paddingTop + paddingBottom).toFloat() else 0.5f

    val maxTx: Float
    val maxTy: Float
    if (panLimit == ViewValues.PAN_LIMIT_CENTER && isReady()) {
        maxTx = 0.coerceAtLeast(width / 2).toFloat()
        maxTy = 0.coerceAtLeast(height / 2).toFloat()
    } else if (newCenter) {
        maxTx = 0f.coerceAtLeast((width - scaleWidth) * xPaddingRatio)
        maxTy = 0f.coerceAtLeast((height - scaleHeight) * yPaddingRatio)
    } else {
        maxTx = 0.coerceAtLeast(width).toFloat()
        maxTy = 0.coerceAtLeast(height).toFloat()
    }

    vTranslate.x = vTranslate.x.coerceAtMost(maxTx)
    vTranslate.y = vTranslate.y.coerceAtMost(maxTy)

    sat.scale = scale
}

/**
 * Adjusts current scale and translate values to keep scale within the allowed range and the image on screen. Minimum scale
 * is set so one dimension fills the view and the image is centered on the other dimension.
 *
 * @param center Whether the image should be centered in the dimension it's too small to fill. While animating this can be false to avoid changes in direction as bounds are reached.
 */
internal fun SubsamplingScaleImageView.fitToBounds(center: Boolean) {
    var init = false
    if (vTranslate == null) {
        init = true
        vTranslate = PointF(0f, 0f)
    }
    if (satTemp == null) {
        satTemp = ScaleAndTranslate(0f, PointF(0f, 0f))
    }
    satTemp?.scale = scale
    vTranslate?.let { satTemp?.vTranslate?.set(it) }
    satTemp?.let { fitToBounds(center, it) }
    scale = satTemp?.scale ?: 0F
    vTranslate?.set(satTemp?.vTranslate ?: PointF())
    if (init && minimumScaleType != ViewValues.SCALE_TYPE_START) {
        vTranslate?.set(vTranslateForSCenter((sWidth() / 2).toFloat(), (sHeight() / 2).toFloat(), scale))
    }
}

/**
 * Once source image and view dimensions are known, creates a map of sample size to tile grid.
 */
internal fun SubsamplingScaleImageView.initialiseTileMap(maxTileDimensions: Point) {
    debug("initialiseTileMap maxTileDimensions=%dx%d", maxTileDimensions.x, maxTileDimensions.y)
    this.tileMap = LinkedHashMap()
    var sampleSize = fullImageSampleSize
    var xTiles = 1
    var yTiles = 1
    while (true) {
        var sTileWidth = sWidth() / xTiles
        var sTileHeight = sHeight() / yTiles
        var subTileWidth = sTileWidth / sampleSize
        var subTileHeight = sTileHeight / sampleSize
        while (subTileWidth + xTiles + 1 > maxTileDimensions.x || subTileWidth > width * 1.25 && sampleSize < fullImageSampleSize) {
            xTiles += 1
            sTileWidth = sWidth() / xTiles
            subTileWidth = sTileWidth / sampleSize
        }
        while (subTileHeight + yTiles + 1 > maxTileDimensions.y || subTileHeight > height * 1.25 && sampleSize < fullImageSampleSize) {
            yTiles += 1
            sTileHeight = sHeight() / yTiles
            subTileHeight = sTileHeight / sampleSize
        }
        val tileGrid = ArrayList<Tile>(xTiles * yTiles)
        for (x in 0 until xTiles) {
            for (y in 0 until yTiles) {
                val tile = Tile()
                tile.sampleSize = sampleSize
                tile.visible = sampleSize == fullImageSampleSize
                tile.sRect = Rect(
                        x * sTileWidth,
                        y * sTileHeight,
                        if (x == xTiles - 1) sWidth() else (x + 1) * sTileWidth,
                        if (y == yTiles - 1) sHeight() else (y + 1) * sTileHeight
                )
                tile.vRect = Rect(0, 0, 0, 0)
                tile.fileSRect = Rect(tile.sRect)
                tileGrid.add(tile)
            }
        }
        tileMap?.let { it[sampleSize] = tileGrid }
        if (sampleSize == 1) {
            break
        } else {
            sampleSize /= 2
        }
    }
}

/**
 * Called by worker task when decoder is ready and image size and EXIF orientation is known.
 */
@Synchronized
internal fun SubsamplingScaleImageView.onTilesInited(decoder: ImageRegionDecoder, sWidth: Int, sHeight: Int, sOrientation: Int) {
    debug("onTilesInited sWidth=%d, sHeight=%d, sOrientation=%d", sWidth, sHeight, orientation)
    // If actual dimensions don't match the declared size, reset everything.
    if (this.sWidth > 0 && this.sHeight > 0 && (this.sWidth != sWidth || this.sHeight != sHeight)) {
        reset(false)
        if (bitmap != null) {
            if (!bitmapIsCached) {
                bitmap?.recycle()
            }
            bitmap = null
            if (onImageEventListener != null && bitmapIsCached) {
                onImageEventListener?.onPreviewReleased()
            }
            bitmapIsPreview = false
            bitmapIsCached = false
        }
    }
    this.decoder = decoder
    this.sWidth = sWidth
    this.sHeight = sHeight
    this.sOrientation = sOrientation
    checkReady()
    if (!checkImageLoaded() && maxTileWidth > 0 && maxTileWidth != SubsamplingScaleImageView.TILE_SIZE_AUTO && maxTileHeight > 0 && maxTileHeight != SubsamplingScaleImageView.TILE_SIZE_AUTO && width > 0 && height > 0) {
        initialiseBaseLayer(Point(maxTileWidth, maxTileHeight))
    }
    invalidate()
    requestLayout()
}

/**
 * Called by worker task when a tile has loaded. Redraws the view.
 */
@Synchronized
internal fun SubsamplingScaleImageView.onTileLoaded() {
    debug("onTileLoaded")
    checkReady()
    checkImageLoaded()
    if (isBaseLayerReady() && bitmap != null) {
        if (!bitmapIsCached) {
            bitmap?.recycle()
        }
        bitmap = null
        if (onImageEventListener != null && bitmapIsCached) {
            onImageEventListener?.onPreviewReleased()
        }
        bitmapIsPreview = false
        bitmapIsCached = false
    }
    invalidate()
}

/**
 * Called by worker task when preview image is loaded.
 */
@Synchronized
internal fun SubsamplingScaleImageView.onPreviewLoaded(previewBitmap: Bitmap?) {
    debug("onPreviewLoaded")
    if (bitmap != null || imageLoadedSent) {
        previewBitmap?.recycle()
        return
    }
    bitmap = if (pRegion != null) {
        previewBitmap?.let {
            Bitmap.createBitmap(it, pRegion?.left ?: 0, pRegion?.top ?: 0, pRegion?.width()
                    ?: 0, pRegion?.height() ?: 0)
        }
    } else {
        previewBitmap
    }
    bitmapIsPreview = true
    if (checkReady()) {
        invalidate()
        requestLayout()
    }
}

/**
 * Called by worker task when full size image bitmap is ready (tiling is disabled).
 */
@Synchronized
internal fun SubsamplingScaleImageView.onImageLoaded(bitmap: Bitmap?, sOrientation: Int, bitmapIsCached: Boolean) {
    debug("onImageLoaded")
    // If actual dimensions don't match the declared size, reset everything.
    if (this.sWidth > 0 && this.sHeight > 0 && (this.sWidth != bitmap?.width || this.sHeight != bitmap.height)) {
        reset(false)
    }
    if (this.bitmap != null && !this.bitmapIsCached) {
        this.bitmap?.recycle()
    }

    if (this.bitmap != null && this.bitmapIsCached && onImageEventListener != null) {
        onImageEventListener?.onPreviewReleased()
    }

    this.bitmapIsPreview = false
    this.bitmapIsCached = bitmapIsCached
    this.bitmap = bitmap
    this.sWidth = bitmap?.width ?: 0
    this.sHeight = bitmap?.height ?: 0
    this.sOrientation = sOrientation
    val ready = checkReady()
    val imageLoaded = checkImageLoaded()
    if (ready || imageLoaded) {
        invalidate()
        requestLayout()
    }
}

internal fun SubsamplingScaleImageView.execute(asyncTask: AsyncTask<Void, Void, *>) {
    asyncTask.executeOnExecutor(executor)
}

/**
 * Set scale, center and orientation from saved state.
 */
internal fun SubsamplingScaleImageView.restoreState(state: ImageViewState?) {
    if (state != null && ViewValues.VALID_ORIENTATIONS.contains(state.orientation)) {
        this.orientation = state.orientation
        this.pendingScale = state.scale
        this.sPendingCenter = state.center
        invalidate()
    }
}

/**
 * Use canvas max bitmap width and height instead of the default 2048, to avoid redundant tiling.
 */
internal fun SubsamplingScaleImageView.getMaxBitmapDimensions(canvas: Canvas): Point {
    return Point(canvas.maximumBitmapWidth.coerceAtMost(maxTileWidth), canvas.maximumBitmapHeight.coerceAtMost(maxTileHeight))
}

/**
 * Get source width taking rotation into account.
 */
internal fun SubsamplingScaleImageView.sWidth(): Int {
    val rotation = getRequiredRotation()
    return if (rotation == 90 || rotation == 270) {
        sHeight
    } else {
        sWidth
    }
}

/**
 * Get source height taking rotation into account.
 */
internal fun SubsamplingScaleImageView.sHeight(): Int {
    val rotation = getRequiredRotation()
    return if (rotation == 90 || rotation == 270) {
        sWidth
    } else {
        sHeight
    }
}

/**
 * Converts source rectangle from tile, which treats the image file as if it were in the correct orientation already,
 * to the rectangle of the image that needs to be loaded.
 */
@AnyThread
internal fun SubsamplingScaleImageView.fileSRect(sRect: Rect, target: Rect) {
    when (getRequiredRotation()) {
        0 -> target.set(sRect)
        90 -> target.set(sRect.top, sHeight - sRect.right, sRect.bottom, sHeight - sRect.left)
        180 -> target.set(sWidth - sRect.right, sHeight - sRect.bottom, sWidth - sRect.left, sHeight - sRect.top)
        else -> target.set(sWidth - sRect.bottom, sRect.left, sWidth - sRect.top, sRect.right)
    }
}

/**
 * Determines the rotation to be applied to tiles, based on EXIF orientation or chosen setting.
 */
@AnyThread
internal fun SubsamplingScaleImageView.getRequiredRotation(): Int {
    return if (orientation == ViewValues.ORIENTATION_USE_EXIF) {
        sOrientation
    } else {
        orientation
    }
}

/**
 * Pythagoras distance between two points.
 */
internal fun SubsamplingScaleImageView.distance(x0: Float, x1: Float, y0: Float, y1: Float): Float {
    val x = x0 - x1
    val y = y0 - y1
    return sqrt((x * x + y * y).toDouble()).toFloat()
}

/**
 * Convert screen to source x coordinate.
 */
internal fun SubsamplingScaleImageView.viewToSourceX(vx: Float): Float {
    vTranslate?.let {
        return (vx - it.x) / scale
    }
    return Float.NaN
}

/**
 * Convert screen to source y coordinate.
 */
internal fun SubsamplingScaleImageView.viewToSourceY(vy: Float): Float {
    vTranslate?.let {
        return (vy - it.y) / scale
    }
    return Float.NaN
}

/**
 * Convert source to view x coordinate.
 */
internal fun SubsamplingScaleImageView.sourceToViewX(sx: Float): Float {
    vTranslate?.let {
        return sx * scale + it.x
    }
    return Float.NaN
}

/**
 * Convert source to view y coordinate.
 */
internal fun SubsamplingScaleImageView.sourceToViewY(sy: Float): Float {
    vTranslate?.let {
        return sy * scale + it.y
    }
    return Float.NaN
}

/**
 * Convert source rect to screen rect, integer values.
 */
internal fun SubsamplingScaleImageView.sourceToViewRect(sRect: Rect, vTarget: Rect) {
    vTarget.set(
            sourceToViewX(sRect.left.toFloat()).toInt(),
            sourceToViewY(sRect.top.toFloat()).toInt(),
            sourceToViewX(sRect.right.toFloat()).toInt(),
            sourceToViewY(sRect.bottom.toFloat()).toInt()
    )
}

/**
 * Get the translation required to place a given source coordinate at the center of the screen, with the center
 * adjusted for asymmetric padding. Accepts the desired scale as an argument, so this is independent of current
 * translate and scale. The result is fitted to bounds, putting the image point as near to the screen center as permitted.
 */
internal fun SubsamplingScaleImageView.vTranslateForSCenter(sCenterX: Float, sCenterY: Float, scale: Float): PointF {
    val vxCenter = paddingLeft + (width - paddingRight - paddingLeft) / 2
    val vyCenter = paddingTop + (height - paddingBottom - paddingTop) / 2
    if (satTemp == null) {
        satTemp = ScaleAndTranslate(0f, PointF(0f, 0f))
    }
    satTemp?.let {
        it.scale = scale
        it.vTranslate.set(vxCenter - sCenterX * scale, vyCenter - sCenterY * scale)
        fitToBounds(true, it)
    }
    return satTemp?.vTranslate ?: PointF()
}

/**
 * Given a requested source center and scale, calculate what the actual center will have to be to keep the image in
 * pan limits, keeping the requested center as near to the middle of the screen as allowed.
 */
internal fun SubsamplingScaleImageView.limitedSCenter(sCenterX: Float, sCenterY: Float, scale: Float, sTarget: PointF): PointF {
    val vTranslate = vTranslateForSCenter(sCenterX, sCenterY, scale)
    val vxCenter = paddingLeft + (width - paddingRight - paddingLeft) / 2
    val vyCenter = paddingTop + (height - paddingBottom - paddingTop) / 2
    val sx = (vxCenter - vTranslate.x) / scale
    val sy = (vyCenter - vTranslate.y) / scale
    sTarget.set(sx, sy)
    return sTarget
}

/**
 * Returns the minimum allowed scale.
 */
internal fun SubsamplingScaleImageView.minScale(): Float {
    val vPadding = paddingBottom + paddingTop
    val hPadding = paddingLeft + paddingRight
    return if (minimumScaleType == ViewValues.SCALE_TYPE_CENTER_CROP || minimumScaleType == ViewValues.SCALE_TYPE_START) {
        ((width - hPadding) / sWidth().toFloat()).coerceAtLeast((height - vPadding) / sHeight().toFloat())
    } else if (minimumScaleType == ViewValues.SCALE_TYPE_CUSTOM && minScale > 0) {
        minScale
    } else {
        ((width - hPadding) / sWidth().toFloat()).coerceAtMost((height - vPadding) / sHeight().toFloat())
    }
}

/**
 * Adjust a requested scale to be within the allowed limits.
 */
internal fun SubsamplingScaleImageView.limitedScale(targetScale: Float): Float {
    val maxTargetScale = max(minScale(), targetScale)
    return min(maxScale, maxTargetScale)
}

/**
 * Apply a selected type of easing.
 *
 * @param type     Easing type, from static fields
 * @param time     Elapsed time
 * @param from     Start value
 * @param change   Target value
 * @param duration Anm duration
 * @return Current value
 */
internal fun SubsamplingScaleImageView.ease(type: Int, time: Long, from: Float, change: Float, duration: Long): Float {
    return when (type) {
        ViewValues.EASE_IN_OUT_QUAD -> easeInOutQuad(time, from, change, duration)
        ViewValues.EASE_OUT_QUAD -> easeOutQuad(time, from, change, duration)
        else -> throw IllegalStateException("Unexpected easing type: $type")
    }
}

/**
 * Quadratic easing for fling. With thanks to Robert Penner - http://gizma.com/easing/
 *
 * @param time     Elapsed time
 * @param from     Start value
 * @param change   Target value
 * @param duration Anm duration
 * @return Current value
 */
internal fun SubsamplingScaleImageView.easeOutQuad(time: Long, from: Float, change: Float, duration: Long): Float {
    val progress = time.toFloat() / duration.toFloat()
    return -change * progress * (progress - 2) + from
}

/**
 * Quadratic easing for scale and center animations. With thanks to Robert Penner - http://gizma.com/easing/
 *
 * @param time     Elapsed time
 * @param from     Start value
 * @param change   Target value
 * @param duration Anm duration
 * @return Current value
 */
internal fun SubsamplingScaleImageView.easeInOutQuad(time: Long, from: Float, change: Float, duration: Long): Float {
    var timeF = time / (duration / 2f)
    return if (timeF < 1) {
        change / 2f * timeF * timeF + from
    } else {
        timeF--
        -change / 2f * (timeF * (timeF - 2) - 1) + from
    }
}

/**
 * Debug logger
 */
@AnyThread
internal fun SubsamplingScaleImageView.debug(message: String, vararg args: Any) {
    if (debug) {
        Log.d(SubsamplingScaleImageView.TAG, String.format(message, *args))
    }
}

/**
 * For debug overlays. Scale pixel value according to screen density.
 */
internal fun SubsamplingScaleImageView.px(px: Int): Int {
    return (density * px).toInt()
}

internal fun SubsamplingScaleImageView.sendStateChanged(oldScale: Float, oldVTranslate: PointF?, origin: Int) {
    if (scale != oldScale) {
        onStateChangedListener?.onScaleChanged(scale, origin)
    }
    if (vTranslate != oldVTranslate) {
        getCenter()?.let { onStateChangedListener?.onCenterChanged(it, origin) }
    }
}