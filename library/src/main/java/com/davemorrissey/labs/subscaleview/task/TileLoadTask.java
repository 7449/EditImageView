package com.davemorrissey.labs.subscaleview.task;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.davemorrissey.labs.subscaleview.core.Tile;
import com.davemorrissey.labs.subscaleview.decoder.ImageRegionDecoder;

import java.lang.ref.WeakReference;

import static com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView.TAG;

/**
 * Async task used to load images without blocking the UI thread.
 */
public class TileLoadTask extends AsyncTask<Void, Void, Bitmap> {

    private final WeakReference<SubsamplingScaleImageView> viewRef;
    private final WeakReference<ImageRegionDecoder> decoderRef;
    private final WeakReference<Tile> tileRef;
    private Exception exception;

    public TileLoadTask(SubsamplingScaleImageView view, ImageRegionDecoder decoder, Tile tile) {
        this.viewRef = new WeakReference<>(view);
        this.decoderRef = new WeakReference<>(decoder);
        this.tileRef = new WeakReference<>(tile);
        tile.setLoading(true);
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        try {
            SubsamplingScaleImageView view = viewRef.get();
            ImageRegionDecoder decoder = decoderRef.get();
            Tile tile = tileRef.get();
            if (decoder != null && tile != null && view != null && decoder.isReady() && tile.getVisible()) {
                view.debug("TileLoadTask.doInBackground, tile.sRect=%s, tile.sampleSize=%d", tile.getSRect(), tile.getSampleSize());
                view.decoderLock.readLock().lock();
                try {
                    if (decoder.isReady()) {
                        // Update tile's file sRect according to rotation
                        view.fileSRect(tile.getSRect(), tile.getFileSRect());
                        if (view.sRegion != null) {
                            tile.getFileSRect().offset(view.sRegion.left, view.sRegion.top);
                        }
                        return decoder.decodeRegion(tile.getFileSRect(), tile.getSampleSize());
                    } else {
                        tile.setLoading(false);
                    }
                } finally {
                    view.decoderLock.readLock().unlock();
                }
            } else if (tile != null) {
                tile.setLoading(false);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to decode tile", e);
            this.exception = e;
        } catch (OutOfMemoryError e) {
            Log.e(TAG, "Failed to decode tile - OutOfMemoryError", e);
            this.exception = new RuntimeException(e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        final SubsamplingScaleImageView subsamplingScaleImageView = viewRef.get();
        final Tile tile = tileRef.get();
        if (subsamplingScaleImageView != null && tile != null) {
            if (bitmap != null) {
                tile.setBitmap(bitmap);
                tile.setLoading(false);
                onTileLoaded(subsamplingScaleImageView);
            } else if (exception != null && subsamplingScaleImageView.onImageEventListener != null) {
                subsamplingScaleImageView.onImageEventListener.onTileLoadError(exception);
            }
        }
    }

    /**
     * Called by worker task when a tile has loaded. Redraws the view.
     */
    private synchronized void onTileLoaded(SubsamplingScaleImageView scaleImageView) {
        scaleImageView.debug("onTileLoaded");
        scaleImageView.checkReady();
        scaleImageView.checkImageLoaded();
        if (scaleImageView.isBaseLayerReady() && scaleImageView.bitmap != null) {
            if (!scaleImageView.bitmapIsCached) {
                scaleImageView.bitmap.recycle();
            }
            scaleImageView.bitmap = null;
            if (scaleImageView.onImageEventListener != null && scaleImageView.bitmapIsCached) {
                scaleImageView.onImageEventListener.onPreviewReleased();
            }
            scaleImageView.bitmapIsPreview = false;
            scaleImageView.bitmapIsCached = false;
        }
        scaleImageView.invalidate();
    }
}
