package com.image.edit.simple;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.annotation.NonNull;
import android.text.TextPaint;

import com.image.edit.EditImageView;
import com.image.edit.OnEditImageInitializeListener;

/**
 * @author y
 * @create 2018/11/20
 */
public class SimpleOnEditImageInitializeListener implements OnEditImageInitializeListener {

    @NonNull
    @Override
    public Paint initPointPaint(@NonNull EditImageView editImageView) {
        Paint paint = new Paint();
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setPathEffect(new PathEffect());
        paint.setStyle(Paint.Style.STROKE);
        return paint;
    }

    @NonNull
    @Override
    public Paint initEraserPaint(@NonNull EditImageView editImageView) {
        Paint eraserPaint = new Paint();
        eraserPaint.setAlpha(0);
        eraserPaint.setColor(Color.TRANSPARENT);
        eraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        eraserPaint.setAntiAlias(true);
        eraserPaint.setDither(true);
        eraserPaint.setStyle(Paint.Style.STROKE);
        eraserPaint.setStrokeJoin(Paint.Join.ROUND);
        eraserPaint.setStrokeCap(Paint.Cap.ROUND);
        eraserPaint.setPathEffect(new PathEffect());
        return eraserPaint;
    }

    @NonNull
    @Override
    public TextPaint initTextPaint(@NonNull EditImageView editImageView) {
        TextPaint textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        return textPaint;
    }

    @NonNull
    @Override
    public Paint initTextFramePaint(@NonNull EditImageView editImageView) {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        return paint;
    }
}
