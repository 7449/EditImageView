package com.image.edit.action;

import android.graphics.Canvas;
import android.support.annotation.NonNull;

import com.image.edit.EditImageView;
import com.image.edit.cache.EditImageText;

/**
 * @author y
 * @create 2018/11/20
 */
public interface OnEditImageTextActionListener extends OnEditImageBaseActionListener {
    void onDrawText(@NonNull EditImageView editImageView, @NonNull EditImageText editImageText, @NonNull Canvas canvas);

    void onSaveText(@NonNull EditImageView editImageView);
}
