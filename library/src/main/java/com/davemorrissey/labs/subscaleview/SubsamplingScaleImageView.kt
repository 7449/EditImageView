package com.davemorrissey.labs.subscaleview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.AsyncTask
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.davemorrissey.labs.subscaleview.core.*
import com.davemorrissey.labs.subscaleview.decoder.*
import com.davemorrissey.labs.subscaleview.decoder.ImageDecoder
import com.davemorrissey.labs.subscaleview.listener.OnImageEventListener
import com.davemorrissey.labs.subscaleview.listener.OnStateChangedListener
import java.util.*
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.math.abs
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

    val decoderLock: ReadWriteLock = ReentrantReadWriteLock(true)

    // Current quickscale state
    val quickScaleThreshold: Float
    // Long click handler
    val clickHandler: Handler
    val srcArray = FloatArray(8)
    val dstArray = FloatArray(8)
    //The logical density of the display
    val density: Float
    // Bitmap (preview or full image)
    var bitmap: Bitmap? = null
    // Whether the bitmap is a preview image
    var bitmapIsPreview: Boolean = false
    // Specifies if a cache handler is also referencing the bitmap. Do not recycle if so.
    var bitmapIsCached: Boolean = false
    var sRegion: Rect? = null
    var pRegion: Rect? = null
    // Event listener
    var onImageEventListener: OnImageEventListener? = null
    // Scale and center listener
    var onStateChangedListener: OnStateChangedListener? = null
    // Long click listener
    var onViewLongClickListener: OnLongClickListener? = null
    // Uri of full size image
    var uri: Uri? = null
    // Sample size used to display the whole image when fully zoomed out
    var fullImageSampleSize: Int = 0
    // Map of zoom level to tile grid
    var tileMap: LinkedHashMap<Int, ArrayList<Tile>>? = null
    // Overlay tile boundaries and other info
    var debug: Boolean = false
    // Image orientation setting
    var orientation = ViewValues.ORIENTATION_0
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
    // An executor service for loading of images
    var executor = AsyncTask.THREAD_POOL_EXECUTOR
    // Whether tiles should be loaded while gestures and animations are still in progress
    var eagerLoadingEnabled = true
    // Gesture detection settings
    var panEnabled = true
    var zoomEnabled = true
    var quickScaleEnabled = true
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
    // Is two-finger zooming in progress
    var isZooming: Boolean = false
    // Is one-finger panning in progress
    var isPanning: Boolean = false
    // Is quick-scale gesture in progress
    var isQuickScaling: Boolean = false
    // Max touches used in current gesture
    var maxTouchCount: Int = 0
    // Fling detector
    var detector: GestureDetector? = null
    var singleDetector: GestureDetector? = null
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
    // Scale and center animation tracking
    var anim: Anim? = null
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
        density = resources.displayMetrics.density
        this.setMinimumDpi(160)
        this.setDoubleTapZoomDpi(160)
        this.setMinimumTileDpi(320)
        setGestureDetector(context)
        this.clickHandler = Handler(Handler.Callback { message ->
            if (message.what == MESSAGE_LONG_CLICK && onViewLongClickListener != null) {
                maxTouchCount = 0
                super@SubsamplingScaleImageView.setOnLongClickListener(onViewLongClickListener)
                performLongClick()
                super@SubsamplingScaleImageView.setOnLongClickListener(null)
            }
            true
        })
        quickScaleThreshold = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20f, context.resources.displayMetrics)
    }

    fun setGestureDetector(context: Context) {
        this.detector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {

            override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
                if (panEnabled && readySent && vTranslate != null && e1 != null && e2 != null && (abs(e1.x - e2.x) > 50 || abs(e1.y - e2.y) > 50) && (abs(velocityX) > 500 || abs(velocityY) > 500) && !isZooming) {
                    val vTranslateEnd = PointF(vTranslate!!.x + velocityX * 0.25f, vTranslate!!.y + velocityY * 0.25f)
                    val sCenterXEnd = (width / 2 - vTranslateEnd.x) / scale
                    val sCenterYEnd = (height / 2 - vTranslateEnd.y) / scale
                    AnimationBuilder(this@SubsamplingScaleImageView, PointF(sCenterXEnd, sCenterYEnd)).withEasing(ViewValues.EASE_OUT_QUAD).withPanLimited(false).withOrigin(ViewValues.ORIGIN_FLING).start()
                    return true
                }
                return super.onFling(e1, e2, velocityX, velocityY)
            }

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                performClick()
                return true
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                if (zoomEnabled && readySent && vTranslate != null) {
                    // Hacky solution for #15 - after a double tap the GestureDetector gets in a state
                    // where the next fling is ignored, so here we replace it with a new one.
                    setGestureDetector(context)
                    if (quickScaleEnabled) {
                        // Store quick scale params. This will become either a double tap zoom or a
                        // quick scale depending on whether the user swipes.
                        vCenterStart = PointF(e.x, e.y)
                        vTranslateStart = PointF(vTranslate!!.x, vTranslate!!.y)
                        scaleStart = scale
                        isQuickScaling = true
                        isZooming = true
                        quickScaleLastDistance = -1f
                        quickScaleSCenter = this@SubsamplingScaleImageView.viewToSourceCoord(vCenterStart!!)
                        quickScaleVStart = PointF(e.x, e.y)
                        quickScaleVLastPoint = PointF(quickScaleSCenter!!.x, quickScaleSCenter!!.y)
                        quickScaleMoved = false
                        // We need to get events in onTouchEvent after this.
                        return false
                    } else {
                        // Start double tap zoom animation.
                        this@SubsamplingScaleImageView.doubleTapZoom(this@SubsamplingScaleImageView.viewToSourceCoord(PointF(e.x, e.y))!!, PointF(e.x, e.y))
                        return true
                    }
                }
                return super.onDoubleTapEvent(e)
            }
        })

        singleDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                performClick()
                return true
            }
        })
    }

    /**
     * On resize, preserve center and scale. Various behaviours are possible, override this method to use another.
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        this.debug("onSizeChanged %dx%d -> %dx%d", oldw, oldh, w, h)
        val sCenter = this.getCenter()
        if (readySent && sCenter != null) {
            this.anim = null
            this.pendingScale = scale
            this.sPendingCenter = sCenter
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
                width = this.sWidth()
                height = this.sHeight()
            } else if (resizeHeight) {
                height = (this.sHeight().toDouble() / this.sWidth().toDouble() * width).toInt()
            } else if (resizeWidth) {
                width = (this.sWidth().toDouble() / this.sHeight().toDouble() * height).toInt()
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
        if (anim != null && !anim!!.interruptible) {
            this.requestDisallowInterceptTouchEvent(true)
            return true
        } else {
            if (anim != null && anim!!.listener != null) {
                try {
                    anim!!.listener!!.onInterruptedByUser()
                } catch (e: Exception) {
                    Log.w(TAG, "Error thrown by animation listener", e)
                }

            }
            anim = null
        }

        // Abort if not ready
        if (vTranslate == null) {
            if (singleDetector != null) {
                singleDetector!!.onTouchEvent(event)
            }
            return true
        }
        // Detect flings, taps and double taps
        if (!isQuickScaling && (detector == null || detector!!.onTouchEvent(event))) {
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
        vTranslateBefore!!.set(vTranslate!!)

        val handled = this.onTouchEventInternal(event)
        this.sendStateChanged(scaleBefore, vTranslateBefore!!, ViewValues.ORIGIN_TOUCH)
        return handled || super.onTouchEvent(event)
    }

    /**
     * Draw method should not be called until the view has dimensions so the first calls are used as triggers to calculate
     * the scaling and tiling required. Once the view is setup, tiles are displayed as they are loaded.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        this.createPaints()

        // If image or view dimensions are not known yet, abort.
        if (sWidth == 0 || sHeight == 0 || width == 0 || height == 0) {
            return
        }

        // When using tiles, on first render with no tile map ready, initialise it and kick off async base image loading.
        if (tileMap == null && decoder != null) {
            this.initialiseBaseLayer(this.getMaxBitmapDimensions(canvas))
        }

        // If image has been loaded or supplied as a bitmap, onDraw may be the first time the view has
        // dimensions and therefore the first opportunity to set scale and translate. If this call returns
        // false there is nothing to be drawn so return immediately.
        if (!this.checkReady()) {
            return
        }

        // Set scale and translate before draw.
        this.preDraw()

        // If animating scale, calculate current scale and center with easing equations
        if (anim != null && anim!!.vFocusStart != null) {
            // Store current values so we can send an event if they change
            val scaleBefore = scale
            if (vTranslateBefore == null) {
                vTranslateBefore = PointF(0f, 0f)
            }
            vTranslateBefore!!.set(vTranslate!!)

            var scaleElapsed = System.currentTimeMillis() - anim!!.time
            val finished = scaleElapsed > anim!!.duration
            scaleElapsed = min(scaleElapsed, anim!!.duration)
            scale = this.ease(anim!!.easing, scaleElapsed, anim!!.scaleStart, anim!!.scaleEnd - anim!!.scaleStart, anim!!.duration)

            // Apply required animation to the focal point
            val vFocusNowX = this.ease(anim!!.easing, scaleElapsed, anim!!.vFocusStart!!.x, anim!!.vFocusEnd!!.x - anim!!.vFocusStart!!.x, anim!!.duration)
            val vFocusNowY = this.ease(anim!!.easing, scaleElapsed, anim!!.vFocusStart!!.y, anim!!.vFocusEnd!!.y - anim!!.vFocusStart!!.y, anim!!.duration)
            // Find out where the focal point is at this scale and adjust its position to follow the animation path
            vTranslate!!.x -= this.sourceToViewX(anim!!.sCenterEnd!!.x) - vFocusNowX
            vTranslate!!.y -= this.sourceToViewY(anim!!.sCenterEnd!!.y) - vFocusNowY

            // For translate anims, showing the image non-centered is never allowed, for scaling anims it is during the animation.
            this.fitToBounds(finished || anim!!.scaleStart == anim!!.scaleEnd)
            this.sendStateChanged(scaleBefore, vTranslateBefore!!, anim!!.origin)
            this.refreshRequiredTiles(finished)
            if (finished) {
                if (anim!!.listener != null) {
                    try {
                        anim!!.listener!!.onComplete()
                    } catch (e: Exception) {
                        Log.w(TAG, "Error thrown by animation listener", e)
                    }

                }
                anim = null
            }
            invalidate()
        }

        if (tileMap != null && this.isBaseLayerReady()) {

            // Optimum sample size for current scale
            val sampleSize = min(fullImageSampleSize, this.calculateInSampleSize(scale))

            // First check for missing tiles - if there are any we need the base layer underneath to avoid gaps
            var hasMissingTiles = false
            for ((key, value) in tileMap!!) {
                if (key == sampleSize) {
                    for (tile in value) {
                        if (tile.visible && (tile.loading || tile.bitmap == null)) {
                            hasMissingTiles = true
                        }
                    }
                }
            }

            // Render all loaded tiles. LinkedHashMap used for bottom up rendering - lower res tiles underneath.
            for ((key, value) in tileMap!!) {
                if (key == sampleSize || hasMissingTiles) {
                    for (tile in value) {
                        this.sourceToViewRect(tile.sRect!!, tile.vRect!!)
                        if (!tile.loading && tile.bitmap != null) {
                            if (tileBgPaint != null) {
                                canvas.drawRect(tile.vRect!!, tileBgPaint!!)
                            }
                            if (supportMatrix == null) {
                                supportMatrix = Matrix()
                            }
                            supportMatrix!!.reset()
                            setMatrixArray(srcArray, 0f, 0f, tile.bitmap!!.width.toFloat(), 0f, tile.bitmap!!.width.toFloat(), tile.bitmap!!.height.toFloat(), 0f, tile.bitmap!!.height.toFloat())
                            when {
                                this.getRequiredRotation() == ViewValues.ORIENTATION_0 -> setMatrixArray(dstArray, tile.vRect!!.left.toFloat(), tile.vRect!!.top.toFloat(), tile.vRect!!.right.toFloat(), tile.vRect!!.top.toFloat(), tile.vRect!!.right.toFloat(), tile.vRect!!.bottom.toFloat(), tile.vRect!!.left.toFloat(), tile.vRect!!.bottom.toFloat())
                                this.getRequiredRotation() == ViewValues.ORIENTATION_90 -> setMatrixArray(dstArray, tile.vRect!!.right.toFloat(), tile.vRect!!.top.toFloat(), tile.vRect!!.right.toFloat(), tile.vRect!!.bottom.toFloat(), tile.vRect!!.left.toFloat(), tile.vRect!!.bottom.toFloat(), tile.vRect!!.left.toFloat(), tile.vRect!!.top.toFloat())
                                this.getRequiredRotation() == ViewValues.ORIENTATION_180 -> setMatrixArray(dstArray, tile.vRect!!.right.toFloat(), tile.vRect!!.bottom.toFloat(), tile.vRect!!.left.toFloat(), tile.vRect!!.bottom.toFloat(), tile.vRect!!.left.toFloat(), tile.vRect!!.top.toFloat(), tile.vRect!!.right.toFloat(), tile.vRect!!.top.toFloat())
                                this.getRequiredRotation() == ViewValues.ORIENTATION_270 -> setMatrixArray(dstArray, tile.vRect!!.left.toFloat(), tile.vRect!!.bottom.toFloat(), tile.vRect!!.left.toFloat(), tile.vRect!!.top.toFloat(), tile.vRect!!.right.toFloat(), tile.vRect!!.top.toFloat(), tile.vRect!!.right.toFloat(), tile.vRect!!.bottom.toFloat())
                            }
                            supportMatrix!!.setPolyToPoly(srcArray, 0, dstArray, 0, 4)
                            canvas.drawBitmap(tile.bitmap!!, supportMatrix!!, bitmapPaint)
                            if (debug) {
                                canvas.drawRect(tile.vRect!!, debugLinePaint!!)
                            }
                        } else if (tile.loading && debug) {
                            canvas.drawText("LOADING", (tile.vRect!!.left + this.px(5)).toFloat(), (tile.vRect!!.top + this.px(35)).toFloat(), debugTextPaint!!)
                        }
                        if (tile.visible && debug) {
                            canvas.drawText("ISS " + tile.sampleSize + " RECT " + tile.sRect!!.top + "," + tile.sRect!!.left + "," + tile.sRect!!.bottom + "," + tile.sRect!!.right, (tile.vRect!!.left + this.px(5)).toFloat(), (tile.vRect!!.top + this.px(15)).toFloat(), debugTextPaint!!)
                        }
                    }
                }
            }

        } else if (bitmap != null) {

            var xScale = scale
            var yScale = scale
            if (bitmapIsPreview) {
                xScale = scale * (sWidth.toFloat() / bitmap!!.width)
                yScale = scale * (sHeight.toFloat() / bitmap!!.height)
            }

            if (supportMatrix == null) {
                supportMatrix = Matrix()
            }
            supportMatrix!!.reset()
            supportMatrix!!.postScale(xScale, yScale)
            supportMatrix!!.postRotate(this.getRequiredRotation().toFloat())
            supportMatrix!!.postTranslate(vTranslate!!.x, vTranslate!!.y)

            if (this.getRequiredRotation() == ViewValues.ORIENTATION_180) {
                supportMatrix!!.postTranslate(scale * sWidth, scale * sHeight)
            } else if (this.getRequiredRotation() == ViewValues.ORIENTATION_90) {
                supportMatrix!!.postTranslate(scale * sHeight, 0f)
            } else if (this.getRequiredRotation() == ViewValues.ORIENTATION_270) {
                supportMatrix!!.postTranslate(0f, scale * sWidth)
            }

            if (tileBgPaint != null) {
                if (sRect == null) {
                    sRect = RectF()
                }
                sRect!!.set(0f, 0f, (if (bitmapIsPreview) bitmap!!.width else sWidth).toFloat(), (if (bitmapIsPreview) bitmap!!.height else sHeight).toFloat())
                supportMatrix!!.mapRect(sRect)
                canvas.drawRect(sRect!!, tileBgPaint!!)
            }
            canvas.drawBitmap(bitmap!!, supportMatrix!!, bitmapPaint)

        }

        if (debug) {
            canvas.drawText("Scale: " + String.format(Locale.ENGLISH, "%.2f", scale) + " (" + String.format(Locale.ENGLISH, "%.2f", this.minScale()) + " - " + String.format(Locale.ENGLISH, "%.2f", maxScale) + ")", this.px(5).toFloat(), this.px(15).toFloat(), debugTextPaint!!)
            canvas.drawText("Translate: " + String.format(Locale.ENGLISH, "%.2f", vTranslate!!.x) + ":" + String.format(Locale.ENGLISH, "%.2f", vTranslate!!.y), this.px(5).toFloat(), this.px(30).toFloat(), debugTextPaint!!)
            val center = this.getCenter()

            canvas.drawText("Source center: " + String.format(Locale.ENGLISH, "%.2f", center!!.x) + ":" + String.format(Locale.ENGLISH, "%.2f", center.y), this.px(5).toFloat(), this.px(45).toFloat(), debugTextPaint!!)
            if (anim != null) {
                val vCenterStart = this.sourceToViewCoord(anim!!.sCenterStart!!)
                val vCenterEndRequested = this.sourceToViewCoord(anim!!.sCenterEndRequested!!)
                val vCenterEnd = this.sourceToViewCoord(anim!!.sCenterEnd!!)

                canvas.drawCircle(vCenterStart!!.x, vCenterStart.y, this.px(10).toFloat(), debugLinePaint!!)
                debugLinePaint!!.color = Color.RED

                canvas.drawCircle(vCenterEndRequested!!.x, vCenterEndRequested.y, this.px(20).toFloat(), debugLinePaint!!)
                debugLinePaint!!.color = Color.BLUE

                canvas.drawCircle(vCenterEnd!!.x, vCenterEnd.y, this.px(25).toFloat(), debugLinePaint!!)
                debugLinePaint!!.color = Color.CYAN
                canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), this.px(30).toFloat(), debugLinePaint!!)
            }
            if (vCenterStart != null) {
                debugLinePaint!!.color = Color.RED
                canvas.drawCircle(vCenterStart!!.x, vCenterStart!!.y, this.px(20).toFloat(), debugLinePaint!!)
            }
            if (quickScaleSCenter != null) {
                debugLinePaint!!.color = Color.BLUE
                canvas.drawCircle(this.sourceToViewX(quickScaleSCenter!!.x), this.sourceToViewY(quickScaleSCenter!!.y), this.px(35).toFloat(), debugLinePaint!!)
            }
            if (quickScaleVStart != null && isQuickScaling) {
                debugLinePaint!!.color = Color.CYAN
                canvas.drawCircle(quickScaleVStart!!.x, quickScaleVStart!!.y, this.px(30).toFloat(), debugLinePaint!!)
            }
            debugLinePaint!!.color = Color.MAGENTA
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun setOnLongClickListener(onLongClickListener: OnLongClickListener?) {
        this.onViewLongClickListener = onLongClickListener
    }

}
