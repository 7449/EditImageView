package com.davemorrissey.labs.subscaleview.task

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.util.Log
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView.Companion.TAG
import com.davemorrissey.labs.subscaleview.api.debug
import com.davemorrissey.labs.subscaleview.api.onImageLoaded
import com.davemorrissey.labs.subscaleview.api.onPreviewLoaded
import com.davemorrissey.labs.subscaleview.decoder.DecoderFactory
import com.davemorrissey.labs.subscaleview.decoder.ImageDecoder
import java.lang.ref.WeakReference

/**
 * Async task used to load bitmap without blocking the UI thread.
 */
internal class BitmapLoadTask(view: SubsamplingScaleImageView, context: Context, decoderFactory: DecoderFactory<out ImageDecoder>, private val source: Uri, private val preview: Boolean) : AsyncTask<Void, Void, Int>() {

    private val viewRef: WeakReference<SubsamplingScaleImageView> = WeakReference(view)
    private val contextRef: WeakReference<Context> = WeakReference(context)
    private val decoderFactoryRef: WeakReference<DecoderFactory<out ImageDecoder>> = WeakReference(decoderFactory)
    private var bitmap: Bitmap? = null
    private var exception: Exception? = null

    override fun doInBackground(vararg params: Void): Int? {
        try {
            val sourceUri = source.toString()
            val context = contextRef.get()
            val decoderFactory = decoderFactoryRef.get()
            val view = viewRef.get()
            if (context != null && decoderFactory != null && view != null) {
                view.debug("BitmapLoadTask.doInBackground")
                bitmap = decoderFactory.make().decode(context, source)
                return context.getExifOrientation(sourceUri)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load bitmap", e)
            this.exception = e
        } catch (e: OutOfMemoryError) {
            Log.e(TAG, "Failed to load bitmap - OutOfMemoryError", e)
            this.exception = RuntimeException(e)
        }
        return null
    }

    override fun onPostExecute(orientation: Int?) {
        val subsamplingScaleImageView = viewRef.get()
        subsamplingScaleImageView?.let {
            if (bitmap != null && orientation != null) {
                if (preview) {
                    subsamplingScaleImageView.onPreviewLoaded(bitmap)
                } else {
                    subsamplingScaleImageView.onImageLoaded(bitmap, orientation, false)
                }
            } else {
                exception?.let {
                    if (preview) {
                        subsamplingScaleImageView.onImageEventListener?.onPreviewLoadError(it)
                    } else {
                        subsamplingScaleImageView.onImageEventListener?.onImageLoadError(it)
                    }
                }
            }
        }
    }
}