package com.image.edit;

import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.text.TextPaint;

/**
 * @author y
 * @create 2018/11/20
 */
public interface OnEditImageInitializeListener {

    /**
     * 初始化画笔
     *
     * @param editImageView {@link EditImageView}
     * @return {@link Paint}
     */
    @NonNull
    Paint initPointPaint(@NonNull EditImageView editImageView);

    /**
     * 初始化橡皮擦
     *
     * @param editImageView {@link EditImageView}
     * @return {@link Paint}
     */
    @NonNull
    Paint initEraserPaint(@NonNull EditImageView editImageView);

    /**
     * 初始化文字画笔
     *
     * @param editImageView {@link EditImageView}
     * @return {@link Paint}
     */
    @NonNull
    TextPaint initTextPaint(@NonNull EditImageView editImageView);

    /**
     * 初始化文字框画笔
     *
     * @param editImageView {@link EditImageView}
     * @return {@link Paint}
     */
    @NonNull
    Paint initTextFramePaint(@NonNull EditImageView editImageView);
}
