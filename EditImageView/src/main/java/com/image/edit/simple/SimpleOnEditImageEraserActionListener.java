package com.image.edit.simple;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.image.edit.EditImageView;
import com.image.edit.OnEditImageListener;
import com.image.edit.action.OnEditImageEraserActionListener;
import com.image.edit.cache.EditImageCache;
import com.image.edit.cache.EditImagePath;

import java.util.LinkedList;

/**
 * @author y
 * @create 2018/11/20
 */
public class SimpleOnEditImageEraserActionListener implements OnEditImageEraserActionListener {

    @Nullable
    private Path paintPath;
    private PointF pointF;

    public SimpleOnEditImageEraserActionListener() {
        this.pointF = new PointF();
    }

    @Override
    public void init(@NonNull EditImageView editImageView) {
    }

    @Override
    public void onDraw(@NonNull EditImageView editImageView, @NonNull Canvas canvas) {
        if (paintPath == null) return;
        paintPath.lineTo(pointF.x, pointF.y);
        editImageView.getNewBitmapCanvas().drawPath(paintPath, editImageView.getEraserPaint());
    }

    @Override
    public void onDown(@NonNull EditImageView editImageView, float x, float y) {
        paintPath = new Path();
        editImageView.viewToSourceCoord(x, y, pointF);
        paintPath.moveTo(pointF.x, pointF.y);
    }

    @Override
    public void onMove(@NonNull EditImageView editImageView, float x, float y) {
        editImageView.viewToSourceCoord(x, y, pointF);
        editImageView.refresh();
    }

    @Override
    public void onUp(@NonNull EditImageView editImageView, float x, float y) {
        Paint pointPaint = editImageView.getEraserPaint();
        OnEditImageListener onEditImageListener = editImageView.getOnEditImageListener();
        LinkedList<EditImageCache> cacheArrayList = editImageView.getCacheArrayList();
        if (editImageView.getEditImageConfig().eraserSave) {
            if (cacheArrayList.size() < editImageView.getEditImageConfig().maxCacheCount) {
                cacheArrayList.add(EditImageCache.createEraserPointCache(editImageView.getState(), new EditImagePath(paintPath, pointPaint.getStrokeWidth(), pointPaint.getColor())));
            } else {
                onEditImageListener.onLastCacheMax();
            }
        }
        paintPath = null;
    }

    @Override
    public void onLastImage(@NonNull EditImageView editImageView, @NonNull EditImageCache editImageCache) {
        Paint eraserPaint = editImageView.getEraserPaint();
        eraserPaint.setColor(editImageCache.editImagePath.color);
        eraserPaint.setStrokeWidth(editImageCache.editImagePath.width);
        editImageView.getNewBitmapCanvas().drawPath(editImageCache.editImagePath.path, eraserPaint);
    }
}
