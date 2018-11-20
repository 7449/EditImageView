package com.image.edit.action;

import android.graphics.Canvas;
import android.support.annotation.NonNull;

import com.image.edit.EditImageView;
import com.image.edit.cache.EditImageCache;

/**
 * @author y
 * @create 2018/11/20
 */
public interface OnEditImageBaseActionListener {

    void init(@NonNull EditImageView editImageView);

    void onDraw(@NonNull EditImageView editImageView, @NonNull Canvas canvas);

    void onDown(@NonNull EditImageView editImageView, float x, float y);

    void onMove(@NonNull EditImageView editImageView, float x, float y);

    void onUp(@NonNull EditImageView editImageView, float x, float y);

    void onLastImage(@NonNull EditImageView editImageView, @NonNull EditImageCache editImageCache);
}
