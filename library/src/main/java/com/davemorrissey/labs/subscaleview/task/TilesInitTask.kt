package com.davemorrissey.labs.subscaleview.task

import android.content.Context
import android.net.Uri
import android.os.AsyncTask
import android.util.Log
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView.Companion.TAG
import com.davemorrissey.labs.subscaleview.core.getExifOrientation
import com.davemorrissey.labs.subscaleview.debug
import com.davemorrissey.labs.subscaleview.decoder.DecoderFactory
import com.davemorrissey.labs.subscaleview.decoder.ImageRegionDecoder
import com.davemorrissey.labs.subscaleview.onTilesInited
import java.lang.ref.WeakReference

/**
 * Async task used to get image details without blocking the UI thread.
 */
class TilesInitTask(view: SubsamplingScaleImageView, context: Context, decoderFactory: DecoderFactory<out ImageRegionDecoder>, private val source: Uri) : AsyncTask<Void, Void, IntArray>() {
    private val viewRef: WeakReference<SubsamplingScaleImageView> = WeakReference(view)
    private val contextRef: WeakReference<Context> = WeakReference(context)
    private val decoderFactoryRef: WeakReference<DecoderFactory<out ImageRegionDecoder>> = WeakReference(decoderFactory)
    private var decoder: ImageRegionDecoder? = null
    private var exception: Exception? = null

    override fun doInBackground(vararg params: Void): IntArray? {
        try {
            val sourceUri = source.toString()
            val context = contextRef.get()
            val decoderFactory = decoderFactoryRef.get()
            val view = viewRef.get()
            if (context != null && decoderFactory != null && view != null) {
                view.debug("TilesInitTask.doInBackground")
                decoder = decoderFactory.make()
                val dimensions = decoder?.init(context, source) ?: return null
                var sWidth = dimensions.x
                var sHeight = dimensions.y
                val exifOrientation = context.getExifOrientation(sourceUri)
                if (view.sRegion != null) {
                    view.sRegion!!.left = 0.coerceAtLeast(view.sRegion!!.left)
                    view.sRegion!!.top = 0.coerceAtLeast(view.sRegion!!.top)
                    view.sRegion!!.right = sWidth.coerceAtMost(view.sRegion!!.right)
                    view.sRegion!!.bottom = sHeight.coerceAtMost(view.sRegion!!.bottom)
                    sWidth = view.sRegion!!.width()
                    sHeight = view.sRegion!!.height()
                }
                return intArrayOf(sWidth, sHeight, exifOrientation)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialise bitmap decoder", e)
            this.exception = e
        }
        return null
    }

    override fun onPostExecute(xyo: IntArray?) {
        val view = viewRef.get()
        if (view != null) {
            if (decoder != null && xyo != null && xyo.size == 3) {
                view.onTilesInited(decoder!!, xyo[0], xyo[1], xyo[2])
            } else if (exception != null && view.onImageEventListener != null) {
                view.onImageEventListener?.onImageLoadError(exception!!)
            }
        }
    }
}