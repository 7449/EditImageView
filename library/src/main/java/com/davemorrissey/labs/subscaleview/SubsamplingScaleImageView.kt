package com.davemorrissey.labs.subscaleview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.AsyncTask
import android.os.Handler
import android.util.AttributeSet
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.davemorrissey.labs.subscaleview.temp.*
import com.davemorrissey.labs.subscaleview.temp.decoder.*
import com.davemorrissey.labs.subscaleview.temp.decoder.ImageDecoder
import com.davemorrissey.labs.subscaleview.temp.gesture.DetectorListener
import com.davemorrissey.labs.subscaleview.temp.gesture.SingleDetectorListener
import com.davemorrissey.labs.subscaleview.temp.listener.OnImageEventListener
import com.davemorrissey.labs.subscaleview.temp.listener.OnStateChangedListener
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.math.max
import kotlin.math.min

/**
 *
 *
 * Displays an image subsampled as necessary to avoid loading too much image data into memory. After zooming in,
 * a set of image tiles subsampled at higher resolution are loaded and displayed over the base layer. During pan and
 * zoom, tiles off screen or higher/lower resolution than required are discarded from memory.
 *
 *
 * Tiles are no larger than the max supported bitmap size, so with large images tiling may be used even when zoomed out.
 *
 *
 * v prefixes - coordinates, translations and distances measured in screen (view) pixels
 * <br></br>
 * s prefixes - coordinates, translations and distances measured in rotated and cropped source image pixels (scaled)
 * <br></br>
 * f prefixes - coordinates, translations and distances measured in original unrotated, uncropped source file pixels
 *
 *
 * [View project on GitHub](https://github.com/davemorrissey/subsampling-scale-image-view)
 *
 */
open class SubsamplingScaleImageView @JvmOverloads constructor(context: Context, attr: AttributeSet? = null) : View(context, attr) {

    companion object {

        val TAG = SubsamplingScaleImageView::class.java.simpleName

        // overrides for the dimensions of the generated tiles
        const val TILE_SIZE_AUTO = Integer.MAX_VALUE
        const val MESSAGE_LONG_CLICK = 1
        // A global preference for bitmap format, available to decoder classes that respect it
        var preferredBitmapConfig: Bitmap.Config? = null
    }

    internal val decoderLock: ReadWriteLock = ReentrantReadWriteLock(true)

    // Long click handler
    private val clickHandler: Handler

    // Scale and center animation tracking
    internal var anim: Anim? = null

    //The logical density of the display
    internal val density: Float

    // Current quickscale state
    internal val quickScaleThreshold: Float

    // Fling detector
    private lateinit var detector: GestureDetector
    private lateinit var singleDetector: GestureDetector

    private val srcArray = FloatArray(8)
    private val dstArray = FloatArray(8)

    // Bitmap (preview or full image)
    internal var bitmap: Bitmap? = null

    // Overlay tile boundaries and other info
    var debug: Boolean = false

    // Whether the bitmap is a preview image
    var bitmapIsPreview: Boolean = false
    // Specifies if a cache handler is also referencing the bitmap. Do not recycle if so.
    var bitmapIsCached: Boolean = false

    // Whether tiles should be loaded while gestures and animations are still in progress
    var eagerLoadingEnabled = true

    // Gesture detection settings
    var panEnabled = true
    var zoomEnabled = true
    var quickScaleEnabled = true

    // Is two-finger zooming in progress
    var isZooming: Boolean = false
    // Is one-finger panning in progress
    var isPanning: Boolean = false
    // Is quick-scale gesture in progress
    var isQuickScaling: Boolean = false

    var sRegion: Rect? = null
    var pRegion: Rect? = null

    // Event listener
    var onImageEventListener: OnImageEventListener? = null
    // Scale and center listener
    var onStateChangedListener: OnStateChangedListener? = null
    // Long click listener
    var onViewLongClickListener: OnLongClickListener? = null

    // Image orientation setting
    var orientation = ViewValues.ORIENTATION_0

    // An executor service for loading of images
    var executor: Executor = AsyncTask.THREAD_POOL_EXECUTOR

    // Uri of full size image
    var uri: Uri? = null
    // Sample size used to display the whole image when fully zoomed out
    var fullImageSampleSize: Int = 0
    // Map of zoom level to tile grid
    var tileMap: LinkedHashMap<Int, ArrayList<Tile>>? = null
    // Max scale allowed (prevent infinite zoom)
    var maxScale = 2f
    // Density to reach before loading higher resolution tiles
    var minimumTileDpi = -1
    // Pan limiting style
    var panLimit = ViewValues.PAN_LIMIT_INSIDE
    // Minimum scale type
    var minimumScaleType = ViewValues.SCALE_TYPE_CENTER_INSIDE
    var maxTileWidth = TILE_SIZE_AUTO
    var maxTileHeight = TILE_SIZE_AUTO
    // Double tap zoom behaviour
    var doubleTapZoomScale = 1f
    var doubleTapZoomStyle = ViewValues.ZOOM_FOCUS_FIXED
    var doubleTapZoomDuration = 500
    // Current scale and scale at start of zoom
    var scale: Float = 0.toFloat()
    var scaleStart: Float = 0.toFloat()
    // Screen coordinate of top-left corner of source image
    var vTranslate: PointF? = null
    var vTranslateStart: PointF? = null
    var vTranslateBefore: PointF? = null
    // Source coordinate to center on, used when new position is set externally before view is ready
    var pendingScale: Float? = null
    var sPendingCenter: PointF? = null
    var sRequestedCenter: PointF? = null
    // Source image dimensions and orientation - dimensions relate to the unrotated image
    var sWidth: Int = 0
    var sHeight: Int = 0
    var sOrientation: Int = 0
    // Min scale allowed (prevent infinite zoom)
    var minScale = this.minScale()
    // Max touches used in current gesture
    var maxTouchCount: Int = 0
    // Tile and image decoding
    var decoder: ImageRegionDecoder? = null
    var bitmapDecoderFactory: DecoderFactory<out ImageDecoder> = CompatDecoderFactory<ImageDecoder>(SkiaImageDecoder::class.java)
    var regionDecoderFactory: DecoderFactory<out ImageRegionDecoder> = CompatDecoderFactory<ImageRegionDecoder>(SkiaImageRegionDecoder::class.java)
    // Debug values
    var vCenterStart: PointF? = null
    var vDistStart: Float = 0.toFloat()
    var quickScaleLastDistance: Float = 0.toFloat()
    var quickScaleMoved: Boolean = false
    var quickScaleVLastPoint: PointF? = null
    var quickScaleSCenter: PointF? = null
    var quickScaleVStart: PointF? = null
    // Whether a ready notification has been sent to subclasses
    var readySent: Boolean = false
    // Whether a base layer loaded notification has been sent to subclasses
    var imageLoadedSent: Boolean = false
    // Paint objects created once and reused for efficiency
    var bitmapPaint: Paint? = null
    var debugTextPaint: Paint? = null
    var debugLinePaint: Paint? = null
    var tileBgPaint: Paint? = null
    // Volatile fields used to reduce object creation
    var satTemp: ScaleAndTranslate? = null
    var supportMatrix: Matrix? = null
    var sRect: RectF? = null

    init {
        clickHandler = Handler(Handler.Callback { message ->
            if (message.what == MESSAGE_LONG_CLICK && onViewLongClickListener != null) {
                maxTouchCount = 0
                super@SubsamplingScaleImageView.setOnLongClickListener(onViewLongClickListener)
                performLongClick()
                super@SubsamplingScaleImageView.setOnLongClickListener(null)
            }
            true
        })
        density = resources.displayMetrics.density
        setMinimumDpi(160)
        setDoubleTapZoomDpi(160)
        setMinimumTileDpi(320)
        setGestureDetector(context)
        quickScaleThreshold = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20f, context.resources.displayMetrics)
    }

    fun setGestureDetector(context: Context) {
        detector = GestureDetector(context, DetectorListener(this))
        singleDetector = GestureDetector(context, SingleDetectorListener(this))
    }

    /**
     * On resize, preserve center and scale. Various behaviours are possible, override this method to use another.
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        debug("onSizeChanged %dx%d -> %dx%d", oldw, oldh, w, h)
        val sCenter = getCenter()
        if (readySent && sCenter != null) {
            anim = null
            pendingScale = scale
            sPendingCenter = sCenter
        }
    }

    /**
     * Measures the width and height of the view, preserving the aspect ratio of the image displayed if wrap_content is
     * used. The image will scale within this box, not resizing the view as it is zoomed.
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
        val parentWidth = MeasureSpec.getSize(widthMeasureSpec)
        val parentHeight = MeasureSpec.getSize(heightMeasureSpec)
        val resizeWidth = widthSpecMode != MeasureSpec.EXACTLY
        val resizeHeight = heightSpecMode != MeasureSpec.EXACTLY
        var width = parentWidth
        var height = parentHeight
        if (sWidth > 0 && sHeight > 0) {
            if (resizeWidth && resizeHeight) {
                width = sWidth()
                height = sHeight()
            } else if (resizeHeight) {
                height = (sHeight().toDouble() / sWidth().toDouble() * width).toInt()
            } else if (resizeWidth) {
                width = (sWidth().toDouble() / sHeight().toDouble() * height).toInt()
            }
        }
        width = max(width, suggestedMinimumWidth)
        height = max(height, suggestedMinimumHeight)
        setMeasuredDimension(width, height)
    }

    /**
     * Handle touch events. One finger pans, and two finger pinch and zoom plus panning.
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // During non-interruptible anims, ignore all touch events

        if (anim != null && anim?.interruptible == false) {
            requestDisallowInterceptTouchEvent(true)
            return true
        } else {
            anim?.listener?.onInterruptedByUser()
            anim = null
        }

        // Abort if not ready
        if (vTranslate == null) {
            singleDetector.onTouchEvent(event)
            return true
        }
        // Detect flings, taps and double taps
        if (!isQuickScaling && (detector.onTouchEvent(event))) {
            isZooming = false
            isPanning = false
            maxTouchCount = 0
            return true
        }

        if (vTranslateStart == null) {
            vTranslateStart = PointF(0f, 0f)
        }
        if (vTranslateBefore == null) {
            vTranslateBefore = PointF(0f, 0f)
        }
        if (vCenterStart == null) {
            vCenterStart = PointF(0f, 0f)
        }

        // Store current values so we can send an event if they change
        val scaleBefore = scale

        safeLet(vTranslateBefore, vTranslate) { vTranslateBefore, vTranslate -> vTranslateBefore.set(vTranslate) }

        val handled = onTouchEventInternal(event)
        sendStateChanged(scaleBefore, vTranslateBefore, ViewValues.ORIGIN_TOUCH)
        return handled || super.onTouchEvent(event)
    }

    /**
     * Draw method should not be called until the view has dimensions so the first calls are used as triggers to calculate
     * the scaling and tiling required. Once the view is setup, tiles are displayed as they are loaded.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        createPaints()

        // If image or view dimensions are not known yet, abort.
        if (sWidth == 0 || sHeight == 0 || width == 0 || height == 0) {
            return
        }

        // When using tiles, on first render with no tile map ready, initialise it and kick off async base image loading.
        if (tileMap == null && decoder != null) {
            initialiseBaseLayer(getMaxBitmapDimensions(canvas))
        }

        // If image has been loaded or supplied as a bitmap, onDraw may be the first time the view has
        // dimensions and therefore the first opportunity to set scale and translate. If this call returns
        // false there is nothing to be drawn so return immediately.
        if (!checkReady()) {
            return
        }

        // Set scale and translate before draw.
        preDraw()

        // If animating scale, calculate current scale and center with easing equations
        if (anim != null && anim?.vFocusStart != null) {
            // Store current values so we can send an event if they change
            val scaleBefore = scale
            if (vTranslateBefore == null) {
                vTranslateBefore = PointF(0f, 0f)
            }
            vTranslate?.let { vTranslate -> vTranslateBefore?.set(vTranslate) }
            var finished = false
            anim?.let { anim ->
                var scaleElapsed = System.currentTimeMillis() - anim.time
                finished = scaleElapsed > anim.duration
                scaleElapsed = min(scaleElapsed, anim.duration)
                scale = ease(anim.easing, scaleElapsed, anim.scaleStart, anim.scaleEnd - anim.scaleStart, anim.duration)

                safeLet(anim.vFocusStart, anim.vFocusEnd, anim.sCenterEnd, vTranslate) { vFocusStart, vFocusEnd, sCenterEnd, vTranslate ->
                    // Apply required animation to the focal point
                    val vFocusNowX = ease(anim.easing, scaleElapsed, vFocusStart.x, vFocusEnd.x - vFocusStart.x, anim.duration)
                    val vFocusNowY = ease(anim.easing, scaleElapsed, vFocusStart.y, vFocusEnd.y - vFocusStart.y, anim.duration)
                    // Find out where the focal point is at this scale and adjust its position to follow the animation path
                    vTranslate.x -= sourceToViewX(sCenterEnd.x) - vFocusNowX
                    vTranslate.y -= sourceToViewY(sCenterEnd.y) - vFocusNowY
                }

                // For translate anims, showing the image non-centered is never allowed, for scaling anims it is during the animation.
                fitToBounds(finished || anim.scaleStart == anim.scaleEnd)
                sendStateChanged(scaleBefore, vTranslateBefore, anim.origin)
            }

            refreshRequiredTiles(finished)
            if (finished) {
                anim?.listener?.onComplete()
                anim = null
            }
            invalidate()
        }

        if (tileMap != null && isBaseLayerReady()) {

            tileMap?.let { tileMap ->
                // Optimum sample size for current scale
                val sampleSize = min(fullImageSampleSize, calculateInSampleSize(scale))

                // First check for missing tiles - if there are any we need the base layer underneath to avoid gaps
                var hasMissingTiles = false
                for ((key, value) in tileMap) {
                    if (key == sampleSize) {
                        for (tile in value) {
                            if (tile.visible && (tile.loading || tile.bitmap == null)) {
                                hasMissingTiles = true
                            }
                        }
                    }
                }
                // Render all loaded tiles. LinkedHashMap used for bottom up rendering - lower res tiles underneath.
                for ((key, value) in tileMap) {
                    if (key == sampleSize || hasMissingTiles) {
                        for (tile in value) {
                            safeLet(tile.vRect, tile.sRect) { vRect, sRect -> sourceToViewRect(sRect, vRect) }
                            if (!tile.loading && tile.bitmap != null) {
                                tile.bitmap?.let { bitmap ->
                                    safeLet(tile.vRect, tileBgPaint) { vRect, tileBgPaint -> canvas.drawRect(vRect, tileBgPaint) }
                                    if (supportMatrix == null) {
                                        supportMatrix = Matrix()
                                    }
                                    supportMatrix?.reset()
                                    setMatrixArray(srcArray, 0f, 0f, bitmap.width.toFloat(), 0f, bitmap.width.toFloat(), bitmap.height.toFloat(), 0f, bitmap.height.toFloat())
                                    tile.vRect?.let { vRect ->
                                        when {
                                            getRequiredRotation() == ViewValues.ORIENTATION_0 -> setMatrixArray(dstArray, vRect.left.toFloat(), vRect.top.toFloat(), vRect.right.toFloat(), vRect.top.toFloat(), vRect.right.toFloat(), vRect.bottom.toFloat(), vRect.left.toFloat(), vRect.bottom.toFloat())
                                            getRequiredRotation() == ViewValues.ORIENTATION_90 -> setMatrixArray(dstArray, vRect.right.toFloat(), vRect.top.toFloat(), vRect.right.toFloat(), vRect.bottom.toFloat(), vRect.left.toFloat(), vRect.bottom.toFloat(), vRect.left.toFloat(), vRect.top.toFloat())
                                            getRequiredRotation() == ViewValues.ORIENTATION_180 -> setMatrixArray(dstArray, vRect.right.toFloat(), vRect.bottom.toFloat(), vRect.left.toFloat(), vRect.bottom.toFloat(), vRect.left.toFloat(), vRect.top.toFloat(), vRect.right.toFloat(), vRect.top.toFloat())
                                            getRequiredRotation() == ViewValues.ORIENTATION_270 -> setMatrixArray(dstArray, vRect.left.toFloat(), vRect.bottom.toFloat(), vRect.left.toFloat(), vRect.top.toFloat(), vRect.right.toFloat(), vRect.top.toFloat(), vRect.right.toFloat(), vRect.bottom.toFloat())
                                        }
                                    }
                                    supportMatrix?.setPolyToPoly(srcArray, 0, dstArray, 0, 4)
                                    supportMatrix?.let { supportMatrix -> canvas.drawBitmap(bitmap, supportMatrix, bitmapPaint) }
                                }
                                if (debug) {
                                    safeLet(tile.vRect, debugTextPaint) { vRect, debugTextPaint -> canvas.drawRect(vRect, debugTextPaint) }
                                }
                            } else if (tile.loading && debug) {
                                safeLet(tile.vRect, debugTextPaint) { vRect, debugTextPaint ->
                                    canvas.drawText("LOADING", (vRect.left + px(5)).toFloat(), (vRect.top + px(35)).toFloat(), debugTextPaint)
                                }
                            }
                            if (tile.visible && debug) {
                                safeLet(tile.vRect, tile.sRect, debugTextPaint) { vRect, sRect, debugTextPaint ->
                                    canvas.drawText("ISS " + tile.sampleSize + " RECT " + sRect.top + "," + sRect.left + "," + sRect.bottom + "," + sRect.right, (vRect.left + px(5)).toFloat(), (vRect.top + px(15)).toFloat(), debugTextPaint)
                                }
                            }
                        }
                    }
                }
            }
        } else if (bitmap != null) {
            if (supportMatrix == null) {
                supportMatrix = Matrix()
            }
            safeLet(bitmap, supportMatrix, vTranslate) { bitmap, supportMatrix, vTranslate ->
                var xScale = scale
                var yScale = scale
                if (bitmapIsPreview) {
                    xScale = scale * (sWidth.toFloat() / bitmap.width)
                    yScale = scale * (sHeight.toFloat() / bitmap.height)
                }
                supportMatrix.reset()
                supportMatrix.postScale(xScale, yScale)
                supportMatrix.postRotate(getRequiredRotation().toFloat())
                supportMatrix.postTranslate(vTranslate.x, vTranslate.y)
                when {
                    getRequiredRotation() == ViewValues.ORIENTATION_180 -> supportMatrix.postTranslate(scale * sWidth, scale * sHeight)
                    getRequiredRotation() == ViewValues.ORIENTATION_90 -> supportMatrix.postTranslate(scale * sHeight, 0f)
                    getRequiredRotation() == ViewValues.ORIENTATION_270 -> supportMatrix.postTranslate(0f, scale * sWidth)
                }
                tileBgPaint?.let { tileBgPaint ->
                    if (sRect == null) {
                        sRect = RectF()
                    }
                    sRect?.set(0f, 0f, (if (bitmapIsPreview) bitmap.width else sWidth).toFloat(), (if (bitmapIsPreview) bitmap.height else sHeight).toFloat())
                    supportMatrix.mapRect(sRect)
                    sRect?.let { sRect -> canvas.drawRect(sRect, tileBgPaint) }
                }
                canvas.drawBitmap(bitmap, supportMatrix, bitmapPaint)
            }
        }

        if (debug) {
            safeLet(getCenter(), debugTextPaint, vTranslate) { centers, debugTextPaint, vTranslate ->
                canvas.drawText("Scale: " + String.format(Locale.ENGLISH, "%.2f", scale) + " (" + String.format(Locale.ENGLISH, "%.2f", minScale()) + " - " + String.format(Locale.ENGLISH, "%.2f", maxScale) + ")", px(5).toFloat(), px(15).toFloat(), debugTextPaint)
                canvas.drawText("Translate: " + String.format(Locale.ENGLISH, "%.2f", vTranslate.x) + ":" + String.format(Locale.ENGLISH, "%.2f", vTranslate.y), px(5).toFloat(), px(30).toFloat(), debugTextPaint)
                canvas.drawText("Source center: " + String.format(Locale.ENGLISH, "%.2f", centers.x) + ":" + String.format(Locale.ENGLISH, "%.2f", centers.y), px(5).toFloat(), px(45).toFloat(), debugTextPaint)
            }
            debugLinePaint?.let { debugLinePaint ->
                safeLet(anim?.sCenterStart, anim?.sCenterEndRequested, anim?.sCenterEnd) { sCenterStart, sCenterEndRequested, sCenterEnd ->
                    safeLet(sourceToViewCoord(sCenterStart), sourceToViewCoord(sCenterEndRequested), sourceToViewCoord(sCenterEnd)) { vCenterStart, vCenterEndRequested, vCenterEnd ->
                        canvas.drawCircle(vCenterStart.x, vCenterStart.y, px(10).toFloat(), debugLinePaint)
                        debugLinePaint.color = Color.RED
                        canvas.drawCircle(vCenterEndRequested.x, vCenterEndRequested.y, px(20).toFloat(), debugLinePaint)
                        debugLinePaint.color = Color.BLUE
                        canvas.drawCircle(vCenterEnd.x, vCenterEnd.y, px(25).toFloat(), debugLinePaint)
                    }
                }
                debugLinePaint.color = Color.CYAN
                canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), px(30).toFloat(), debugLinePaint)
                vCenterStart?.let { vCenterStart ->
                    debugLinePaint.color = Color.RED
                    canvas.drawCircle(vCenterStart.x, vCenterStart.y, px(20).toFloat(), debugLinePaint)
                }
                quickScaleSCenter?.let { quickScaleSCenter ->
                    debugLinePaint.color = Color.BLUE
                    canvas.drawCircle(sourceToViewX(quickScaleSCenter.x), sourceToViewY(quickScaleSCenter.y), px(35).toFloat(), debugLinePaint)
                }
                quickScaleVStart?.let { quickScaleVStart ->
                    if (isQuickScaling) {
                        debugLinePaint.color = Color.CYAN
                        canvas.drawCircle(quickScaleVStart.x, quickScaleVStart.y, px(30).toFloat(), debugLinePaint)
                    }
                }
                debugLinePaint.color = Color.MAGENTA
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun setOnLongClickListener(onLongClickListener: OnLongClickListener?) {
        this.onViewLongClickListener = onLongClickListener
    }
}
