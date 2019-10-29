package com.davemorrissey.labs.subscaleview.task

import android.graphics.Bitmap
import android.graphics.Rect
import android.os.AsyncTask
import android.util.Log
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView.Companion.TAG
import com.davemorrissey.labs.subscaleview.api.debug
import com.davemorrissey.labs.subscaleview.api.fileSRect
import com.davemorrissey.labs.subscaleview.api.onTileLoaded
import com.davemorrissey.labs.subscaleview.decoder.ImageRegionDecoder
import java.lang.ref.WeakReference

/**
 * Async task used to load images without blocking the UI thread.
 */
internal class TileLoadTask(view: SubsamplingScaleImageView, decoder: ImageRegionDecoder, tile: Tile) : AsyncTask<Void, Void, Bitmap>() {

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
                view.debug("TileLoadTask.doInBackground, tile.sRect=%s, tile.sampleSize=%d", tile.sRect
                        ?: "", tile.sampleSize)
                view.decoderLock.readLock().lock()
                try {
                    if (decoder.isReady) {
                        // Update tile's file sRect according to rotation
                        safeLet(tile.sRect, tile.fileSRect) { sRect, fileSRect ->
                            view.fileSRect(sRect, fileSRect)
                        }
                        view.sRegion?.let {
                            tile.fileSRect?.offset(view.sRegion?.left ?: 0, view.sRegion?.top ?: 0)
                        }
                        return decoder.decodeRegion(tile.fileSRect ?: Rect(), tile.sampleSize)
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
        safeLet(viewRef.get(), tileRef.get()) { imageView, tile ->
            if (bitmap != null) {
                tile.bitmap = bitmap
                tile.loading = false
                imageView.onTileLoaded()
            } else {
                exception?.let { imageView.onImageEventListener?.onTileLoadError(it) }
            }
        }
    }
}
