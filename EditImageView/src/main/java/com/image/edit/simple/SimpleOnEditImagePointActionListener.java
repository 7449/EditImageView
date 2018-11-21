package com.image.edit.simple;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.image.edit.EditImageView;
import com.image.edit.action.OnEditImagePointActionListener;
import com.image.edit.cache.EditImageCache;
import com.image.edit.cache.EditImagePath;

/**
 * @author y
 * @create 2018/11/20
 */
public class SimpleOnEditImagePointActionListener implements OnEditImagePointActionListener {

    @Nullable
    private Path paintPath;
    private PointF pointF;

    public SimpleOnEditImagePointActionListener() {
        pointF = new PointF();
    }

    @Override
    public void onDraw(@NonNull EditImageView editImageView, @NonNull Canvas canvas) {
        if (paintPath == null) return;
        paintPath.quadTo(pointF.x, pointF.y, pointF.x, pointF.y);
        editImageView.getNewBitmapCanvas().drawPath(paintPath, editImageView.getPointPaint());
    }

    @Override
    public void onDown(@NonNull EditImageView editImageView, float x, float y) {
        paintPath = new Path();
        editImageView.viewToSourceCoord(x, y, pointF);
        paintPath.moveTo(pointF.x, pointF.y);
    }

    @Override
    public void onMove(@NonNull EditImageView editImageView, float x, float y) {
        if (Math.abs(x - pointF.x) >= 3 || Math.abs(y - pointF.y) >= 3) {
            editImageView.viewToSourceCoord(x, y, pointF);
            editImageView.refresh();
        }
    }

    @Override
    public void onUp(@NonNull EditImageView editImageView, float x, float y) {
        onSaveImageCache(editImageView);
        paintPath = null;
    }

    @Override
    public void onSaveImageCache(@NonNull EditImageView editImageView) {
        Paint pointPaint = editImageView.getPointPaint();
        editImageView.setCache(EditImageCache.createPointCache(editImageView.getState(), this, new EditImagePath(paintPath, pointPaint.getStrokeWidth(), pointPaint.getColor())));
    }

    @Override
    public void onLastImageCache(@NonNull EditImageView editImageView, @NonNull EditImageCache editImageCache) {
        Paint paint = editImageView.getPointPaint();
        paint.setColor(editImageCache.editImagePath.color);
        paint.setStrokeWidth(editImageCache.editImagePath.width);
        editImageView.getNewBitmapCanvas().drawPath(editImageCache.editImagePath.path, paint);
    }
}
