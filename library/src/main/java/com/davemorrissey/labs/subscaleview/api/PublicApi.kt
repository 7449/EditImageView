package com.davemorrissey.labs.subscaleview.api

import android.content.ContentResolver
import android.graphics.*
import android.net.Uri
import android.os.AsyncTask
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.ImageViewState
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.davemorrissey.labs.subscaleview.ViewValues
import com.davemorrissey.labs.subscaleview.decoder.CompatDecoderFactory
import com.davemorrissey.labs.subscaleview.decoder.DecoderFactory
import com.davemorrissey.labs.subscaleview.decoder.ImageDecoder
import com.davemorrissey.labs.subscaleview.decoder.ImageRegionDecoder
import com.davemorrissey.labs.subscaleview.listener.OnImageEventListener
import com.davemorrissey.labs.subscaleview.listener.OnStateChangedListener
import com.davemorrissey.labs.subscaleview.task.BitmapLoadTask
import com.davemorrissey.labs.subscaleview.task.TilesInitTask
import com.davemorrissey.labs.subscaleview.task.safeLet
import java.util.concurrent.Executor
import kotlin.math.max
import kotlin.math.min

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
 * Set the image source from a bitmap, resource, asset, file or other URI, providing a preview image to be
 * displayed until the full size image is loaded, starting with a given orientation setting, scale and center.
 * This is the best method to use when you want scale and center to be restored after screen orientation change;
 * it avoids any redundant loading of tiles in the wrong orientation.
 *
 *
 * You must declare the dimensions of the full size image by calling [ImageSource.dimensions]
 * on the imageSource object. The preview source will be ignored if you don't provide dimensions,
 * and if you provide a bitmap for the full size image.
 *
 * @param imageSource   Image source. Dimensions must be declared.
 * @param previewSource Optional source for a preview image to be displayed and allow interaction while the full size image loads.
 * @param state         State to be restored. Nullable.
 */
fun SubsamplingScaleImageView.setImage(imageSource: ImageSource, previewSource: ImageSource?, state: ImageViewState?) {
    reset(true)
    if (state != null) {
        restoreState(state)
    }
    if (previewSource != null) {
        require(imageSource.getBitmap() == null) { "Preview image cannot be used when a bitmap is provided for the main image" }
        require(!(imageSource.getSWidth() <= 0 || imageSource.getSHeight() <= 0)) { "Preview image cannot be used unless dimensions are provided for the main image" }
        this.sWidth = imageSource.getSWidth()
        this.sHeight = imageSource.getSHeight()
        this.pRegion = previewSource.getSRegion()
        if (previewSource.getBitmap() != null) {
            this.bitmapIsCached = previewSource.isCached()
            onPreviewLoaded(previewSource.getBitmap())
        } else {
            var uri = previewSource.getUri()
            if (uri == null && previewSource.getResource() != ImageSource.DEFAULT_RESOURCE) {
                uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.packageName + "/" + previewSource.getResource())
            }
            uri?.let {
                val task = BitmapLoadTask(this, context, bitmapDecoderFactory, it, true)
                execute(task)
            }
        }
    }

    if (imageSource.getBitmap() != null && imageSource.getSRegion() != null) {
        safeLet(imageSource.getBitmap(), imageSource.getSRegion()) { bitmap, rect ->
            onImageLoaded(Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width(), rect.height()), ViewValues.ORIENTATION_0, false)
        }
    } else if (imageSource.getBitmap() != null) {
        onImageLoaded(imageSource.getBitmap(), ViewValues.ORIENTATION_0, imageSource.isCached())
    } else {
        sRegion = imageSource.getSRegion()
        uri = imageSource.getUri()
        if (uri == null && imageSource.getResource() != ImageSource.DEFAULT_RESOURCE) {
            uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.packageName + "/" + imageSource.getResource())
        }
        if (imageSource.getTile() || sRegion != null) {
            // Load the bitmap using tile decoding.
            uri?.let {
                val task = TilesInitTask(this, context, regionDecoderFactory, it)
                execute(task)
            }
        } else {
            // Load the bitmap as a single image.
            uri?.let {
                val task = BitmapLoadTask(this, context, bitmapDecoderFactory, it, false)
                execute(task)
            }
        }
    }
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
 * Releases all resources the view is using and resets the state, nulling any fields that use significant memory.
 * After you have called this method, the view can be re-used by setting a new image. Settings are remembered
 * but state (scale and center) is forgotten. You can restore these yourself if required.
 */
fun SubsamplingScaleImageView.recycle() {
    reset(true)
    bitmapPaint = null
    debugTextPaint = null
    debugLinePaint = null
    tileBgPaint = null
}

/**
 * Converts a rectangle within the view to the corresponding rectangle from the source file, taking
 * into account the current scale, translation, orientation and clipped region. This can be used
 * to decode a bitmap from the source file.
 *
 *
 * This method will only work when the image has fully initialised, after [SubsamplingScaleImageView.isReady] ()} returns
 * true. It is not guaranteed to work with preloaded bitmaps.
 *
 *
 * The result is written to the fRect argument. Re-use a single instance for efficiency.
 *
 * @param vRect rectangle representing the view area to interpret.
 * @param fRect rectangle instance to which the result will be written. Re-use for efficiency.
 */
fun SubsamplingScaleImageView.viewToFileRect(vRect: Rect, fRect: Rect) {
    if (vTranslate == null || !readySent) {
        return
    }
    fRect.set(
            viewToSourceX(vRect.left.toFloat()).toInt(),
            viewToSourceY(vRect.top.toFloat()).toInt(),
            viewToSourceX(vRect.right.toFloat()).toInt(),
            viewToSourceY(vRect.bottom.toFloat()).toInt())
    fileSRect(fRect, fRect)
    fRect.set(
            max(0, fRect.left),
            max(0, fRect.top),
            min(sWidth, fRect.right),
            min(sHeight, fRect.bottom)
    )
    sRegion?.let { fRect.offset(it.left, it.top) }
}

/**
 * Find the area of the source file that is currently visible on screen, taking into account the
 * current scale, translation, orientation and clipped region. This is a convenience method; see
 * [.viewToFileRect].
 *
 * @param fRect rectangle instance to which the result will be written. Re-use for efficiency.
 */
fun SubsamplingScaleImageView.visibleFileRect(fRect: Rect) {
    if (vTranslate == null || !readySent) {
        return
    }
    fRect.set(0, 0, width, height)
    viewToFileRect(fRect, fRect)
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
 * Swap the default region decoder implementation for one of your own. You must do this before setting the image file or
 * asset, and you cannot use a custom decoder when using layout XML to set an asset name. Your class must have a
 * public default constructor.
 *
 * @param regionDecoderClass The [ImageRegionDecoder] implementation to use.
 */
fun SubsamplingScaleImageView.setRegionDecoderClass(regionDecoderClass: Class<out ImageRegionDecoder>) {
    this.regionDecoderFactory = CompatDecoderFactory(regionDecoderClass)
}

/**
 * Swap the default region decoder implementation for one of your own. You must do this before setting the image file or
 * asset, and you cannot use a custom decoder when using layout XML to set an asset name.
 *
 * @param regionDecoderFactory The [DecoderFactory] implementation that produces [ImageRegionDecoder]
 * instances.
 */
fun SubsamplingScaleImageView.setRegionDecoderFactory(regionDecoderFactory: DecoderFactory<out ImageRegionDecoder>) {
    this.regionDecoderFactory = regionDecoderFactory
}

/**
 * Swap the default bitmap decoder implementation for one of your own. You must do this before setting the image file or
 * asset, and you cannot use a custom decoder when using layout XML to set an asset name. Your class must have a
 * public default constructor.
 *
 * @param bitmapDecoderClass The [ImageDecoder] implementation to use.
 */
fun SubsamplingScaleImageView.setBitmapDecoderClass(bitmapDecoderClass: Class<out ImageDecoder>) {
    this.bitmapDecoderFactory = CompatDecoderFactory(bitmapDecoderClass)
}

/**
 * Swap the default bitmap decoder implementation for one of your own. You must do this before setting the image file or
 * asset, and you cannot use a custom decoder when using layout XML to set an asset name.
 *
 * @param bitmapDecoderFactory The [DecoderFactory] implementation that produces [ImageDecoder] instances.
 */
fun SubsamplingScaleImageView.setBitmapDecoderFactory(bitmapDecoderFactory: DecoderFactory<out ImageDecoder>) {
    this.bitmapDecoderFactory = bitmapDecoderFactory
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

/**
 * Set the pan limiting style. See static fields. Normally [ViewValues.PAN_LIMIT_INSIDE] is best, for image galleries.
 *
 * @param panLimit a pan limit constant. See static fields.
 */
fun SubsamplingScaleImageView.setPanLimit(panLimit: Int) {
    require(ViewValues.VALID_PAN_LIMITS.contains(panLimit)) { "Invalid pan limit: $panLimit" }
    this.panLimit = panLimit
    if (isReady()) {
        fitToBounds(true)
        invalidate()
    }
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
 * Set the minimum scale allowed. A value of 1 means 1:1 pixels at minimum scale. You may wish to set this according
 * to screen density. Consider using [.setMaximumDpi], which is density aware.
 *
 * @param minScale minimum scale expressed as a source/view pixels ratio.
 */
fun SubsamplingScaleImageView.setMinScale(minScale: Float) {
    this.minScale = minScale
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
 * Returns the minimum allowed scale.
 *
 * @return the minimum scale as a source/view pixels ratio.
 */
fun SubsamplingScaleImageView.getMinScale(): Float {
    return minScale()
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
 * Call to find whether the main image (base layer tiles where relevant) have been loaded. Before
 * this event the view is blank unless a preview was provided.
 *
 * @return true if the main image (not the preview) has been loaded and is ready to display.
 */
fun SubsamplingScaleImageView.isImageLoaded(): Boolean {
    return imageLoadedSent
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
 * Set the scale the image will zoom in to when double tapped. This also the scale point where a double tap is interpreted
 * as a zoom out gesture - if the scale is greater than 90% of this value, a double tap zooms out. Avoid using values
 * greater than the max zoom.
 *
 * @param doubleTapZoomScale New value for double tap gesture zoom scale.
 */
fun SubsamplingScaleImageView.setDoubleTapZoomScale(doubleTapZoomScale: Float) {
    this.doubleTapZoomScale = doubleTapZoomScale
}

/**
 * A density aware alternative to [.setDoubleTapZoomScale]; this allows you to express the scale the
 * image will zoom in to when double tapped in terms of the image pixel density. Values lower than the max scale will
 * be ignored. A sensible starting point is 160 - the default used by this view.
 *
 * @param dpi New value for double tap gesture zoom scale.
 */
fun SubsamplingScaleImageView.setDoubleTapZoomDpi(dpi: Int) {
    val metrics = resources.displayMetrics
    val averageDpi = (metrics.xdpi + metrics.ydpi) / 2
    setDoubleTapZoomScale(averageDpi / dpi)
}

/**
 * Set the type of zoom animation to be used for double taps. See static fields.
 *
 * @param doubleTapZoomStyle New value for zoom style.
 */
fun SubsamplingScaleImageView.setDoubleTapZoomStyle(doubleTapZoomStyle: Int) {
    require(ViewValues.VALID_ZOOM_STYLES.contains(doubleTapZoomStyle)) { "Invalid zoom style: $doubleTapZoomStyle" }
    this.doubleTapZoomStyle = doubleTapZoomStyle
}

/**
 * Set the duration of the double tap zoom animation.
 *
 * @param durationMs Duration in milliseconds.
 */
fun SubsamplingScaleImageView.setDoubleTapZoomDuration(durationMs: Int) {
    this.doubleTapZoomDuration = max(0, durationMs)
}

/**
 *
 *
 * Provide an [Executor] to be used for loading images. By default, [AsyncTask.THREAD_POOL_EXECUTOR]
 * is used to minimise contention with other background work the app is doing. You can also choose
 * to use [AsyncTask.SERIAL_EXECUTOR] if you want to limit concurrent background tasks.
 * Alternatively you can supply an [Executor] of your own to avoid any contention. It is
 * strongly recommended to use a single executor instance for the life of your application, not
 * one per view instance.
 *
 *
 * **Warning:** If you are using a custom implementation of [ImageRegionDecoder], and you
 * supply an executor with more than one thread, you must make sure your implementation supports
 * multi-threaded bitmap decoding or has appropriate internal synchronization. From SDK 21, Android's
 * [android.graphics.BitmapRegionDecoder] uses an internal lock so it is thread safe but
 * there is no advantage to using multiple threads.
 *
 *
 * @param executor an [Executor] for image loading.
 */
fun SubsamplingScaleImageView.setExecutor(executor: Executor) {
    this.executor = executor
}

/**
 * Enable or disable eager loading of tiles that appear on screen during gestures or animations,
 * while the gesture or animation is still in progress. By default this is enabled to improve
 * responsiveness, but it can result in tiles being loaded and discarded more rapidly than
 * necessary and reduce the animation frame rate on old/cheap devices. Disable this on older
 * devices if you see poor performance. Tiles will then be loaded only when gestures and animations
 * are completed.
 *
 * @param eagerLoadingEnabled true to enable loading during gestures, false to delay loading until gestures end
 */
fun SubsamplingScaleImageView.setEagerLoadingEnabled(eagerLoadingEnabled: Boolean) {
    this.eagerLoadingEnabled = eagerLoadingEnabled
}

/**
 * Enables visual debugging, showing tile boundaries and sizes.
 *
 * @param debug true to enable debugging, false to disable.
 */
fun SubsamplingScaleImageView.setDebug(debug: Boolean) {
    this.debug = debug
}

/**
 * Check if an image has been set. The image may not have been loaded and displayed yet.
 *
 * @return If an image is currently set.
 */
fun SubsamplingScaleImageView.hasImage(): Boolean {
    return uri != null || bitmap != null
}

/**
 * Add a listener allowing notification of load and error events. Extend [OnImageEventListener.DefaultOnImageEventListener]
 * to simplify implementation.
 *
 * @param onImageEventListener an [OnImageEventListener] instance.
 */
fun SubsamplingScaleImageView.setOnImageEventListener(onImageEventListener: OnImageEventListener) {
    this.onImageEventListener = onImageEventListener
}

/**
 * Add a listener for pan and zoom events. Extend [OnStateChangedListener.DefaultOnStateChangedListener] to simplify
 * implementation.
 *
 * @param onStateChangedListener an [OnStateChangedListener] instance.
 */
fun SubsamplingScaleImageView.setOnStateChangedListener(onStateChangedListener: OnStateChangedListener) {
    this.onStateChangedListener = onStateChangedListener
}

fun SubsamplingScaleImageView.getSupportMatrix(): Matrix? {
    return supportMatrix
}

fun SubsamplingScaleImageView.getBitmap(): Bitmap? {
    return bitmap
}