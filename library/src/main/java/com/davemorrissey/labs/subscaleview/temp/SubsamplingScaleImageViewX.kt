package com.davemorrissey.labs.subscaleview.temp

import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.ImageViewState
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.davemorrissey.labs.subscaleview.ViewValues
import com.davemorrissey.labs.subscaleview.listener.OnImageEventListener
import kotlin.math.max

/**
 * Set the image source from a bitmap, resource, asset, file or other URI.
 *
 * @param imageSource Image source.
 */
fun SubsamplingScaleImageView.setImage(imageSource: ImageSource) {
    setImage(imageSource, null, null)
}

/**
 * Set the image source from a bitmap, resource, asset, file or other URI, starting with a given orientation
 * setting, scale and center. This is the best method to use when you want scale and center to be restored
 * after screen orientation change; it avoids any redundant loading of tiles in the wrong orientation.
 *
 * @param imageSource Image source.
 * @param state       State to be restored. Nullable.
 */
fun SubsamplingScaleImageView.setImage(imageSource: ImageSource, state: ImageViewState) {
    setImage(imageSource, null, state)
}

/**
 * Set the image source from a bitmap, resource, asset, file or other URI, providing a preview image to be
 * displayed until the full size image is loaded.
 *
 *
 * You must declare the dimensions of the full size image by calling [ImageSource.dimensions]
 * on the imageSource object. The preview source will be ignored if you don't provide dimensions,
 * and if you provide a bitmap for the full size image.
 *
 * @param imageSource   Image source. Dimensions must be declared.
 * @param previewSource Optional source for a preview image to be displayed and allow interaction while the full size image loads.
 */
fun SubsamplingScaleImageView.setImage(imageSource: ImageSource, previewSource: ImageSource) {
    setImage(imageSource, previewSource, null)
}

/**
 * By default the View automatically calculates the optimal tile size. Set this to override this, and force an upper limit to the dimensions of the generated tiles. Passing [.TILE_SIZE_AUTO] will re-enable the default behaviour.
 *
 * @param maxPixels Maximum tile size X and Y in pixels.
 */
fun SubsamplingScaleImageView.setMaxTileSize(maxPixels: Int) {
    this.maxTileWidth = maxPixels
    this.maxTileHeight = maxPixels
}

/**
 * By default the View automatically calculates the optimal tile size. Set this to override this, and force an upper limit to the dimensions of the generated tiles. Passing [.TILE_SIZE_AUTO] will re-enable the default behaviour.
 *
 * @param maxPixelsX Maximum tile width.
 * @param maxPixelsY Maximum tile height.
 */
fun SubsamplingScaleImageView.setMaxTileSize(maxPixelsX: Int, maxPixelsY: Int) {
    this.maxTileWidth = maxPixelsX
    this.maxTileHeight = maxPixelsY
}

/**
 * Convert screen coordinate to source coordinate.
 *
 * @param vxy view X/Y coordinate.
 * @return a coordinate representing the corresponding source coordinate.
 */
fun SubsamplingScaleImageView.viewToSourceCoord(vxy: PointF): PointF? {
    return viewToSourceCoord(vxy.x, vxy.y, PointF())
}

/**
 * Convert screen coordinate to source coordinate.
 *
 * @param vx view X coordinate.
 * @param vy view Y coordinate.
 * @return a coordinate representing the corresponding source coordinate.
 */
fun SubsamplingScaleImageView.viewToSourceCoord(vx: Float, vy: Float): PointF? {
    return viewToSourceCoord(vx, vy, PointF())
}

/**
 * Convert screen coordinate to source coordinate.
 *
 * @param vxy     view coordinates to convert.
 * @param sTarget target object for result. The same instance is also returned.
 * @return source coordinates. This is the same instance passed to the sTarget param.
 */
fun SubsamplingScaleImageView.viewToSourceCoord(vxy: PointF, sTarget: PointF): PointF? {
    return viewToSourceCoord(vxy.x, vxy.y, sTarget)
}

/**
 * Convert screen coordinate to source coordinate.
 *
 * @param vx      view X coordinate.
 * @param vy      view Y coordinate.
 * @param sTarget target object for result. The same instance is also returned.
 * @return source coordinates. This is the same instance passed to the sTarget param.
 */
fun SubsamplingScaleImageView.viewToSourceCoord(vx: Float, vy: Float, sTarget: PointF): PointF? {
    if (vTranslate == null) {
        return null
    }
    sTarget.set(viewToSourceX(vx), viewToSourceY(vy))
    return sTarget
}

/**
 * Convert source coordinate to view coordinate.
 *
 * @param sxy source coordinates to convert.
 * @return view coordinates.
 */
fun SubsamplingScaleImageView.sourceToViewCoord(sxy: PointF): PointF? {
    return sourceToViewCoord(sxy.x, sxy.y, PointF())
}

/**
 * Convert source coordinate to view coordinate.
 *
 * @param sx source X coordinate.
 * @param sy source Y coordinate.
 * @return view coordinates.
 */
fun SubsamplingScaleImageView.sourceToViewCoord(sx: Float, sy: Float): PointF? {
    return sourceToViewCoord(sx, sy, PointF())
}

/**
 * Convert source coordinate to view coordinate.
 *
 * @param sxy     source coordinates to convert.
 * @param vTarget target object for result. The same instance is also returned.
 * @return view coordinates. This is the same instance passed to the vTarget param.
 */
fun SubsamplingScaleImageView.sourceToViewCoord(sxy: PointF, vTarget: PointF): PointF? {
    return sourceToViewCoord(sxy.x, sxy.y, vTarget)
}

/**
 * Convert source coordinate to view coordinate.
 *
 * @param sx      source X coordinate.
 * @param sy      source Y coordinate.
 * @param vTarget target object for result. The same instance is also returned.
 * @return view coordinates. This is the same instance passed to the vTarget param.
 */
fun SubsamplingScaleImageView.sourceToViewCoord(sx: Float, sy: Float, vTarget: PointF): PointF? {
    if (vTranslate == null) {
        return null
    }
    vTarget.set(sourceToViewX(sx), sourceToViewY(sy))
    return vTarget
}

/**
 * Set the minimum scale type. See static fields. Normally [ViewValues.SCALE_TYPE_CENTER_INSIDE] is best, for image galleries.
 *
 * @param scaleType a scale type constant. See static fields.
 */
fun SubsamplingScaleImageView.setMinimumScaleType(scaleType: Int) {
    require(ViewValues.VALID_SCALE_TYPES.contains(scaleType)) { "Invalid scale type: $scaleType" }
    this.minimumScaleType = scaleType
    if (isReady()) {
        fitToBounds(true)
        invalidate()
    }
}


/**
 * This is a screen density aware alternative to [.setMaxScale]; it allows you to express the maximum
 * allowed scale in terms of the minimum pixel density. This avoids the problem of 1:1 scale still being
 * too small on a high density screen. A sensible starting point is 160 - the default used by this view.
 *
 * @param dpi Source image pixel density at maximum zoom.
 */
fun SubsamplingScaleImageView.setMinimumDpi(dpi: Int) {
    val metrics = resources.displayMetrics
    val averageDpi = (metrics.xdpi + metrics.ydpi) / 2
    maxScale = averageDpi / dpi
}

/**
 * This is a screen density aware alternative to [.setMinScale]; it allows you to express the minimum
 * allowed scale in terms of the maximum pixel density.
 *
 * @param dpi Source image pixel density at minimum zoom.
 */
fun SubsamplingScaleImageView.setMaximumDpi(dpi: Int) {
    val metrics = resources.displayMetrics
    val averageDpi = (metrics.xdpi + metrics.ydpi) / 2
    minScale = averageDpi / dpi
}

/**
 * Returns the maximum allowed scale.
 *
 * @return the maximum scale as a source/view pixels ratio.
 */
fun SubsamplingScaleImageView.getMaxScale(): Float {
    return maxScale
}

/**
 * Set the maximum scale allowed. A value of 1 means 1:1 pixels at maximum scale. You may wish to set this according
 * to screen density - on a retina screen, 1:1 may still be too small. Consider using [.setMinimumDpi],
 * which is density aware.
 *
 * @param maxScale maximum scale expressed as a source/view pixels ratio.
 */
fun SubsamplingScaleImageView.setMaxScale(maxScale: Float) {
    this.maxScale = maxScale
}

/**
 * Returns the minimum allowed scale.
 *
 * @return the minimum scale as a source/view pixels ratio.
 */
fun SubsamplingScaleImageView.getMinScale(): Float {
    return minScale()
}

/**
 * Set the minimum scale allowed. A value of 1 means 1:1 pixels at minimum scale. You may wish to set this according
 * to screen density. Consider using [.setMaximumDpi], which is density aware.
 *
 * @param minScale minimum scale expressed as a source/view pixels ratio.
 */
fun SubsamplingScaleImageView.setMinScale(minScale: Float) {
    this.minScale = minScale
}

/**
 * By default, image tiles are at least as high resolution as the screen. For a retina screen this may not be
 * necessary, and may increase the likelihood of an OutOfMemoryError. This method sets a DPI at which higher
 * resolution tiles should be loaded. Using a lower number will on average use less memory but result in a lower
 * quality image. 160-240dpi will usually be enough. This should be called before setting the image source,
 * because it affects which tiles get loaded. When using an untiled source image this method has no effect.
 *
 * @param minimumTileDpi Tile loading threshold.
 */
fun SubsamplingScaleImageView.setMinimumTileDpi(minimumTileDpi: Int) {
    val metrics = resources.displayMetrics
    val averageDpi = (metrics.xdpi + metrics.ydpi) / 2
    this.minimumTileDpi = averageDpi.coerceAtMost(minimumTileDpi.toFloat()).toInt()
    if (isReady()) {
        reset(false)
        invalidate()
    }
}

/**
 * Returns the source point at the center of the view.
 *
 * @return the source coordinates current at the center of the view.
 */
fun SubsamplingScaleImageView.getCenter(): PointF? {
    val mX = width / 2
    val mY = height / 2
    return viewToSourceCoord(mX.toFloat(), mY.toFloat())
}

/**
 * Returns the current scale value.
 *
 * @return the current scale as a source/view pixels ratio.
 */
fun SubsamplingScaleImageView.getScale(): Float {
    return scale
}

/**
 * Externally change the scale and translation of the source image. This may be used with getCenter() and getScale()
 * to restore the scale and zoom after a screen rotate.
 *
 * @param scale   New scale to set.
 * @param sCenter New source image coordinate to center on the screen, subject to boundaries.
 */
fun SubsamplingScaleImageView.setScaleAndCenter(scale: Float, sCenter: PointF?) {
    this.anim = null
    this.pendingScale = scale
    this.sPendingCenter = sCenter
    this.sRequestedCenter = sCenter
    invalidate()
}

/**
 * Fully zoom out and return the image to the middle of the screen. This might be useful if you have a view pager
 * and want images to be reset when the user has moved to another page.
 */
fun SubsamplingScaleImageView.resetScaleAndCenter() {
    this.anim = null
    this.pendingScale = limitedScale(0f)
    if (isReady()) {
        this.sPendingCenter = PointF((sWidth() / 2).toFloat(), (sHeight() / 2).toFloat())
    } else {
        this.sPendingCenter = PointF(0f, 0f)
    }
    invalidate()
}

/**
 * Call to find whether the view is initialised, has dimensions, and will display an image on
 * the next draw. If a preview has been provided, it may be the preview that will be displayed
 * and the full size image may still be loading. If no preview was provided, this is called once
 * the base layer tiles of the full size image are loaded.
 *
 * @return true if the view is ready to display an image and accept touch gestures.
 */
fun SubsamplingScaleImageView.isReady(): Boolean {
    return readySent
}

/**
 * Called once when the view is initialised, has dimensions, and will display an image on the
 * next draw. This is triggered at the same time as [OnImageEventListener.onReady] but
 * allows a subclass to receive this event without using a listener.
 */
fun SubsamplingScaleImageView.onReady() {

}

/**
 * Call to find whether the main image (base layer tiles where relevant) have been loaded. Before
 * this event the view is blank unless a preview was provided.
 *
 * @return true if the main image (not the preview) has been loaded and is ready to display.
 */
fun SubsamplingScaleImageView.isImageLoaded(): Boolean {
    return imageLoadedSent
}

/**
 * Called once when the full size image or its base layer tiles have been loaded.
 */
fun SubsamplingScaleImageView.onImageLoaded() {

}

/**
 * Get source width, ignoring orientation. If [.getOrientation] returns 90 or 270, you can use [.getSHeight]
 * for the apparent width.
 *
 * @return the source image width in pixels.
 */
fun SubsamplingScaleImageView.getSWidth(): Int {
    return sWidth
}

/**
 * Get source height, ignoring orientation. If [.getOrientation] returns 90 or 270, you can use [.getSWidth]
 * for the apparent height.
 *
 * @return the source image height in pixels.
 */
fun SubsamplingScaleImageView.getSHeight(): Int {
    return sHeight
}

/**
 * Returns the orientation setting. This can return [ViewValues.ORIENTATION_USE_EXIF], in which case it doesn't tell you
 * the applied orientation of the image. For that, use [.getAppliedOrientation].
 *
 * @return the orientation setting. See static fields.
 */
fun SubsamplingScaleImageView.getOrientation(): Int {
    return orientation
}

/**
 * Sets the image orientation. It's best to call this before setting the image file or asset, because it may waste
 * loading of tiles. However, this can be freely called at any time.
 *
 * @param orientation orientation to be set. See ORIENTATION_ static fields for valid values.
 */
fun SubsamplingScaleImageView.setOrientation(orientation: Int) {
    require(ViewValues.VALID_ORIENTATIONS.contains(orientation)) { "Invalid orientation: $orientation" }
    this.orientation = orientation
    reset(false)
    invalidate()
    requestLayout()
}

/**
 * Returns the actual orientation of the image relative to the source file. This will be based on the source file's
 * EXIF orientation if you're using ORIENTATION_USE_EXIF. Values are 0, 90, 180, 270.
 *
 * @return the orientation applied after EXIF information has been extracted. See static fields.
 */
fun SubsamplingScaleImageView.getAppliedOrientation(): Int {
    return getRequiredRotation()
}

/**
 * Get the current state of the view (scale, center, orientation) for restoration after rotate. Will return null if
 * the view is not ready.
 *
 * @return an [ImageViewState] instance representing the current position of the image. null if the view isn't ready.
 */
fun SubsamplingScaleImageView.getState(): ImageViewState? {
    return if (vTranslate != null && sWidth > 0 && sHeight > 0) {
        getCenter()?.let { ImageViewState(getScale(), it, getOrientation()) }
    } else null
}

/**
 * Returns true if zoom gesture detection is enabled.
 *
 * @return true if zoom gesture detection is enabled.
 */
fun SubsamplingScaleImageView.isZoomEnabled(): Boolean {
    return zoomEnabled
}

/**
 * Enable or disable zoom gesture detection. Disabling zoom locks the the current scale.
 *
 * @param zoomEnabled true to enable zoom gestures, false to disable.
 */
fun SubsamplingScaleImageView.setZoomEnabled(zoomEnabled: Boolean) {
    this.zoomEnabled = zoomEnabled
}

/**
 * Returns true if double tap &amp; swipe to zoom is enabled.
 *
 * @return true if double tap &amp; swipe to zoom is enabled.
 */
fun SubsamplingScaleImageView.isQuickScaleEnabled(): Boolean {
    return quickScaleEnabled
}

/**
 * Enable or disable double tap &amp; swipe to zoom.
 *
 * @param quickScaleEnabled true to enable quick scale, false to disable.
 */
fun SubsamplingScaleImageView.setQuickScaleEnabled(quickScaleEnabled: Boolean) {
    this.quickScaleEnabled = quickScaleEnabled
}

/**
 * Returns true if pan gesture detection is enabled.
 *
 * @return true if pan gesture detection is enabled.
 */
fun SubsamplingScaleImageView.isPanEnabled(): Boolean {
    return panEnabled
}

/**
 * Enable or disable pan gesture detection. Disabling pan causes the image to be centered. Pan
 * can still be changed from code.
 *
 * @param panEnabled true to enable panning, false to disable.
 */
fun SubsamplingScaleImageView.setPanEnabled(panEnabled: Boolean) {
    this.panEnabled = panEnabled
    if (!panEnabled) {
        vTranslate?.let {
            it.x = width / 2 - scale * (sWidth() / 2)
            it.y = height / 2 - scale * (sHeight() / 2)
            if (isReady()) {
                refreshRequiredTiles(true)
                invalidate()
            }
        }
    }
}

/**
 * Set a solid color to render behind tiles, useful for displaying transparent PNGs.
 *
 * @param tileBgColor Background color for tiles.
 */
fun SubsamplingScaleImageView.setTileBackgroundColor(tileBgColor: Int) {
    if (Color.alpha(tileBgColor) == 0) {
        tileBgPaint = null
    } else {
        tileBgPaint = Paint()
        tileBgPaint?.style = Paint.Style.FILL
        tileBgPaint?.color = tileBgColor
    }
    invalidate()
}

/**
 * Calculate how much further the image can be panned in each direction. The results are set on
 * the supplied [RectF] and expressed as screen pixels. For example, if the image cannot be
 * panned any further towards the left, the value of [RectF.left] will be set to 0.
 *
 * @param vTarget target object for results. Re-use for efficiency.
 */
fun SubsamplingScaleImageView.getPanRemaining(vTarget: RectF) {
    if (!isReady()) {
        return
    }

    val scaleWidth = scale * sWidth()
    val scaleHeight = scale * sHeight()

    vTranslate?.let {
        when (panLimit) {
            ViewValues.PAN_LIMIT_CENTER -> {
                vTarget.top = max(0f, -(it.y - height / 2))
                vTarget.left = max(0f, -(it.x - width / 2))
                vTarget.bottom = max(0f, it.y - (height / 2 - scaleHeight))
                vTarget.right = max(0f, it.x - (width / 2 - scaleWidth))
            }
            ViewValues.PAN_LIMIT_OUTSIDE -> {
                vTarget.top = max(0f, -(it.y - height))
                vTarget.left = max(0f, -(it.x - width))
                vTarget.bottom = max(0f, it.y + scaleHeight)
                vTarget.right = max(0f, it.x + scaleWidth)
            }
            else -> {
                vTarget.top = max(0f, -it.y)
                vTarget.left = max(0f, -it.x)
                vTarget.bottom = max(0f, scaleHeight + it.y - height)
                vTarget.right = max(0f, scaleWidth + it.x - width)
            }
        }
    }
}