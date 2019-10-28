package com.davemorrissey.labs.subscaleview.task

import android.graphics.Bitmap
import android.os.AsyncTask
import android.util.Log

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.davemorrissey.labs.subscaleview.core.Tile
import com.davemorrissey.labs.subscaleview.decoder.ImageRegionDecoder

import java.lang.ref.WeakReference

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView.TAG

/**
 * Async task used to load images without blocking the UI thread.
 */
class TileLoadTask(view: SubsamplingScaleImageView, decoder: ImageRegionDecoder, tile: Tile) : AsyncTask<Void, Void, Bitmap>() {

    private val viewRef: WeakReference<SubsamplingScaleImageView> = WeakReference(view)
    private val decoderRef: WeakReference<ImageRegionDecoder> = WeakReference(decoder)
    private val tileRef: WeakReference<Tile> = WeakReference(tile)
    private var exception: Exception? = null

    init {
        tile.loading = true
    }

    override fun doInBackground(vararg params: Void): Bitmap? {
        try {
            val view = viewRef.get()
            val decoder = decoderRef.get()
            val tile = tileRef.get()
            if (decoder != null && tile != null && view != null && decoder.isReady && tile.visible) {
                view.debug("TileLoadTask.doInBackground, tile.sRect=%s, tile.sampleSize=%d", tile.sRect, tile.sampleSize)
                view.decoderLock.readLock().lock()
                try {
                    if (decoder.isReady) {
                        // Update tile's file sRect according to rotation
                        view.fileSRect(tile.sRect, tile.fileSRect)
                        if (view.sRegion != null) {
                            tile.fileSRect?.offset(view.sRegion.left, view.sRegion.top)
                        }
                        return decoder.decodeRegion(tile.fileSRect!!, tile.sampleSize)
                    } else {
                        tile.loading = false
                    }
                } finally {
                    view.decoderLock.readLock().unlock()
                }
            } else if (tile != null) {
                tile.loading = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode tile", e)
            this.exception = e
        } catch (e: OutOfMemoryError) {
            Log.e(TAG, "Failed to decode tile - OutOfMemoryError", e)
            this.exception = RuntimeException(e)
        }

        return null
    }

    override fun onPostExecute(bitmap: Bitmap?) {
        val subsamplingScaleImageView = viewRef.get()
        val tile = tileRef.get()
        if (subsamplingScaleImageView != null && tile != null) {
            if (bitmap != null) {
                tile.bitmap = bitmap
                tile.loading = false
                onTileLoaded(subsamplingScaleImageView)
            } else if (exception != null && subsamplingScaleImageView.onImageEventListener != null) {
                subsamplingScaleImageView.onImageEventListener.onTileLoadError(exception!!)
            }
        }
    }

    /**
     * Called by worker task when a tile has loaded. Redraws the view.
     */
    @Synchronized
    private fun onTileLoaded(scaleImageView: SubsamplingScaleImageView) {
        scaleImageView.debug("onTileLoaded")
        scaleImageView.checkReady()
        scaleImageView.checkImageLoaded()
        if (scaleImageView.isBaseLayerReady && scaleImageView.bitmap != null) {
            if (!scaleImageView.bitmapIsCached) {
                scaleImageView.bitmap.recycle()
            }
            scaleImageView.bitmap = null
            if (scaleImageView.onImageEventListener != null && scaleImageView.bitmapIsCached) {
                scaleImageView.onImageEventListener.onPreviewReleased()
            }
            scaleImageView.bitmapIsPreview = false
            scaleImageView.bitmapIsCached = false
        }
        scaleImageView.invalidate()
    }
}
