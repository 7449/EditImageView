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

    /**
     * 绘制
     *
     * @param editImageView {@link EditImageView}
     * @param canvas        {@link Canvas}
     */
    void onDraw(@NonNull EditImageView editImageView, @NonNull Canvas canvas);

    /**
     * 按下
     *
     * @param editImageView {@link EditImageView}
     * @param x             x
     * @param y             y
     */
    void onDown(@NonNull EditImageView editImageView, float x, float y);

    /**
     * 移动
     *
     * @param editImageView {@link EditImageView}
     * @param x             x
     * @param y             y
     */
    void onMove(@NonNull EditImageView editImageView, float x, float y);

    /**
     * 抬起
     *
     * @param editImageView {@link EditImageView}
     * @param x             x
     * @param y             y
     */
    void onUp(@NonNull EditImageView editImageView, float x, float y);

    /**
     * 保存缓存
     *
     * @param editImageView {@link EditImageView}
     */
    void onSaveImageCache(@NonNull EditImageView editImageView);

    /**
     * 回退到上一步
     *
     * @param editImageView  {@link EditImageView}
     * @param editImageCache {@link EditImageCache}
     */
    void onLastImageCache(@NonNull EditImageView editImageView, @NonNull EditImageCache editImageCache);
}
