package com.image.edit.cache;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.davemorrissey.labs.subscaleview.ImageViewState;
import com.image.edit.EditType;

/**
 * @author y
 * @create 2018/11/17
 */
public class EditImageCache {
    @Nullable
    public ImageViewState imageViewState;
    @NonNull
    public EditType editType;
    public EditImagePath editImagePath;
    public EditImageText editImageText;

    private EditImageCache(@Nullable ImageViewState imageViewState, @NonNull EditType editType, EditImagePath editImagePath) {
        this.imageViewState = imageViewState;
        this.editType = editType;
        this.editImagePath = editImagePath;
    }

    private EditImageCache(@Nullable ImageViewState imageViewState, @NonNull EditType editType, EditImageText editImageText) {
        this.imageViewState = imageViewState;
        this.editType = editType;
        this.editImageText = editImageText;
    }

    public static EditImageCache createPointCache(@Nullable ImageViewState imageViewState, @NonNull EditImagePath editImagePath) {
        return new EditImageCache(imageViewState, EditType.PAINT, editImagePath);
    }

    public static EditImageCache createEraserPointCache(@Nullable ImageViewState imageViewState, @NonNull EditImagePath editImagePath) {
        return new EditImageCache(imageViewState, EditType.ERASER, editImagePath);
    }

    public static EditImageCache createTextCache(@Nullable ImageViewState imageViewState, @NonNull EditImageText editImageText) {
        return new EditImageCache(imageViewState, EditType.TEXT, editImageText);
    }
}
