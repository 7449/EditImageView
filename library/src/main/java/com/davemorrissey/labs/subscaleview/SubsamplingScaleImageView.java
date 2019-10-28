package com.davemorrissey.labs.subscaleview;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;

import com.davemorrissey.labs.subscaleview.core.Anim;
import com.davemorrissey.labs.subscaleview.core.AnimationBuilder;
import com.davemorrissey.labs.subscaleview.core.ScaleAndTranslate;
import com.davemorrissey.labs.subscaleview.core.Tile;
import com.davemorrissey.labs.subscaleview.core.ViewValues;
import com.davemorrissey.labs.subscaleview.decoder.CompatDecoderFactory;
import com.davemorrissey.labs.subscaleview.decoder.DecoderFactory;
import com.davemorrissey.labs.subscaleview.decoder.ImageDecoder;
import com.davemorrissey.labs.subscaleview.decoder.ImageRegionDecoder;
import com.davemorrissey.labs.subscaleview.decoder.SkiaImageDecoder;
import com.davemorrissey.labs.subscaleview.decoder.SkiaImageRegionDecoder;
import com.davemorrissey.labs.subscaleview.listener.OnImageEventListener;
import com.davemorrissey.labs.subscaleview.listener.OnStateChangedListener;
import com.davemorrissey.labs.subscaleview.task.BitmapLoadTask;
import com.davemorrissey.labs.subscaleview.task.TileLoadTask;
import com.davemorrissey.labs.subscaleview.task.TilesInitTask;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * <p>
 * Displays an image subsampled as necessary to avoid loading too much image data into memory. After zooming in,
 * a set of image tiles subsampled at higher resolution are loaded and displayed over the base layer. During pan and
 * zoom, tiles off screen or higher/lower resolution than required are discarded from memory.
 * </p><p>
 * Tiles are no larger than the max supported bitmap size, so with large images tiling may be used even when zoomed out.
 * </p><p>
 * v prefixes - coordinates, translations and distances measured in screen (view) pixels
 * <br>
 * s prefixes - coordinates, translations and distances measured in rotated and cropped source image pixels (scaled)
 * <br>
 * f prefixes - coordinates, translations and distances measured in original unrotated, uncropped source file pixels
 * </p><p>
 * <a href="https://github.com/davemorrissey/subsampling-scale-image-view">View project on GitHub</a>
 * </p>
 */
public class SubsamplingScaleImageView extends View {

    public static final String TAG = SubsamplingScaleImageView.class.getSimpleName();

    // overrides for the dimensions of the generated tiles
    public static final int TILE_SIZE_AUTO = Integer.MAX_VALUE;
    private static final int MESSAGE_LONG_CLICK = 1;
    // A global preference for bitmap format, available to decoder classes that respect it
    public static Bitmap.Config preferredBitmapConfig;

    public final ReadWriteLock decoderLock = new ReentrantReadWriteLock(true);

    // Current quickscale state
    public final float quickScaleThreshold;
    // Long click handler
    public final Handler handler;
    public final float[] srcArray = new float[8];
    public final float[] dstArray = new float[8];
    //The logical density of the display
    public final float density;
    // Bitmap (preview or full image)
    public Bitmap bitmap;
    // Whether the bitmap is a preview image
    public boolean bitmapIsPreview;
    // Specifies if a cache handler is also referencing the bitmap. Do not recycle if so.
    public boolean bitmapIsCached;
    public Rect sRegion;
    public Rect pRegion;
    // Event listener
    public OnImageEventListener onImageEventListener;
    // Scale and center listener
    public OnStateChangedListener onStateChangedListener;
    // Long click listener
    public OnLongClickListener onLongClickListener;
    // Uri of full size image
    public Uri uri;
    // Sample size used to display the whole image when fully zoomed out
    public int fullImageSampleSize;
    // Map of zoom level to tile grid
    public Map<Integer, List<Tile>> tileMap;
    // Overlay tile boundaries and other info
    public boolean debug;
    // Image orientation setting
    public int orientation = ViewValues.ORIENTATION_0;
    // Max scale allowed (prevent infinite zoom)
    public float maxScale = 2F;
    // Density to reach before loading higher resolution tiles
    public int minimumTileDpi = -1;
    // Pan limiting style
    public int panLimit = ViewValues.PAN_LIMIT_INSIDE;
    // Minimum scale type
    public int minimumScaleType = ViewValues.SCALE_TYPE_CENTER_INSIDE;
    public int maxTileWidth = TILE_SIZE_AUTO;
    public int maxTileHeight = TILE_SIZE_AUTO;
    // An executor service for loading of images
    public Executor executor = AsyncTask.THREAD_POOL_EXECUTOR;
    // Whether tiles should be loaded while gestures and animations are still in progress
    public boolean eagerLoadingEnabled = true;
    // Gesture detection settings
    public boolean panEnabled = true;
    public boolean zoomEnabled = true;
    public boolean quickScaleEnabled = true;
    // Double tap zoom behaviour
    public float doubleTapZoomScale = 1F;
    public int doubleTapZoomStyle = ViewValues.ZOOM_FOCUS_FIXED;
    public int doubleTapZoomDuration = 500;
    // Current scale and scale at start of zoom
    public float scale;
    public float scaleStart;
    // Screen coordinate of top-left corner of source image
    public PointF vTranslate;
    public PointF vTranslateStart;
    public PointF vTranslateBefore;
    // Source coordinate to center on, used when new position is set externally before view is ready
    public Float pendingScale;
    public PointF sPendingCenter;
    public PointF sRequestedCenter;
    // Source image dimensions and orientation - dimensions relate to the unrotated image
    public int sWidth;
    public int sHeight;
    public int sOrientation;
    // Min scale allowed (prevent infinite zoom)
    public float minScale = minScale();
    // Is two-finger zooming in progress
    public boolean isZooming;
    // Is one-finger panning in progress
    public boolean isPanning;
    // Is quick-scale gesture in progress
    public boolean isQuickScaling;
    // Max touches used in current gesture
    public int maxTouchCount;
    // Fling detector
    public GestureDetector detector;
    public GestureDetector singleDetector;
    // Tile and image decoding
    public ImageRegionDecoder decoder;
    public DecoderFactory<? extends ImageDecoder> bitmapDecoderFactory = new CompatDecoderFactory<ImageDecoder>(SkiaImageDecoder.class);
    public DecoderFactory<? extends ImageRegionDecoder> regionDecoderFactory = new CompatDecoderFactory<ImageRegionDecoder>(SkiaImageRegionDecoder.class);
    // Debug values
    public PointF vCenterStart;
    public float vDistStart;
    public float quickScaleLastDistance;
    public boolean quickScaleMoved;
    public PointF quickScaleVLastPoint;
    public PointF quickScaleSCenter;
    public PointF quickScaleVStart;
    // Scale and center animation tracking
    public Anim anim;
    // Whether a ready notification has been sent to subclasses
    public boolean readySent;
    // Whether a base layer loaded notification has been sent to subclasses
    public boolean imageLoadedSent;
    // Paint objects created once and reused for efficiency
    public Paint bitmapPaint;
    public Paint debugTextPaint;
    public Paint debugLinePaint;
    public Paint tileBgPaint;
    // Volatile fields used to reduce object creation
    public ScaleAndTranslate satTemp;
    public Matrix matrix;
    public RectF sRect;

    public SubsamplingScaleImageView(Context context, AttributeSet attr) {
        super(context, attr);
        density = getResources().getDisplayMetrics().density;
        SubsamplingScaleImageViewXKt.setMinimumDpi(this, 160);
        SetXKt.setDoubleTapZoomDpi(this, 160);
        SubsamplingScaleImageViewXKt.setMinimumTileDpi(this, 320);
        setGestureDetector(context);
        this.handler = new Handler(new Handler.Callback() {
            public boolean handleMessage(Message message) {
                if (message.what == MESSAGE_LONG_CLICK && onLongClickListener != null) {
                    maxTouchCount = 0;
                    SubsamplingScaleImageView.super.setOnLongClickListener(onLongClickListener);
                    performLongClick();
                    SubsamplingScaleImageView.super.setOnLongClickListener(null);
                }
                return true;
            }
        });
        quickScaleThreshold = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, context.getResources().getDisplayMetrics());
    }

    public SubsamplingScaleImageView(Context context) {
        this(context, null);
    }

    /**
     * Get the current preferred configuration for decoding bitmaps. {@link ImageDecoder} and {@link ImageRegionDecoder}
     * instances can read this and use it when decoding images.
     *
     * @return the preferred bitmap configuration, or null if none has been set.
     */
    public static Bitmap.Config getPreferredBitmapConfig() {
        return preferredBitmapConfig;
    }

    /**
     * Set a global preferred bitmap config shared by all view instances and applied to new instances
     * initialised after the call is made. This is a hint only; the bundled {@link ImageDecoder} and
     * {@link ImageRegionDecoder} classes all respect this (except when they were constructed with
     * an instance-specific config) but custom decoder classes will not.
     *
     * @param preferredBitmapConfig the bitmap configuration to be used by future instances of the view. Pass null to restore the default.
     */
    public static void setPreferredBitmapConfig(Bitmap.Config preferredBitmapConfig) {
        SubsamplingScaleImageView.preferredBitmapConfig = preferredBitmapConfig;
    }

    /**
     * Set the image source from a bitmap, resource, asset, file or other URI, providing a preview image to be
     * displayed until the full size image is loaded, starting with a given orientation setting, scale and center.
     * This is the best method to use when you want scale and center to be restored after screen orientation change;
     * it avoids any redundant loading of tiles in the wrong orientation.
     * <p>
     * You must declare the dimensions of the full size image by calling {@link ImageSource#dimensions(int, int)}
     * on the imageSource object. The preview source will be ignored if you don't provide dimensions,
     * and if you provide a bitmap for the full size image.
     *
     * @param imageSource   Image source. Dimensions must be declared.
     * @param previewSource Optional source for a preview image to be displayed and allow interaction while the full size image loads.
     * @param state         State to be restored. Nullable.
     */
    public final void setImage(@NonNull ImageSource imageSource, ImageSource previewSource, ImageViewState state) {
        //noinspection ConstantConditions
        if (imageSource == null) {
            throw new NullPointerException("imageSource must not be null");
        }

        reset(true);
        if (state != null) {
            restoreState(state);
        }

        if (previewSource != null) {
            if (imageSource.getBitmap() != null) {
                throw new IllegalArgumentException("Preview image cannot be used when a bitmap is provided for the main image");
            }
            if (imageSource.getSWidth() <= 0 || imageSource.getSHeight() <= 0) {
                throw new IllegalArgumentException("Preview image cannot be used unless dimensions are provided for the main image");
            }
            this.sWidth = imageSource.getSWidth();
            this.sHeight = imageSource.getSHeight();
            this.pRegion = previewSource.getSRegion();
            if (previewSource.getBitmap() != null) {
                this.bitmapIsCached = previewSource.isCached();
                onPreviewLoaded(previewSource.getBitmap());
            } else {
                Uri uri = previewSource.getUri();
                if (uri == null && previewSource.getResource() != ImageSource.DEFAULT_RESOURCE) {
                    uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getContext().getPackageName() + "/" + previewSource.getResource());
                }
                BitmapLoadTask task = new BitmapLoadTask(this, getContext(), bitmapDecoderFactory, uri, true);
                execute(task);
            }
        }

        if (imageSource.getBitmap() != null && imageSource.getSRegion() != null) {
            onImageLoaded(Bitmap.createBitmap(imageSource.getBitmap(), imageSource.getSRegion().left, imageSource.getSRegion().top, imageSource.getSRegion().width(), imageSource.getSRegion().height()), ViewValues.ORIENTATION_0, false);
        } else if (imageSource.getBitmap() != null) {
            onImageLoaded(imageSource.getBitmap(), ViewValues.ORIENTATION_0, imageSource.isCached());
        } else {
            sRegion = imageSource.getSRegion();
            uri = imageSource.getUri();
            if (uri == null && imageSource.getResource() != ImageSource.DEFAULT_RESOURCE) {
                uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getContext().getPackageName() + "/" + imageSource.getResource());
            }
            if (imageSource.getTile() || sRegion != null) {
                // Load the bitmap using tile decoding.
                TilesInitTask task = new TilesInitTask(this, getContext(), regionDecoderFactory, uri);
                execute(task);
            } else {
                // Load the bitmap as a single image.
                BitmapLoadTask task = new BitmapLoadTask(this, getContext(), bitmapDecoderFactory, uri, false);
                execute(task);
            }
        }
    }

    /**
     * Reset all state before setting/changing image or setting new rotation.
     */
    public void reset(boolean newImage) {
        debug("reset newImage=" + newImage);
        scale = 0f;
        scaleStart = 0f;
        vTranslate = null;
        vTranslateStart = null;
        vTranslateBefore = null;
        pendingScale = 0f;
        sPendingCenter = null;
        sRequestedCenter = null;
        isZooming = false;
        isPanning = false;
        isQuickScaling = false;
        maxTouchCount = 0;
        fullImageSampleSize = 0;
        vCenterStart = null;
        vDistStart = 0;
        quickScaleLastDistance = 0f;
        quickScaleMoved = false;
        quickScaleSCenter = null;
        quickScaleVLastPoint = null;
        quickScaleVStart = null;
        anim = null;
        satTemp = null;
        matrix = null;
        sRect = null;
        if (newImage) {
            uri = null;
            decoderLock.writeLock().lock();
            try {
                if (decoder != null) {
                    decoder.recycle();
                    decoder = null;
                }
            } finally {
                decoderLock.writeLock().unlock();
            }
            if (bitmap != null && !bitmapIsCached) {
                bitmap.recycle();
            }
            if (bitmap != null && bitmapIsCached && onImageEventListener != null) {
                onImageEventListener.onPreviewReleased();
            }
            sWidth = 0;
            sHeight = 0;
            sOrientation = 0;
            sRegion = null;
            pRegion = null;
            readySent = false;
            imageLoadedSent = false;
            bitmap = null;
            bitmapIsPreview = false;
            bitmapIsCached = false;
        }
        if (tileMap != null) {
            for (Map.Entry<Integer, List<Tile>> tileMapEntry : tileMap.entrySet()) {
                for (Tile tile : tileMapEntry.getValue()) {
                    tile.setVisible(false);
                    if (tile.getBitmap() != null) {
                        tile.getBitmap().recycle();
                        tile.setBitmap(null);
                    }
                }
            }
            tileMap = null;
        }
        setGestureDetector(getContext());
    }

    private void setGestureDetector(final Context context) {
        this.detector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (panEnabled && readySent && vTranslate != null && e1 != null && e2 != null && (Math.abs(e1.getX() - e2.getX()) > 50 || Math.abs(e1.getY() - e2.getY()) > 50) && (Math.abs(velocityX) > 500 || Math.abs(velocityY) > 500) && !isZooming) {
                    PointF vTranslateEnd = new PointF(vTranslate.x + (velocityX * 0.25f), vTranslate.y + (velocityY * 0.25f));
                    float sCenterXEnd = ((getWidth() / 2) - vTranslateEnd.x) / scale;
                    float sCenterYEnd = ((getHeight() / 2) - vTranslateEnd.y) / scale;
                    new AnimationBuilder(SubsamplingScaleImageView.this, new PointF(sCenterXEnd, sCenterYEnd)).withEasing(ViewValues.EASE_OUT_QUAD).withPanLimited(false).withOrigin(ViewValues.ORIGIN_FLING).start();
                    return true;
                }
                return super.onFling(e1, e2, velocityX, velocityY);
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                performClick();
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (zoomEnabled && readySent && vTranslate != null) {
                    // Hacky solution for #15 - after a double tap the GestureDetector gets in a state
                    // where the next fling is ignored, so here we replace it with a new one.
                    setGestureDetector(context);
                    if (quickScaleEnabled) {
                        // Store quick scale params. This will become either a double tap zoom or a
                        // quick scale depending on whether the user swipes.
                        vCenterStart = new PointF(e.getX(), e.getY());
                        vTranslateStart = new PointF(vTranslate.x, vTranslate.y);
                        scaleStart = scale;
                        isQuickScaling = true;
                        isZooming = true;
                        quickScaleLastDistance = -1F;
                        quickScaleSCenter = SubsamplingScaleImageViewXKt.viewToSourceCoord(SubsamplingScaleImageView.this, vCenterStart);
                        quickScaleVStart = new PointF(e.getX(), e.getY());
                        quickScaleVLastPoint = new PointF(quickScaleSCenter.x, quickScaleSCenter.y);
                        quickScaleMoved = false;
                        // We need to get events in onTouchEvent after this.
                        return false;
                    } else {
                        // Start double tap zoom animation.
                        doubleTapZoom(SubsamplingScaleImageViewXKt.viewToSourceCoord(SubsamplingScaleImageView.this, new PointF(e.getX(), e.getY())), new PointF(e.getX(), e.getY()));
                        return true;
                    }
                }
                return super.onDoubleTapEvent(e);
            }
        });

        singleDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                performClick();
                return true;
            }
        });
    }

    /**
     * On resize, preserve center and scale. Various behaviours are possible, override this method to use another.
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        debug("onSizeChanged %dx%d -> %dx%d", oldw, oldh, w, h);
        PointF sCenter = SubsamplingScaleImageViewXKt.getCenter(this);
        if (readySent && sCenter != null) {
            this.anim = null;
            this.pendingScale = scale;
            this.sPendingCenter = sCenter;
        }
    }

    /**
     * Measures the width and height of the view, preserving the aspect ratio of the image displayed if wrap_content is
     * used. The image will scale within this box, not resizing the view as it is zoomed.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        boolean resizeWidth = widthSpecMode != MeasureSpec.EXACTLY;
        boolean resizeHeight = heightSpecMode != MeasureSpec.EXACTLY;
        int width = parentWidth;
        int height = parentHeight;
        if (sWidth > 0 && sHeight > 0) {
            if (resizeWidth && resizeHeight) {
                width = sWidth();
                height = sHeight();
            } else if (resizeHeight) {
                height = (int) ((((double) sHeight() / (double) sWidth()) * width));
            } else if (resizeWidth) {
                width = (int) ((((double) sWidth() / (double) sHeight()) * height));
            }
        }
        width = Math.max(width, getSuggestedMinimumWidth());
        height = Math.max(height, getSuggestedMinimumHeight());
        setMeasuredDimension(width, height);
    }

    /**
     * Handle touch events. One finger pans, and two finger pinch and zoom plus panning.
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        // During non-interruptible anims, ignore all touch events
        if (anim != null && !anim.getInterruptible()) {
            requestDisallowInterceptTouchEvent(true);
            return true;
        } else {
            if (anim != null && anim.getListener() != null) {
                try {
                    anim.getListener().onInterruptedByUser();
                } catch (Exception e) {
                    Log.w(TAG, "Error thrown by animation listener", e);
                }
            }
            anim = null;
        }

        // Abort if not ready
        if (vTranslate == null) {
            if (singleDetector != null) {
                singleDetector.onTouchEvent(event);
            }
            return true;
        }
        // Detect flings, taps and double taps
        if (!isQuickScaling && (detector == null || detector.onTouchEvent(event))) {
            isZooming = false;
            isPanning = false;
            maxTouchCount = 0;
            return true;
        }

        if (vTranslateStart == null) {
            vTranslateStart = new PointF(0, 0);
        }
        if (vTranslateBefore == null) {
            vTranslateBefore = new PointF(0, 0);
        }
        if (vCenterStart == null) {
            vCenterStart = new PointF(0, 0);
        }

        // Store current values so we can send an event if they change
        float scaleBefore = scale;
        vTranslateBefore.set(vTranslate);

        boolean handled = onTouchEventInternal(event);
        sendStateChanged(scaleBefore, vTranslateBefore, ViewValues.ORIGIN_TOUCH);
        return handled || super.onTouchEvent(event);
    }

    @SuppressWarnings("deprecation")
    private boolean onTouchEventInternal(@NonNull MotionEvent event) {
        int touchCount = event.getPointerCount();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_1_DOWN:
            case MotionEvent.ACTION_POINTER_2_DOWN:
                anim = null;
                requestDisallowInterceptTouchEvent(true);
                maxTouchCount = Math.max(maxTouchCount, touchCount);
                if (touchCount >= 2) {
                    if (zoomEnabled) {
                        // Start pinch to zoom. Calculate distance between touch points and center point of the pinch.
                        float distance = distance(event.getX(0), event.getX(1), event.getY(0), event.getY(1));
                        scaleStart = scale;
                        vDistStart = distance;
                        vTranslateStart.set(vTranslate.x, vTranslate.y);
                        vCenterStart.set((event.getX(0) + event.getX(1)) / 2, (event.getY(0) + event.getY(1)) / 2);
                    } else {
                        // Abort all gestures on second touch
                        maxTouchCount = 0;
                    }
                    // Cancel long click timer
                    handler.removeMessages(MESSAGE_LONG_CLICK);
                } else if (!isQuickScaling) {
                    // Start one-finger pan
                    vTranslateStart.set(vTranslate.x, vTranslate.y);
                    vCenterStart.set(event.getX(), event.getY());

                    // Start long click timer
                    handler.sendEmptyMessageDelayed(MESSAGE_LONG_CLICK, 600);
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                boolean consumed = false;
                if (maxTouchCount > 0) {
                    if (touchCount >= 2) {
                        // Calculate new distance between touch points, to scale and pan relative to start values.
                        float vDistEnd = distance(event.getX(0), event.getX(1), event.getY(0), event.getY(1));
                        float vCenterEndX = (event.getX(0) + event.getX(1)) / 2;
                        float vCenterEndY = (event.getY(0) + event.getY(1)) / 2;

                        if (zoomEnabled && (distance(vCenterStart.x, vCenterEndX, vCenterStart.y, vCenterEndY) > 5 || Math.abs(vDistEnd - vDistStart) > 5 || isPanning)) {
                            isZooming = true;
                            isPanning = true;
                            consumed = true;

                            double previousScale = scale;
                            scale = Math.min(maxScale, (vDistEnd / vDistStart) * scaleStart);

                            if (scale <= minScale()) {
                                // Minimum scale reached so don't pan. Adjust start settings so any expand will zoom in.
                                vDistStart = vDistEnd;
                                scaleStart = minScale();
                                vCenterStart.set(vCenterEndX, vCenterEndY);
                                vTranslateStart.set(vTranslate);
                            } else if (panEnabled) {
                                // Translate to place the source image coordinate that was at the center of the pinch at the start
                                // at the center of the pinch now, to give simultaneous pan + zoom.
                                float vLeftStart = vCenterStart.x - vTranslateStart.x;
                                float vTopStart = vCenterStart.y - vTranslateStart.y;
                                float vLeftNow = vLeftStart * (scale / scaleStart);
                                float vTopNow = vTopStart * (scale / scaleStart);
                                vTranslate.x = vCenterEndX - vLeftNow;
                                vTranslate.y = vCenterEndY - vTopNow;
                                if ((previousScale * sHeight() < getHeight() && scale * sHeight() >= getHeight()) || (previousScale * sWidth() < getWidth() && scale * sWidth() >= getWidth())) {
                                    fitToBounds(true);
                                    vCenterStart.set(vCenterEndX, vCenterEndY);
                                    vTranslateStart.set(vTranslate);
                                    scaleStart = scale;
                                    vDistStart = vDistEnd;
                                }
                            } else if (sRequestedCenter != null) {
                                // With a center specified from code, zoom around that point.
                                vTranslate.x = (getWidth() / 2) - (scale * sRequestedCenter.x);
                                vTranslate.y = (getHeight() / 2) - (scale * sRequestedCenter.y);
                            } else {
                                // With no requested center, scale around the image center.
                                vTranslate.x = (getWidth() / 2) - (scale * (sWidth() / 2));
                                vTranslate.y = (getHeight() / 2) - (scale * (sHeight() / 2));
                            }

                            fitToBounds(true);
                            refreshRequiredTiles(eagerLoadingEnabled);
                        }
                    } else if (isQuickScaling) {
                        // One finger zoom
                        // Stole Google's Magical Formula™ to make sure it feels the exact same
                        float dist = Math.abs(quickScaleVStart.y - event.getY()) * 2 + quickScaleThreshold;

                        if (quickScaleLastDistance == -1f) {
                            quickScaleLastDistance = dist;
                        }
                        boolean isUpwards = event.getY() > quickScaleVLastPoint.y;
                        quickScaleVLastPoint.set(0, event.getY());

                        float spanDiff = Math.abs(1 - (dist / quickScaleLastDistance)) * 0.5f;

                        if (spanDiff > 0.03f || quickScaleMoved) {
                            quickScaleMoved = true;

                            float multiplier = 1;
                            if (quickScaleLastDistance > 0) {
                                multiplier = isUpwards ? (1 + spanDiff) : (1 - spanDiff);
                            }

                            double previousScale = scale;
                            scale = Math.max(minScale(), Math.min(maxScale, scale * multiplier));

                            if (panEnabled) {
                                float vLeftStart = vCenterStart.x - vTranslateStart.x;
                                float vTopStart = vCenterStart.y - vTranslateStart.y;
                                float vLeftNow = vLeftStart * (scale / scaleStart);
                                float vTopNow = vTopStart * (scale / scaleStart);
                                vTranslate.x = vCenterStart.x - vLeftNow;
                                vTranslate.y = vCenterStart.y - vTopNow;
                                if ((previousScale * sHeight() < getHeight() && scale * sHeight() >= getHeight()) || (previousScale * sWidth() < getWidth() && scale * sWidth() >= getWidth())) {
                                    fitToBounds(true);
                                    vCenterStart.set(SubsamplingScaleImageViewXKt.sourceToViewCoord(this, quickScaleSCenter));
                                    vTranslateStart.set(vTranslate);
                                    scaleStart = scale;
                                    dist = 0;
                                }
                            } else if (sRequestedCenter != null) {
                                // With a center specified from code, zoom around that point.
                                vTranslate.x = (getWidth() / 2) - (scale * sRequestedCenter.x);
                                vTranslate.y = (getHeight() / 2) - (scale * sRequestedCenter.y);
                            } else {
                                // With no requested center, scale around the image center.
                                vTranslate.x = (getWidth() / 2) - (scale * (sWidth() / 2));
                                vTranslate.y = (getHeight() / 2) - (scale * (sHeight() / 2));
                            }
                        }

                        quickScaleLastDistance = dist;

                        fitToBounds(true);
                        refreshRequiredTiles(eagerLoadingEnabled);

                        consumed = true;
                    } else if (!isZooming) {
                        // One finger pan - translate the image. We do this calculation even with pan disabled so click
                        // and long click behaviour is preserved.
                        float dx = Math.abs(event.getX() - vCenterStart.x);
                        float dy = Math.abs(event.getY() - vCenterStart.y);

                        //On the Samsung S6 long click event does not work, because the dx > 5 usually true
                        float offset = density * 5;
                        if (dx > offset || dy > offset || isPanning) {
                            consumed = true;
                            vTranslate.x = vTranslateStart.x + (event.getX() - vCenterStart.x);
                            vTranslate.y = vTranslateStart.y + (event.getY() - vCenterStart.y);

                            float lastX = vTranslate.x;
                            float lastY = vTranslate.y;
                            fitToBounds(true);
                            boolean atXEdge = lastX != vTranslate.x;
                            boolean atYEdge = lastY != vTranslate.y;
                            boolean edgeXSwipe = atXEdge && dx > dy && !isPanning;
                            boolean edgeYSwipe = atYEdge && dy > dx && !isPanning;
                            boolean yPan = lastY == vTranslate.y && dy > offset * 3;
                            if (!edgeXSwipe && !edgeYSwipe && (!atXEdge || !atYEdge || yPan || isPanning)) {
                                isPanning = true;
                            } else if (dx > offset || dy > offset) {
                                // Haven't panned the image, and we're at the left or right edge. Switch to page swipe.
                                maxTouchCount = 0;
                                handler.removeMessages(MESSAGE_LONG_CLICK);
                                requestDisallowInterceptTouchEvent(false);
                            }
                            if (!panEnabled) {
                                vTranslate.x = vTranslateStart.x;
                                vTranslate.y = vTranslateStart.y;
                                requestDisallowInterceptTouchEvent(false);
                            }

                            refreshRequiredTiles(eagerLoadingEnabled);
                        }
                    }
                }
                if (consumed) {
                    handler.removeMessages(MESSAGE_LONG_CLICK);
                    invalidate();
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_POINTER_2_UP:
                handler.removeMessages(MESSAGE_LONG_CLICK);
                if (isQuickScaling) {
                    isQuickScaling = false;
                    if (!quickScaleMoved) {
                        doubleTapZoom(quickScaleSCenter, vCenterStart);
                    }
                }
                if (maxTouchCount > 0 && (isZooming || isPanning)) {
                    if (isZooming && touchCount == 2) {
                        // Convert from zoom to pan with remaining touch
                        isPanning = true;
                        vTranslateStart.set(vTranslate.x, vTranslate.y);
                        if (event.getActionIndex() == 1) {
                            vCenterStart.set(event.getX(0), event.getY(0));
                        } else {
                            vCenterStart.set(event.getX(1), event.getY(1));
                        }
                    }
                    if (touchCount < 3) {
                        // End zooming when only one touch point
                        isZooming = false;
                    }
                    if (touchCount < 2) {
                        // End panning when no touch points
                        isPanning = false;
                        maxTouchCount = 0;
                    }
                    // Trigger load of tiles now required
                    refreshRequiredTiles(true);
                    return true;
                }
                if (touchCount == 1) {
                    isZooming = false;
                    isPanning = false;
                    maxTouchCount = 0;
                }
                return true;
        }
        return false;
    }

    private void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(disallowIntercept);
        }
    }

    /**
     * Double tap zoom handler triggered from gesture detector or on touch, depending on whether
     * quick scale is enabled.
     */
    private void doubleTapZoom(PointF sCenter, PointF vFocus) {
        if (!panEnabled) {
            if (sRequestedCenter != null) {
                // With a center specified from code, zoom around that point.
                sCenter.x = sRequestedCenter.x;
                sCenter.y = sRequestedCenter.y;
            } else {
                // With no requested center, scale around the image center.
                sCenter.x = sWidth() / 2;
                sCenter.y = sHeight() / 2;
            }
        }
        float doubleTapZoomScale = Math.min(maxScale, SubsamplingScaleImageView.this.doubleTapZoomScale);
        boolean zoomIn = (scale <= doubleTapZoomScale * 0.9) || scale == minScale;
        float targetScale = zoomIn ? doubleTapZoomScale : minScale();
        if (doubleTapZoomStyle == ViewValues.ZOOM_FOCUS_CENTER_IMMEDIATE) {
            SubsamplingScaleImageViewXKt.setScaleAndCenter(this, targetScale, sCenter);
        } else if (doubleTapZoomStyle == ViewValues.ZOOM_FOCUS_CENTER || !zoomIn || !panEnabled) {
            new AnimationBuilder(this, targetScale, sCenter).withInterruptible(false).withDuration(doubleTapZoomDuration).withOrigin(ViewValues.ORIGIN_DOUBLE_TAP_ZOOM).start();
        } else if (doubleTapZoomStyle == ViewValues.ZOOM_FOCUS_FIXED) {
            new AnimationBuilder(this, targetScale, sCenter, vFocus).withInterruptible(false).withDuration(doubleTapZoomDuration).withOrigin(ViewValues.ORIGIN_DOUBLE_TAP_ZOOM).start();
        }
        invalidate();
    }

    /**
     * Draw method should not be called until the view has dimensions so the first calls are used as triggers to calculate
     * the scaling and tiling required. Once the view is setup, tiles are displayed as they are loaded.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        createPaints();

        // If image or view dimensions are not known yet, abort.
        if (sWidth == 0 || sHeight == 0 || getWidth() == 0 || getHeight() == 0) {
            return;
        }

        // When using tiles, on first render with no tile map ready, initialise it and kick off async base image loading.
        if (tileMap == null && decoder != null) {
            initialiseBaseLayer(getMaxBitmapDimensions(canvas));
        }

        // If image has been loaded or supplied as a bitmap, onDraw may be the first time the view has
        // dimensions and therefore the first opportunity to set scale and translate. If this call returns
        // false there is nothing to be drawn so return immediately.
        if (!checkReady()) {
            return;
        }

        // Set scale and translate before draw.
        preDraw();

        // If animating scale, calculate current scale and center with easing equations
        if (anim != null && anim.getVFocusStart() != null) {
            // Store current values so we can send an event if they change
            float scaleBefore = scale;
            if (vTranslateBefore == null) {
                vTranslateBefore = new PointF(0, 0);
            }
            vTranslateBefore.set(vTranslate);

            long scaleElapsed = System.currentTimeMillis() - anim.getTime();
            boolean finished = scaleElapsed > anim.getDuration();
            scaleElapsed = Math.min(scaleElapsed, anim.getDuration());
            scale = ease(anim.getEasing(), scaleElapsed, anim.getScaleStart(), anim.getScaleEnd() - anim.getScaleStart(), anim.getDuration());

            // Apply required animation to the focal point
            float vFocusNowX = ease(anim.getEasing(), scaleElapsed, anim.getVFocusStart().x, anim.getVFocusEnd().x - anim.getVFocusStart().x, anim.getDuration());
            float vFocusNowY = ease(anim.getEasing(), scaleElapsed, anim.getVFocusStart().y, anim.getVFocusEnd().y - anim.getVFocusStart().y, anim.getDuration());
            // Find out where the focal point is at this scale and adjust its position to follow the animation path
            vTranslate.x -= sourceToViewX(anim.getSCenterEnd().x) - vFocusNowX;
            vTranslate.y -= sourceToViewY(anim.getSCenterEnd().y) - vFocusNowY;

            // For translate anims, showing the image non-centered is never allowed, for scaling anims it is during the animation.
            fitToBounds(finished || (anim.getScaleStart() == anim.getScaleEnd()));
            sendStateChanged(scaleBefore, vTranslateBefore, anim.getOrigin());
            refreshRequiredTiles(finished);
            if (finished) {
                if (anim.getListener() != null) {
                    try {
                        anim.getListener().onComplete();
                    } catch (Exception e) {
                        Log.w(TAG, "Error thrown by animation listener", e);
                    }
                }
                anim = null;
            }
            invalidate();
        }

        if (tileMap != null && isBaseLayerReady()) {

            // Optimum sample size for current scale
            int sampleSize = Math.min(fullImageSampleSize, calculateInSampleSize(scale));

            // First check for missing tiles - if there are any we need the base layer underneath to avoid gaps
            boolean hasMissingTiles = false;
            for (Map.Entry<Integer, List<Tile>> tileMapEntry : tileMap.entrySet()) {
                if (tileMapEntry.getKey() == sampleSize) {
                    for (Tile tile : tileMapEntry.getValue()) {
                        if (tile.getVisible() && (tile.getLoading() || tile.getBitmap() == null)) {
                            hasMissingTiles = true;
                        }
                    }
                }
            }

            // Render all loaded tiles. LinkedHashMap used for bottom up rendering - lower res tiles underneath.
            for (Map.Entry<Integer, List<Tile>> tileMapEntry : tileMap.entrySet()) {
                if (tileMapEntry.getKey() == sampleSize || hasMissingTiles) {
                    for (Tile tile : tileMapEntry.getValue()) {
                        sourceToViewRect(tile.getSRect(), tile.getVRect());
                        if (!tile.getLoading() && tile.getBitmap() != null) {
                            if (tileBgPaint != null) {
                                canvas.drawRect(tile.getVRect(), tileBgPaint);
                            }
                            if (matrix == null) {
                                matrix = new Matrix();
                            }
                            matrix.reset();
                            setMatrixArray(srcArray, 0, 0, tile.getBitmap().getWidth(), 0, tile.getBitmap().getWidth(), tile.getBitmap().getHeight(), 0, tile.getBitmap().getHeight());
                            if (getRequiredRotation() == ViewValues.ORIENTATION_0) {
                                setMatrixArray(dstArray, tile.getVRect().left, tile.getVRect().top, tile.getVRect().right, tile.getVRect().top, tile.getVRect().right, tile.getVRect().bottom, tile.getVRect().left, tile.getVRect().bottom);
                            } else if (getRequiredRotation() == ViewValues.ORIENTATION_90) {
                                setMatrixArray(dstArray, tile.getVRect().right, tile.getVRect().top, tile.getVRect().right, tile.getVRect().bottom, tile.getVRect().left, tile.getVRect().bottom, tile.getVRect().left, tile.getVRect().top);
                            } else if (getRequiredRotation() == ViewValues.ORIENTATION_180) {
                                setMatrixArray(dstArray, tile.getVRect().right, tile.getVRect().bottom, tile.getVRect().left, tile.getVRect().bottom, tile.getVRect().left, tile.getVRect().top, tile.getVRect().right, tile.getVRect().top);
                            } else if (getRequiredRotation() == ViewValues.ORIENTATION_270) {
                                setMatrixArray(dstArray, tile.getVRect().left, tile.getVRect().bottom, tile.getVRect().left, tile.getVRect().top, tile.getVRect().right, tile.getVRect().top, tile.getVRect().right, tile.getVRect().bottom);
                            }
                            matrix.setPolyToPoly(srcArray, 0, dstArray, 0, 4);
                            canvas.drawBitmap(tile.getBitmap(), matrix, bitmapPaint);
                            if (debug) {
                                canvas.drawRect(tile.getVRect(), debugLinePaint);
                            }
                        } else if (tile.getLoading() && debug) {
                            canvas.drawText("LOADING", tile.getVRect().left + px(5), tile.getVRect().top + px(35), debugTextPaint);
                        }
                        if (tile.getVisible() && debug) {
                            canvas.drawText("ISS " + tile.getSampleSize() + " RECT " + tile.getSRect().top + "," + tile.getSRect().left + "," + tile.getSRect().bottom + "," + tile.getSRect().right, tile.getVRect().left + px(5), tile.getVRect().top + px(15), debugTextPaint);
                        }
                    }
                }
            }

        } else if (bitmap != null) {

            float xScale = scale, yScale = scale;
            if (bitmapIsPreview) {
                xScale = scale * ((float) sWidth / bitmap.getWidth());
                yScale = scale * ((float) sHeight / bitmap.getHeight());
            }

            if (matrix == null) {
                matrix = new Matrix();
            }
            matrix.reset();
            matrix.postScale(xScale, yScale);
            matrix.postRotate(getRequiredRotation());
            matrix.postTranslate(vTranslate.x, vTranslate.y);

            if (getRequiredRotation() == ViewValues.ORIENTATION_180) {
                matrix.postTranslate(scale * sWidth, scale * sHeight);
            } else if (getRequiredRotation() == ViewValues.ORIENTATION_90) {
                matrix.postTranslate(scale * sHeight, 0);
            } else if (getRequiredRotation() == ViewValues.ORIENTATION_270) {
                matrix.postTranslate(0, scale * sWidth);
            }

            if (tileBgPaint != null) {
                if (sRect == null) {
                    sRect = new RectF();
                }
                sRect.set(0f, 0f, bitmapIsPreview ? bitmap.getWidth() : sWidth, bitmapIsPreview ? bitmap.getHeight() : sHeight);
                matrix.mapRect(sRect);
                canvas.drawRect(sRect, tileBgPaint);
            }
            canvas.drawBitmap(bitmap, matrix, bitmapPaint);

        }

        if (debug) {
            canvas.drawText("Scale: " + String.format(Locale.ENGLISH, "%.2f", scale) + " (" + String.format(Locale.ENGLISH, "%.2f", minScale()) + " - " + String.format(Locale.ENGLISH, "%.2f", maxScale) + ")", px(5), px(15), debugTextPaint);
            canvas.drawText("Translate: " + String.format(Locale.ENGLISH, "%.2f", vTranslate.x) + ":" + String.format(Locale.ENGLISH, "%.2f", vTranslate.y), px(5), px(30), debugTextPaint);
            PointF center = SubsamplingScaleImageViewXKt.getCenter(this);
            //noinspection ConstantConditions
            canvas.drawText("Source center: " + String.format(Locale.ENGLISH, "%.2f", center.x) + ":" + String.format(Locale.ENGLISH, "%.2f", center.y), px(5), px(45), debugTextPaint);
            if (anim != null) {
                PointF vCenterStart = SubsamplingScaleImageViewXKt.sourceToViewCoord(this, anim.getSCenterStart());
                PointF vCenterEndRequested = SubsamplingScaleImageViewXKt.sourceToViewCoord(this, anim.getSCenterEndRequested());
                PointF vCenterEnd = SubsamplingScaleImageViewXKt.sourceToViewCoord(this, anim.getSCenterEnd());
                //noinspection ConstantConditions
                canvas.drawCircle(vCenterStart.x, vCenterStart.y, px(10), debugLinePaint);
                debugLinePaint.setColor(Color.RED);
                //noinspection ConstantConditions
                canvas.drawCircle(vCenterEndRequested.x, vCenterEndRequested.y, px(20), debugLinePaint);
                debugLinePaint.setColor(Color.BLUE);
                //noinspection ConstantConditions
                canvas.drawCircle(vCenterEnd.x, vCenterEnd.y, px(25), debugLinePaint);
                debugLinePaint.setColor(Color.CYAN);
                canvas.drawCircle(getWidth() / 2, getHeight() / 2, px(30), debugLinePaint);
            }
            if (vCenterStart != null) {
                debugLinePaint.setColor(Color.RED);
                canvas.drawCircle(vCenterStart.x, vCenterStart.y, px(20), debugLinePaint);
            }
            if (quickScaleSCenter != null) {
                debugLinePaint.setColor(Color.BLUE);
                canvas.drawCircle(sourceToViewX(quickScaleSCenter.x), sourceToViewY(quickScaleSCenter.y), px(35), debugLinePaint);
            }
            if (quickScaleVStart != null && isQuickScaling) {
                debugLinePaint.setColor(Color.CYAN);
                canvas.drawCircle(quickScaleVStart.x, quickScaleVStart.y, px(30), debugLinePaint);
            }
            debugLinePaint.setColor(Color.MAGENTA);
        }
    }

    /**
     * Helper method for setting the values of a tile matrix array.
     */
    private void setMatrixArray(float[] array, float f0, float f1, float f2, float f3, float f4, float f5, float f6, float f7) {
        array[0] = f0;
        array[1] = f1;
        array[2] = f2;
        array[3] = f3;
        array[4] = f4;
        array[5] = f5;
        array[6] = f6;
        array[7] = f7;
    }

    /**
     * Checks whether the base layer of tiles or full size bitmap is ready.
     */
    public boolean isBaseLayerReady() {
        if (bitmap != null && !bitmapIsPreview) {
            return true;
        } else if (tileMap != null) {
            boolean baseLayerReady = true;
            for (Map.Entry<Integer, List<Tile>> tileMapEntry : tileMap.entrySet()) {
                if (tileMapEntry.getKey() == fullImageSampleSize) {
                    for (Tile tile : tileMapEntry.getValue()) {
                        if (tile.getLoading() || tile.getBitmap() == null) {
                            baseLayerReady = false;
                        }
                    }
                }
            }
            return baseLayerReady;
        }
        return false;
    }

    /**
     * Check whether view and image dimensions are known and either a preview, full size image or
     * base layer tiles are loaded. First time, send ready event to listener. The next draw will
     * display an image.
     */
    public boolean checkReady() {
        boolean ready = getWidth() > 0 && getHeight() > 0 && sWidth > 0 && sHeight > 0 && (bitmap != null || isBaseLayerReady());
        if (!readySent && ready) {
            preDraw();
            readySent = true;
            SubsamplingScaleImageViewXKt.onReady(this);
            if (onImageEventListener != null) {
                onImageEventListener.onReady();
            }
        }
        return ready;
    }

    /**
     * Check whether either the full size bitmap or base layer tiles are loaded. First time, send image
     * loaded event to listener.
     */
    public boolean checkImageLoaded() {
        boolean imageLoaded = isBaseLayerReady();
        if (!imageLoadedSent && imageLoaded) {
            preDraw();
            imageLoadedSent = true;
            SubsamplingScaleImageViewXKt.onImageLoaded(this);
            if (onImageEventListener != null) {
                onImageEventListener.onImageLoaded();
            }
        }
        return imageLoaded;
    }

    /**
     * Creates Paint objects once when first needed.
     */
    private void createPaints() {
        if (bitmapPaint == null) {
            bitmapPaint = new Paint();
            bitmapPaint.setAntiAlias(true);
            bitmapPaint.setFilterBitmap(true);
            bitmapPaint.setDither(true);
        }
        if ((debugTextPaint == null || debugLinePaint == null) && debug) {
            debugTextPaint = new Paint();
            debugTextPaint.setTextSize(px(12));
            debugTextPaint.setColor(Color.MAGENTA);
            debugTextPaint.setStyle(Style.FILL);
            debugLinePaint = new Paint();
            debugLinePaint.setColor(Color.MAGENTA);
            debugLinePaint.setStyle(Style.STROKE);
            debugLinePaint.setStrokeWidth(px(1));
        }
    }

    /**
     * Called on first draw when the view has dimensions. Calculates the initial sample size and starts async loading of
     * the base layer image - the whole source subsampled as necessary.
     */
    private synchronized void initialiseBaseLayer(@NonNull Point maxTileDimensions) {
        debug("initialiseBaseLayer maxTileDimensions=%dx%d", maxTileDimensions.x, maxTileDimensions.y);

        satTemp = new ScaleAndTranslate(0f, new PointF(0, 0));
        fitToBounds(true, satTemp);

        // Load double resolution - next level will be split into four tiles and at the center all four are required,
        // so don't bother with tiling until the next level 16 tiles are needed.
        fullImageSampleSize = calculateInSampleSize(satTemp.getScale());
        if (fullImageSampleSize > 1) {
            fullImageSampleSize /= 2;
        }

        if (fullImageSampleSize == 1 && sRegion == null && sWidth() < maxTileDimensions.x && sHeight() < maxTileDimensions.y) {

            // Whole image is required at native resolution, and is smaller than the canvas max bitmap size.
            // Use BitmapDecoder for better image support.
            decoder.recycle();
            decoder = null;
            BitmapLoadTask task = new BitmapLoadTask(this, getContext(), bitmapDecoderFactory, uri, false);
            execute(task);

        } else {

            initialiseTileMap(maxTileDimensions);

            List<Tile> baseGrid = tileMap.get(fullImageSampleSize);
            for (Tile baseTile : baseGrid) {
                TileLoadTask task = new TileLoadTask(this, decoder, baseTile);
                execute(task);
            }
            refreshRequiredTiles(true);

        }

    }

    /**
     * Loads the optimum tiles for display at the current scale and translate, so the screen can be filled with tiles
     * that are at least as high resolution as the screen. Frees up bitmaps that are now off the screen.
     *
     * @param load Whether to load the new tiles needed. Use false while scrolling/panning for performance.
     */
    public void refreshRequiredTiles(boolean load) {
        if (decoder == null || tileMap == null) {
            return;
        }

        int sampleSize = Math.min(fullImageSampleSize, calculateInSampleSize(scale));

        // Load tiles of the correct sample size that are on screen. Discard tiles off screen, and those that are higher
        // resolution than required, or lower res than required but not the base layer, so the base layer is always present.
        for (Map.Entry<Integer, List<Tile>> tileMapEntry : tileMap.entrySet()) {
            for (Tile tile : tileMapEntry.getValue()) {
                if (tile.getSampleSize() < sampleSize || (tile.getSampleSize() > sampleSize && tile.getSampleSize() != fullImageSampleSize)) {
                    tile.setVisible(false);
                    if (tile.getBitmap() != null) {
                        tile.getBitmap().recycle();
                        tile.setBitmap(null);
                    }
                }
                if (tile.getSampleSize() == sampleSize) {
                    if (tileVisible(tile)) {
                        tile.setVisible(true);
                        if (!tile.getLoading() && tile.getBitmap() == null && load) {
                            TileLoadTask task = new TileLoadTask(this, decoder, tile);
                            execute(task);
                        }
                    } else if (tile.getSampleSize() != fullImageSampleSize) {
                        tile.setVisible(false);
                        if (tile.getBitmap() != null) {
                            tile.getBitmap().recycle();
                            tile.setBitmap(null);
                        }
                    }
                } else if (tile.getSampleSize() == fullImageSampleSize) {
                    tile.setVisible(true);
                }
            }
        }

    }

    /**
     * Determine whether tile is visible.
     */
    private boolean tileVisible(Tile tile) {
        float sVisLeft = viewToSourceX(0),
                sVisRight = viewToSourceX(getWidth()),
                sVisTop = viewToSourceY(0),
                sVisBottom = viewToSourceY(getHeight());
        return !(sVisLeft > tile.getSRect().right || tile.getSRect().left > sVisRight || sVisTop > tile.getSRect().bottom || tile.getSRect().top > sVisBottom);
    }

    /**
     * Sets scale and translate ready for the next draw.
     */
    private void preDraw() {
        if (getWidth() == 0 || getHeight() == 0 || sWidth <= 0 || sHeight <= 0) {
            return;
        }

        // If waiting to translate to new center position, set translate now
        if (sPendingCenter != null && pendingScale != null) {
            scale = pendingScale;
            if (vTranslate == null) {
                vTranslate = new PointF();
            }
            vTranslate.x = (getWidth() / 2) - (scale * sPendingCenter.x);
            vTranslate.y = (getHeight() / 2) - (scale * sPendingCenter.y);
            sPendingCenter = null;
            pendingScale = null;
            fitToBounds(true);
            refreshRequiredTiles(true);
        }

        // On first display of base image set up position, and in other cases make sure scale is correct.
        fitToBounds(false);
    }

    /**
     * Calculates sample size to fit the source image in given bounds.
     */
    private int calculateInSampleSize(float scale) {
        if (minimumTileDpi > 0) {
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            float averageDpi = (metrics.xdpi + metrics.ydpi) / 2;
            scale = (minimumTileDpi / averageDpi) * scale;
        }

        int reqWidth = (int) (sWidth() * scale);
        int reqHeight = (int) (sHeight() * scale);

        // Raw height and width of image
        int inSampleSize = 1;
        if (reqWidth == 0 || reqHeight == 0) {
            return 32;
        }

        if (sHeight() > reqHeight || sWidth() > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) sHeight() / (float) reqHeight);
            final int widthRatio = Math.round((float) sWidth() / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        // We want the actual sample size that will be used, so round down to nearest power of 2.
        int power = 1;
        while (power * 2 < inSampleSize) {
            power = power * 2;
        }

        return power;
    }

    /**
     * Adjusts hypothetical future scale and translate values to keep scale within the allowed range and the image on screen. Minimum scale
     * is set so one dimension fills the view and the image is centered on the other dimension. Used to calculate what the target of an
     * animation should be.
     *
     * @param center Whether the image should be centered in the dimension it's too small to fill. While animating this can be false to avoid changes in direction as bounds are reached.
     * @param sat    The scale we want and the translation we're aiming for. The values are adjusted to be valid.
     */
    public void fitToBounds(boolean center, ScaleAndTranslate sat) {
        if (panLimit == ViewValues.PAN_LIMIT_OUTSIDE && SubsamplingScaleImageViewXKt.isReady(this)) {
            center = false;
        }

        PointF vTranslate = sat.getVTranslate();
        float scale = limitedScale(sat.getScale());
        float scaleWidth = scale * sWidth();
        float scaleHeight = scale * sHeight();

        if (panLimit == ViewValues.PAN_LIMIT_CENTER && SubsamplingScaleImageViewXKt.isReady(this)) {
            vTranslate.x = Math.max(vTranslate.x, getWidth() / 2 - scaleWidth);
            vTranslate.y = Math.max(vTranslate.y, getHeight() / 2 - scaleHeight);
        } else if (center) {
            vTranslate.x = Math.max(vTranslate.x, getWidth() - scaleWidth);
            vTranslate.y = Math.max(vTranslate.y, getHeight() - scaleHeight);
        } else {
            vTranslate.x = Math.max(vTranslate.x, -scaleWidth);
            vTranslate.y = Math.max(vTranslate.y, -scaleHeight);
        }

        // Asymmetric padding adjustments
        float xPaddingRatio = getPaddingLeft() > 0 || getPaddingRight() > 0 ? getPaddingLeft() / (float) (getPaddingLeft() + getPaddingRight()) : 0.5f;
        float yPaddingRatio = getPaddingTop() > 0 || getPaddingBottom() > 0 ? getPaddingTop() / (float) (getPaddingTop() + getPaddingBottom()) : 0.5f;

        float maxTx;
        float maxTy;
        if (panLimit == ViewValues.PAN_LIMIT_CENTER && SubsamplingScaleImageViewXKt.isReady(this)) {
            maxTx = Math.max(0, getWidth() / 2);
            maxTy = Math.max(0, getHeight() / 2);
        } else if (center) {
            maxTx = Math.max(0, (getWidth() - scaleWidth) * xPaddingRatio);
            maxTy = Math.max(0, (getHeight() - scaleHeight) * yPaddingRatio);
        } else {
            maxTx = Math.max(0, getWidth());
            maxTy = Math.max(0, getHeight());
        }

        vTranslate.x = Math.min(vTranslate.x, maxTx);
        vTranslate.y = Math.min(vTranslate.y, maxTy);

        sat.setScale(scale);
    }

    /**
     * Adjusts current scale and translate values to keep scale within the allowed range and the image on screen. Minimum scale
     * is set so one dimension fills the view and the image is centered on the other dimension.
     *
     * @param center Whether the image should be centered in the dimension it's too small to fill. While animating this can be false to avoid changes in direction as bounds are reached.
     */
    public void fitToBounds(boolean center) {
        boolean init = false;
        if (vTranslate == null) {
            init = true;
            vTranslate = new PointF(0, 0);
        }
        if (satTemp == null) {
            satTemp = new ScaleAndTranslate(0, new PointF(0, 0));
        }
        satTemp.setScale(scale);
        satTemp.getVTranslate().set(vTranslate);
        fitToBounds(center, satTemp);
        scale = satTemp.getScale();
        vTranslate.set(satTemp.getVTranslate());
        if (init && minimumScaleType != ViewValues.SCALE_TYPE_START) {
            vTranslate.set(vTranslateForSCenter(sWidth() / 2, sHeight() / 2, scale));
        }
    }

    /**
     * Once source image and view dimensions are known, creates a map of sample size to tile grid.
     */
    private void initialiseTileMap(Point maxTileDimensions) {
        debug("initialiseTileMap maxTileDimensions=%dx%d", maxTileDimensions.x, maxTileDimensions.y);
        this.tileMap = new LinkedHashMap<>();
        int sampleSize = fullImageSampleSize;
        int xTiles = 1;
        int yTiles = 1;
        while (true) {
            int sTileWidth = sWidth() / xTiles;
            int sTileHeight = sHeight() / yTiles;
            int subTileWidth = sTileWidth / sampleSize;
            int subTileHeight = sTileHeight / sampleSize;
            while (subTileWidth + xTiles + 1 > maxTileDimensions.x || (subTileWidth > getWidth() * 1.25 && sampleSize < fullImageSampleSize)) {
                xTiles += 1;
                sTileWidth = sWidth() / xTiles;
                subTileWidth = sTileWidth / sampleSize;
            }
            while (subTileHeight + yTiles + 1 > maxTileDimensions.y || (subTileHeight > getHeight() * 1.25 && sampleSize < fullImageSampleSize)) {
                yTiles += 1;
                sTileHeight = sHeight() / yTiles;
                subTileHeight = sTileHeight / sampleSize;
            }
            List<Tile> tileGrid = new ArrayList<>(xTiles * yTiles);
            for (int x = 0; x < xTiles; x++) {
                for (int y = 0; y < yTiles; y++) {
                    Tile tile = new Tile();
                    tile.setSampleSize(sampleSize);
                    tile.setVisible(sampleSize == fullImageSampleSize);
                    tile.setSRect(new Rect(
                            x * sTileWidth,
                            y * sTileHeight,
                            x == xTiles - 1 ? sWidth() : (x + 1) * sTileWidth,
                            y == yTiles - 1 ? sHeight() : (y + 1) * sTileHeight
                    ));
                    tile.setVRect(new Rect(0, 0, 0, 0));
                    tile.setFileSRect(new Rect(tile.getSRect()));
                    tileGrid.add(tile);
                }
            }
            tileMap.put(sampleSize, tileGrid);
            if (sampleSize == 1) {
                break;
            } else {
                sampleSize /= 2;
            }
        }
    }

    /**
     * Called by worker task when decoder is ready and image size and EXIF orientation is known.
     */
    public synchronized void onTilesInited(ImageRegionDecoder decoder, int sWidth, int sHeight, int sOrientation) {
        debug("onTilesInited sWidth=%d, sHeight=%d, sOrientation=%d", sWidth, sHeight, orientation);
        // If actual dimensions don't match the declared size, reset everything.
        if (this.sWidth > 0 && this.sHeight > 0 && (this.sWidth != sWidth || this.sHeight != sHeight)) {
            reset(false);
            if (bitmap != null) {
                if (!bitmapIsCached) {
                    bitmap.recycle();
                }
                bitmap = null;
                if (onImageEventListener != null && bitmapIsCached) {
                    onImageEventListener.onPreviewReleased();
                }
                bitmapIsPreview = false;
                bitmapIsCached = false;
            }
        }
        this.decoder = decoder;
        this.sWidth = sWidth;
        this.sHeight = sHeight;
        this.sOrientation = sOrientation;
        checkReady();
        if (!checkImageLoaded() && maxTileWidth > 0 && maxTileWidth != TILE_SIZE_AUTO && maxTileHeight > 0 && maxTileHeight != TILE_SIZE_AUTO && getWidth() > 0 && getHeight() > 0) {
            initialiseBaseLayer(new Point(maxTileWidth, maxTileHeight));
        }
        invalidate();
        requestLayout();
    }

    /**
     * Called by worker task when preview image is loaded.
     */
    public synchronized void onPreviewLoaded(Bitmap previewBitmap) {
        debug("onPreviewLoaded");
        if (bitmap != null || imageLoadedSent) {
            previewBitmap.recycle();
            return;
        }
        if (pRegion != null) {
            bitmap = Bitmap.createBitmap(previewBitmap, pRegion.left, pRegion.top, pRegion.width(), pRegion.height());
        } else {
            bitmap = previewBitmap;
        }
        bitmapIsPreview = true;
        if (checkReady()) {
            invalidate();
            requestLayout();
        }
    }

    /**
     * Called by worker task when full size image bitmap is ready (tiling is disabled).
     */
    public synchronized void onImageLoaded(Bitmap bitmap, int sOrientation, boolean bitmapIsCached) {
        debug("onImageLoaded");
        // If actual dimensions don't match the declared size, reset everything.
        if (this.sWidth > 0 && this.sHeight > 0 && (this.sWidth != bitmap.getWidth() || this.sHeight != bitmap.getHeight())) {
            reset(false);
        }
        if (this.bitmap != null && !this.bitmapIsCached) {
            this.bitmap.recycle();
        }

        if (this.bitmap != null && this.bitmapIsCached && onImageEventListener != null) {
            onImageEventListener.onPreviewReleased();
        }

        this.bitmapIsPreview = false;
        this.bitmapIsCached = bitmapIsCached;
        this.bitmap = bitmap;
        this.sWidth = bitmap.getWidth();
        this.sHeight = bitmap.getHeight();
        this.sOrientation = sOrientation;
        boolean ready = checkReady();
        boolean imageLoaded = checkImageLoaded();
        if (ready || imageLoaded) {
            invalidate();
            requestLayout();
        }
    }

    private void execute(AsyncTask<Void, Void, ?> asyncTask) {
        asyncTask.executeOnExecutor(executor);
    }

    /**
     * Set scale, center and orientation from saved state.
     */
    private void restoreState(ImageViewState state) {
        if (state != null && ViewValues.INSTANCE.getVALID_ORIENTATIONS().contains(state.getOrientation())) {
            this.orientation = state.getOrientation();
            this.pendingScale = state.getScale();
            this.sPendingCenter = state.getCenter();
            invalidate();
        }
    }

    /**
     * Use canvas max bitmap width and height instead of the default 2048, to avoid redundant tiling.
     */
    @NonNull
    private Point getMaxBitmapDimensions(Canvas canvas) {
        return new Point(Math.min(canvas.getMaximumBitmapWidth(), maxTileWidth), Math.min(canvas.getMaximumBitmapHeight(), maxTileHeight));
    }

    /**
     * Get source width taking rotation into account.
     */
    @SuppressWarnings("SuspiciousNameCombination")
    public int sWidth() {
        int rotation = getRequiredRotation();
        if (rotation == 90 || rotation == 270) {
            return sHeight;
        } else {
            return sWidth;
        }
    }

    /**
     * Get source height taking rotation into account.
     */
    @SuppressWarnings("SuspiciousNameCombination")
    public int sHeight() {
        int rotation = getRequiredRotation();
        if (rotation == 90 || rotation == 270) {
            return sWidth;
        } else {
            return sHeight;
        }
    }

    /**
     * Converts source rectangle from tile, which treats the image file as if it were in the correct orientation already,
     * to the rectangle of the image that needs to be loaded.
     */
    @SuppressWarnings("SuspiciousNameCombination")
    @AnyThread
    public void fileSRect(Rect sRect, Rect target) {
        if (getRequiredRotation() == 0) {
            target.set(sRect);
        } else if (getRequiredRotation() == 90) {
            target.set(sRect.top, sHeight - sRect.right, sRect.bottom, sHeight - sRect.left);
        } else if (getRequiredRotation() == 180) {
            target.set(sWidth - sRect.right, sHeight - sRect.bottom, sWidth - sRect.left, sHeight - sRect.top);
        } else {
            target.set(sWidth - sRect.bottom, sRect.left, sWidth - sRect.top, sRect.right);
        }
    }

    /**
     * Determines the rotation to be applied to tiles, based on EXIF orientation or chosen setting.
     */
    @AnyThread
    public int getRequiredRotation() {
        if (orientation == ViewValues.ORIENTATION_USE_EXIF) {
            return sOrientation;
        } else {
            return orientation;
        }
    }

    /**
     * Pythagoras distance between two points.
     */
    private float distance(float x0, float x1, float y0, float y1) {
        float x = x0 - x1;
        float y = y0 - y1;
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * Releases all resources the view is using and resets the state, nulling any fields that use significant memory.
     * After you have called this method, the view can be re-used by setting a new image. Settings are remembered
     * but state (scale and center) is forgotten. You can restore these yourself if required.
     */
    public void recycle() {
        reset(true);
        bitmapPaint = null;
        debugTextPaint = null;
        debugLinePaint = null;
        tileBgPaint = null;
    }

    /**
     * Convert screen to source x coordinate.
     */
    public float viewToSourceX(float vx) {
        if (vTranslate == null) {
            return Float.NaN;
        }
        return (vx - vTranslate.x) / scale;
    }

    /**
     * Convert screen to source y coordinate.
     */
    public float viewToSourceY(float vy) {
        if (vTranslate == null) {
            return Float.NaN;
        }
        return (vy - vTranslate.y) / scale;
    }

    /**
     * Converts a rectangle within the view to the corresponding rectangle from the source file, taking
     * into account the current scale, translation, orientation and clipped region. This can be used
     * to decode a bitmap from the source file.
     * <p>
     * This method will only work when the image has fully initialised, after {@link SubsamplingScaleImageViewXKt#isReady(SubsamplingScaleImageView)} ()} returns
     * true. It is not guaranteed to work with preloaded bitmaps.
     * <p>
     * The result is written to the fRect argument. Re-use a single instance for efficiency.
     *
     * @param vRect rectangle representing the view area to interpret.
     * @param fRect rectangle instance to which the result will be written. Re-use for efficiency.
     */
    public void viewToFileRect(Rect vRect, Rect fRect) {
        if (vTranslate == null || !readySent) {
            return;
        }
        fRect.set(
                (int) viewToSourceX(vRect.left),
                (int) viewToSourceY(vRect.top),
                (int) viewToSourceX(vRect.right),
                (int) viewToSourceY(vRect.bottom));
        fileSRect(fRect, fRect);
        fRect.set(
                Math.max(0, fRect.left),
                Math.max(0, fRect.top),
                Math.min(sWidth, fRect.right),
                Math.min(sHeight, fRect.bottom)
        );
        if (sRegion != null) {
            fRect.offset(sRegion.left, sRegion.top);
        }
    }

    /**
     * Find the area of the source file that is currently visible on screen, taking into account the
     * current scale, translation, orientation and clipped region. This is a convenience method; see
     * {@link #viewToFileRect(Rect, Rect)}.
     *
     * @param fRect rectangle instance to which the result will be written. Re-use for efficiency.
     */
    public void visibleFileRect(Rect fRect) {
        if (vTranslate == null || !readySent) {
            return;
        }
        fRect.set(0, 0, getWidth(), getHeight());
        viewToFileRect(fRect, fRect);
    }


    /**
     * Convert source to view x coordinate.
     */
    public float sourceToViewX(float sx) {
        if (vTranslate == null) {
            return Float.NaN;
        }
        return (sx * scale) + vTranslate.x;
    }

    /**
     * Convert source to view y coordinate.
     */
    public float sourceToViewY(float sy) {
        if (vTranslate == null) {
            return Float.NaN;
        }
        return (sy * scale) + vTranslate.y;
    }

    /**
     * Convert source rect to screen rect, integer values.
     */
    private void sourceToViewRect(@NonNull Rect sRect, @NonNull Rect vTarget) {
        vTarget.set(
                (int) sourceToViewX(sRect.left),
                (int) sourceToViewY(sRect.top),
                (int) sourceToViewX(sRect.right),
                (int) sourceToViewY(sRect.bottom)
        );
    }

    /**
     * Get the translation required to place a given source coordinate at the center of the screen, with the center
     * adjusted for asymmetric padding. Accepts the desired scale as an argument, so this is independent of current
     * translate and scale. The result is fitted to bounds, putting the image point as near to the screen center as permitted.
     */
    @NonNull
    private PointF vTranslateForSCenter(float sCenterX, float sCenterY, float scale) {
        int vxCenter = getPaddingLeft() + (getWidth() - getPaddingRight() - getPaddingLeft()) / 2;
        int vyCenter = getPaddingTop() + (getHeight() - getPaddingBottom() - getPaddingTop()) / 2;
        if (satTemp == null) {
            satTemp = new ScaleAndTranslate(0, new PointF(0, 0));
        }
        satTemp.setScale(scale);
        satTemp.getVTranslate().set(vxCenter - (sCenterX * scale), vyCenter - (sCenterY * scale));
        fitToBounds(true, satTemp);
        return satTemp.getVTranslate();
    }

    /**
     * Given a requested source center and scale, calculate what the actual center will have to be to keep the image in
     * pan limits, keeping the requested center as near to the middle of the screen as allowed.
     */
    @NonNull
    public PointF limitedSCenter(float sCenterX, float sCenterY, float scale, @NonNull PointF sTarget) {
        PointF vTranslate = vTranslateForSCenter(sCenterX, sCenterY, scale);
        int vxCenter = getPaddingLeft() + (getWidth() - getPaddingRight() - getPaddingLeft()) / 2;
        int vyCenter = getPaddingTop() + (getHeight() - getPaddingBottom() - getPaddingTop()) / 2;
        float sx = (vxCenter - vTranslate.x) / scale;
        float sy = (vyCenter - vTranslate.y) / scale;
        sTarget.set(sx, sy);
        return sTarget;
    }

    /**
     * Returns the minimum allowed scale.
     */
    public float minScale() {
        int vPadding = getPaddingBottom() + getPaddingTop();
        int hPadding = getPaddingLeft() + getPaddingRight();
        if (minimumScaleType == ViewValues.SCALE_TYPE_CENTER_CROP || minimumScaleType == ViewValues.SCALE_TYPE_START) {
            return Math.max((getWidth() - hPadding) / (float) sWidth(), (getHeight() - vPadding) / (float) sHeight());
        } else if (minimumScaleType == ViewValues.SCALE_TYPE_CUSTOM && minScale > 0) {
            return minScale;
        } else {
            return Math.min((getWidth() - hPadding) / (float) sWidth(), (getHeight() - vPadding) / (float) sHeight());
        }
    }

    /**
     * Adjust a requested scale to be within the allowed limits.
     */
    public float limitedScale(float targetScale) {
        targetScale = Math.max(minScale(), targetScale);
        targetScale = Math.min(maxScale, targetScale);
        return targetScale;
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
    private float ease(int type, long time, float from, float change, long duration) {
        switch (type) {
            case ViewValues.EASE_IN_OUT_QUAD:
                return easeInOutQuad(time, from, change, duration);
            case ViewValues.EASE_OUT_QUAD:
                return easeOutQuad(time, from, change, duration);
            default:
                throw new IllegalStateException("Unexpected easing type: " + type);
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
    private float easeOutQuad(long time, float from, float change, long duration) {
        float progress = (float) time / (float) duration;
        return -change * progress * (progress - 2) + from;
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
    private float easeInOutQuad(long time, float from, float change, long duration) {
        float timeF = time / (duration / 2f);
        if (timeF < 1) {
            return (change / 2f * timeF * timeF) + from;
        } else {
            timeF--;
            return (-change / 2f) * (timeF * (timeF - 2) - 1) + from;
        }
    }

    /**
     * Debug logger
     */
    @AnyThread
    public void debug(String message, Object... args) {
        if (debug) {
            Log.d(TAG, String.format(message, args));
        }
    }

    /**
     * For debug overlays. Scale pixel value according to screen density.
     */
    private int px(int px) {
        return (int) (density * px);
    }

    /**
     * Calculate how much further the image can be panned in each direction. The results are set on
     * the supplied {@link RectF} and expressed as screen pixels. For example, if the image cannot be
     * panned any further towards the left, the value of {@link RectF#left} will be set to 0.
     *
     * @param vTarget target object for results. Re-use for efficiency.
     */
    public final void getPanRemaining(RectF vTarget) {
        if (!SubsamplingScaleImageViewXKt.isReady(this)) {
            return;
        }

        float scaleWidth = scale * sWidth();
        float scaleHeight = scale * sHeight();

        if (panLimit == ViewValues.PAN_LIMIT_CENTER) {
            vTarget.top = Math.max(0, -(vTranslate.y - (getHeight() / 2)));
            vTarget.left = Math.max(0, -(vTranslate.x - (getWidth() / 2)));
            vTarget.bottom = Math.max(0, vTranslate.y - ((getHeight() / 2) - scaleHeight));
            vTarget.right = Math.max(0, vTranslate.x - ((getWidth() / 2) - scaleWidth));
        } else if (panLimit == ViewValues.PAN_LIMIT_OUTSIDE) {
            vTarget.top = Math.max(0, -(vTranslate.y - getHeight()));
            vTarget.left = Math.max(0, -(vTranslate.x - getWidth()));
            vTarget.bottom = Math.max(0, vTranslate.y + scaleHeight);
            vTarget.right = Math.max(0, vTranslate.x + scaleWidth);
        } else {
            vTarget.top = Math.max(0, -vTranslate.y);
            vTarget.left = Math.max(0, -vTranslate.x);
            vTarget.bottom = Math.max(0, (scaleHeight + vTranslate.y) - getHeight());
            vTarget.right = Math.max(0, (scaleWidth + vTranslate.x) - getWidth());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOnLongClickListener(OnLongClickListener onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
    }

    private void sendStateChanged(float oldScale, PointF oldVTranslate, int origin) {
        if (onStateChangedListener != null && scale != oldScale) {
            onStateChangedListener.onScaleChanged(scale, origin);
        }
        if (onStateChangedListener != null && !vTranslate.equals(oldVTranslate)) {
            onStateChangedListener.onCenterChanged(SubsamplingScaleImageViewXKt.getCenter(this), origin);
        }
    }
}
