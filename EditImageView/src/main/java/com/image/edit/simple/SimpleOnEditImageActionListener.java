package com.image.edit.simple;

import android.graphics.Canvas;
import android.support.annotation.NonNull;

import com.image.edit.EditImageView;
import com.image.edit.action.OnEditImageBaseActionListener;
import com.image.edit.cache.EditImageCache;

/**
 * @author y
 * @create 2018/11/20
 */
public class SimpleOnEditImageActionListener implements OnEditImageBaseActionListener {

    @Override
    public void onDraw(@NonNull EditImageView editImageView, @NonNull Canvas canvas) {

    }

    @Override
    public void onDown(@NonNull EditImageView editImageView, float x, float y) {

    }

    @Override
    public void onMove(@NonNull EditImageView editImageView, float x, float y) {

    }

    @Override
    public void onUp(@NonNull EditImageView editImageView, float x, float y) {

    }

    @Override
    public void onSaveImageCache(@NonNull EditImageView editImageView) {

    }

    @Override
    public void onLastImageCache(@NonNull EditImageView editImageView, @NonNull EditImageCache editImageCache) {

    }
}
