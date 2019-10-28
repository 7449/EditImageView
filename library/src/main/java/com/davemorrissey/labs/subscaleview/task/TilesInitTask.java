package com.davemorrissey.labs.subscaleview.task;

import android.content.Context;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.davemorrissey.labs.subscaleview.core.CoreKt;
import com.davemorrissey.labs.subscaleview.decoder.DecoderFactory;
import com.davemorrissey.labs.subscaleview.decoder.ImageRegionDecoder;

import java.lang.ref.WeakReference;

import static com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView.TAG;

/**
 * Async task used to get image details without blocking the UI thread.
 */
public class TilesInitTask extends AsyncTask<Void, Void, int[]> {
    private final WeakReference<SubsamplingScaleImageView> viewRef;
    private final WeakReference<Context> contextRef;
    private final WeakReference<DecoderFactory<? extends ImageRegionDecoder>> decoderFactoryRef;
    private final Uri source;
    private ImageRegionDecoder decoder;
    private Exception exception;

    public TilesInitTask(SubsamplingScaleImageView view, Context context, DecoderFactory<? extends ImageRegionDecoder> decoderFactory, Uri source) {
        this.viewRef = new WeakReference<>(view);
        this.contextRef = new WeakReference<>(context);
        this.decoderFactoryRef = new WeakReference<DecoderFactory<? extends ImageRegionDecoder>>(decoderFactory);
        this.source = source;
    }

    @Override
    protected int[] doInBackground(Void... params) {
        try {
            String sourceUri = source.toString();
            Context context = contextRef.get();
            DecoderFactory<? extends ImageRegionDecoder> decoderFactory = decoderFactoryRef.get();
            SubsamplingScaleImageView view = viewRef.get();
            if (context != null && decoderFactory != null && view != null) {
                view.debug("TilesInitTask.doInBackground");
                decoder = decoderFactory.make();
                Point dimensions = decoder.init(context, source);
                int sWidth = dimensions.x;
                int sHeight = dimensions.y;
                int exifOrientation = CoreKt.getExifOrientation(context, sourceUri);
                if (view.sRegion != null) {
                    view.sRegion.left = Math.max(0, view.sRegion.left);
                    view.sRegion.top = Math.max(0, view.sRegion.top);
                    view.sRegion.right = Math.min(sWidth, view.sRegion.right);
                    view.sRegion.bottom = Math.min(sHeight, view.sRegion.bottom);
                    sWidth = view.sRegion.width();
                    sHeight = view.sRegion.height();
                }
                return new int[]{sWidth, sHeight, exifOrientation};
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialise bitmap decoder", e);
            this.exception = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(int[] xyo) {
        final SubsamplingScaleImageView view = viewRef.get();
        if (view != null) {
            if (decoder != null && xyo != null && xyo.length == 3) {
                view.onTilesInited(decoder, xyo[0], xyo[1], xyo[2]);
            } else if (exception != null && view.onImageEventListener != null) {
                view.onImageEventListener.onImageLoadError(exception);
            }
        }
    }
}