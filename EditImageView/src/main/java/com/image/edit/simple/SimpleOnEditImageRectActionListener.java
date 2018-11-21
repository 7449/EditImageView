package com.image.edit.simple;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.support.annotation.NonNull;

import com.image.edit.EditImageView;
import com.image.edit.action.OnEditImagePointActionListener;
import com.image.edit.cache.EditImageCache;
import com.image.edit.cache.EditImagePathRect;
import com.image.edit.helper.MatrixAndRectHelper;

/**
 * @author y
 * @create 2018/11/20
 */
public class SimpleOnEditImageRectActionListener implements OnEditImagePointActionListener {

    private PointF startPointF;
    private PointF endPointF;

    public SimpleOnEditImageRectActionListener() {
    }

    @Override
    public void onDraw(@NonNull EditImageView editImageView, @NonNull Canvas canvas) {
        if (startPointF == null || endPointF == null) return;
        canvas.drawRect(startPointF.x, startPointF.y, endPointF.x, endPointF.y, editImageView.getPointPaint());
    }

    @Override
    public void onDown(@NonNull EditImageView editImageView, float x, float y) {
        startPointF = new PointF();
        endPointF = new PointF();
        startPointF.set(x, y);
    }

    @Override
    public void onMove(@NonNull EditImageView editImageView, float x, float y) {
        endPointF.set(x, y);
        editImageView.refresh();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onUp(@NonNull EditImageView editImageView, float x, float y) {
        MatrixAndRectHelper.refreshMatrix(editImageView.getNewBitmapCanvas(), editImageView.getSupperMatrix(),
                (dx, dy, scaleX, scaleY) -> editImageView.getNewBitmapCanvas().drawRect(startPointF.x, startPointF.y, endPointF.x, endPointF.y, editImageView.getPointPaint()));
        editImageView.viewToSourceCoord(startPointF, startPointF);
        editImageView.viewToSourceCoord(endPointF, endPointF);
        onSaveImageCache(editImageView);
        startPointF = null;
        endPointF = null;
    }

    @Override
    public void onSaveImageCache(@NonNull EditImageView editImageView) {
        Paint pointPaint = editImageView.getPointPaint();
        float width = editImageView.getPointPaint().getStrokeWidth() / editImageView.getScale();
        editImageView.setCache(EditImageCache.createPointRectCache(editImageView.getState(), this,
                new EditImagePathRect(startPointF, endPointF, width, pointPaint.getColor())));
    }

    @Override
    public void onLastImageCache(@NonNull EditImageView editImageView, @NonNull EditImageCache editImageCache) {
        Paint paint = editImageView.getPointPaint();
        paint.setStrokeWidth(editImageCache.editImagePathRect.width);
        paint.setColor(editImageCache.editImagePathRect.color);
        editImageView.getNewBitmapCanvas().drawRect(
                editImageCache.editImagePathRect.startPointF.x,
                editImageCache.editImagePathRect.startPointF.y,
                editImageCache.editImagePathRect.endPointF.x,
                editImageCache.editImagePathRect.endPointF.y,
                paint);
    }
}
