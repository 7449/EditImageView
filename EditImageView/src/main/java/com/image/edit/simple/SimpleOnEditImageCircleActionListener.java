package com.image.edit.simple;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.support.annotation.NonNull;

import com.image.edit.EditImageView;
import com.image.edit.action.OnEditImagePointActionListener;
import com.image.edit.cache.EditImageCache;
import com.image.edit.cache.EditImagePathCircle;
import com.image.edit.helper.MatrixAndRectHelper;

/**
 * @author y
 * @create 2018/11/20
 */
public class SimpleOnEditImageCircleActionListener implements OnEditImagePointActionListener {

    private PointF startPointF;
    private PointF endPointF;
    private float currentRadius = 0;

    public SimpleOnEditImageCircleActionListener() {
    }

    @Override
    public void onDraw(@NonNull EditImageView editImageView, @NonNull Canvas canvas) {
        if (startPointF == null || endPointF == null) return;
        canvas.drawCircle((startPointF.x + endPointF.x) / 2, (startPointF.y + endPointF.y) / 2, currentRadius, editImageView.getPointPaint());
    }

    @Override
    public void onDown(@NonNull EditImageView editImageView, float x, float y) {
        startPointF = new PointF();
        endPointF = new PointF();
        startPointF.set(x, y);
    }

    @Override
    public void onMove(@NonNull EditImageView editImageView, float x, float y) {
        currentRadius = (float) (Math.sqrt((x - startPointF.x) * (x - startPointF.x) + (y - startPointF.y) * (y - startPointF.y))) / 2;
        endPointF.set(x, y);
        editImageView.refresh();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onUp(@NonNull EditImageView editImageView, float x, float y) {
        MatrixAndRectHelper.refreshMatrix(editImageView.getNewBitmapCanvas(), editImageView.getSupperMatrix(),
                (dx, dy, scaleX, scaleY) -> editImageView.getNewBitmapCanvas().drawCircle((startPointF.x + endPointF.x) / 2, (startPointF.y + endPointF.y) / 2, currentRadius, editImageView.getPointPaint()));
        editImageView.viewToSourceCoord(startPointF, startPointF);
        editImageView.viewToSourceCoord(endPointF, endPointF);
        onSaveImageCache(editImageView);
        startPointF = null;
        endPointF = null;
        currentRadius = 0;
    }

    @Override
    public void onSaveImageCache(@NonNull EditImageView editImageView) {
        Paint pointPaint = editImageView.getPointPaint();
        float radius = currentRadius / editImageView.getScale();
        float width = editImageView.getPointPaint().getStrokeWidth() / editImageView.getScale();
        editImageView.setCache(EditImageCache.createPointCircleCache(editImageView.getState(),
                this, new EditImagePathCircle(startPointF, endPointF, radius, width, pointPaint.getColor())));
    }

    @Override
    public void onLastImageCache(@NonNull EditImageView editImageView, @NonNull EditImageCache editImageCache) {
        Paint paint = editImageView.getPointPaint();
        paint.setColor(editImageCache.editImagePathCircle.color);
        paint.setStrokeWidth(editImageCache.editImagePathCircle.width);
        editImageView.getNewBitmapCanvas().drawCircle(
                (editImageCache.editImagePathCircle.startPointF.x + editImageCache.editImagePathCircle.endPointF.x) / 2,
                (editImageCache.editImagePathCircle.startPointF.y + editImageCache.editImagePathCircle.endPointF.y) / 2,
                editImageCache.editImagePathCircle.radius,
                paint);
    }
}
