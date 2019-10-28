package com.davemorrissey.labs.subscaleview

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.AsyncTask
import com.davemorrissey.labs.subscaleview.core.ViewValues
import com.davemorrissey.labs.subscaleview.decoder.CompatDecoderFactory
import com.davemorrissey.labs.subscaleview.decoder.DecoderFactory
import com.davemorrissey.labs.subscaleview.decoder.ImageDecoder
import com.davemorrissey.labs.subscaleview.decoder.ImageRegionDecoder
import com.davemorrissey.labs.subscaleview.listener.OnImageEventListener
import com.davemorrissey.labs.subscaleview.listener.OnStateChangedListener
import com.davemorrissey.labs.subscaleview.task.BitmapLoadTask
import com.davemorrissey.labs.subscaleview.task.TilesInitTask
import java.util.concurrent.Executor
import kotlin.math.max
import kotlin.math.min


/**
 * Set the pan limiting style. See static fields. Normally [ViewValues.PAN_LIMIT_INSIDE] is best, for image galleries.
 *
 * @param panLimit a pan limit constant. See static fields.
 */
fun SubsamplingScaleImageView.setPanLimit(panLimit: Int) {
    require(ViewValues.VALID_PAN_LIMITS.contains(panLimit)) { "Invalid pan limit: $panLimit" }
    this.panLimit = panLimit
    if (this.isReady()) {
        fitToBounds(true)
        invalidate()
    }
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
    this.doubleTapZoomDuration = Math.max(0, durationMs)
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
 * Converts a rectangle within the view to the corresponding rectangle from the source file, taking
 * into account the current scale, translation, orientation and clipped region. This can be used
 * to decode a bitmap from the source file.
 *
 *
 * This method will only work when the image has fully initialised, after [SubsamplingScaleImageViewXKt.isReady] ()} returns
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
            this.viewToSourceX(vRect.left.toFloat()).toInt(),
            this.viewToSourceY(vRect.top.toFloat()).toInt(),
            this.viewToSourceX(vRect.right.toFloat()).toInt(),
            this.viewToSourceY(vRect.bottom.toFloat()).toInt())
    this.fileSRect(fRect, fRect)
    fRect.set(
            max(0, fRect.left),
            max(0, fRect.top),
            min(sWidth, fRect.right),
            min(sHeight, fRect.bottom)
    )
    if (sRegion != null) {
        fRect.offset(sRegion!!.left, sRegion!!.top)
    }
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
            this.onPreviewLoaded(previewSource.getBitmap())
        } else {
            var uri = previewSource.getUri()
            if (uri == null && previewSource.getResource() != ImageSource.DEFAULT_RESOURCE) {
                uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.packageName + "/" + previewSource.getResource())
            }
            val task = BitmapLoadTask(this, context, bitmapDecoderFactory, uri!!, true)
            this.execute(task)
        }
    }

    if (imageSource.getBitmap() != null && imageSource.getSRegion() != null) {
        this.onImageLoaded(Bitmap.createBitmap(imageSource.getBitmap()!!, imageSource.getSRegion()!!.left, imageSource.getSRegion()!!.top, imageSource.getSRegion()!!.width(), imageSource.getSRegion()!!.height()), ViewValues.ORIENTATION_0, false)
    } else if (imageSource.getBitmap() != null) {
        this.onImageLoaded(imageSource.getBitmap(), ViewValues.ORIENTATION_0, imageSource.isCached())
    } else {
        sRegion = imageSource.getSRegion()
        uri = imageSource.getUri()
        if (uri == null && imageSource.getResource() != ImageSource.DEFAULT_RESOURCE) {
            uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.packageName + "/" + imageSource.getResource())
        }
        if (imageSource.getTile() || sRegion != null) {
            // Load the bitmap using tile decoding.
            val task = TilesInitTask(this, context, regionDecoderFactory, uri!!)
            this.execute(task)
        } else {
            // Load the bitmap as a single image.
            val task = BitmapLoadTask(this, context, bitmapDecoderFactory, uri!!, false)
            this.execute(task)
        }
    }
}