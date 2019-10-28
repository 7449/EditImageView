package com.davemorrissey.labs.subscaleview.task;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.davemorrissey.labs.subscaleview.core.CoreKt;
import com.davemorrissey.labs.subscaleview.decoder.DecoderFactory;
import com.davemorrissey.labs.subscaleview.decoder.ImageDecoder;

import java.lang.ref.WeakReference;

import static com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView.TAG;

/**
 * Async task used to load bitmap without blocking the UI thread.
 */
public class BitmapLoadTask extends AsyncTask<Void, Void, Integer> {
    private final WeakReference<SubsamplingScaleImageView> viewRef;
    private final WeakReference<Context> contextRef;
    private final WeakReference<DecoderFactory<? extends ImageDecoder>> decoderFactoryRef;
    private final Uri source;
    private final boolean preview;
    private Bitmap bitmap;
    private Exception exception;

    public BitmapLoadTask(SubsamplingScaleImageView view, Context context, DecoderFactory<? extends ImageDecoder> decoderFactory, Uri source, boolean preview) {
        this.viewRef = new WeakReference<>(view);
        this.contextRef = new WeakReference<>(context);
        this.decoderFactoryRef = new WeakReference<DecoderFactory<? extends ImageDecoder>>(decoderFactory);
        this.source = source;
        this.preview = preview;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        try {
            String sourceUri = source.toString();
            Context context = contextRef.get();
            DecoderFactory<? extends ImageDecoder> decoderFactory = decoderFactoryRef.get();
            SubsamplingScaleImageView view = viewRef.get();
            if (context != null && decoderFactory != null && view != null) {
                view.debug("BitmapLoadTask.doInBackground");
                bitmap = decoderFactory.make().decode(context, source);
                return CoreKt.getExifOrientation(context, sourceUri);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to load bitmap", e);
            this.exception = e;
        } catch (OutOfMemoryError e) {
            Log.e(TAG, "Failed to load bitmap - OutOfMemoryError", e);
            this.exception = new RuntimeException(e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Integer orientation) {
        SubsamplingScaleImageView subsamplingScaleImageView = viewRef.get();
        if (subsamplingScaleImageView != null) {
            if (bitmap != null && orientation != null) {
                if (preview) {
                    subsamplingScaleImageView.onPreviewLoaded(bitmap);
                } else {
                    subsamplingScaleImageView.onImageLoaded(bitmap, orientation, false);
                }
            } else if (exception != null && subsamplingScaleImageView.onImageEventListener != null) {
                if (preview) {
                    subsamplingScaleImageView.onImageEventListener.onPreviewLoadError(exception);
                } else {
                    subsamplingScaleImageView.onImageEventListener.onImageLoadError(exception);
                }
            }
        }
    }
}