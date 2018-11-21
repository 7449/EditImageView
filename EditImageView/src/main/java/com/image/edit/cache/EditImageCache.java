package com.image.edit.cache;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.davemorrissey.labs.subscaleview.ImageViewState;
import com.image.edit.EditType;
import com.image.edit.action.OnEditImageBaseActionListener;

/**
 * @author y
 * @create 2018/11/17
 */
public class EditImageCache {
    public ImageViewState imageViewState;
    public EditType editType;
    public OnEditImageBaseActionListener onEditImageBaseActionListener;

    public EditImagePath editImagePath;
    public EditImagePathLine editImagePathLine;
    public EditImagePathRect editImagePathRect;
    public EditImagePathCircle editImagePathCircle;
    public EditImageText editImageText;

    public void reset() {
        imageViewState = null;
        editType = null;
        onEditImageBaseActionListener = null;
        editImagePath = null;
        editImagePathLine = null;
        editImagePathRect = null;
        editImagePathCircle = null;
        editImageText = null;
    }

    private EditImageCache(@Nullable ImageViewState imageViewState, @NonNull OnEditImageBaseActionListener onEditImageBaseActionListener, @NonNull EditType editType, EditImagePath editImagePath) {
        this.imageViewState = imageViewState;
        this.editType = editType;
        this.editImagePath = editImagePath;
        this.onEditImageBaseActionListener = onEditImageBaseActionListener;
    }

    private EditImageCache(@Nullable ImageViewState imageViewState, @NonNull OnEditImageBaseActionListener onEditImageBaseActionListener, @NonNull EditType editType, EditImageText editImageText) {
        this.imageViewState = imageViewState;
        this.editType = editType;
        this.editImageText = editImageText;
        this.onEditImageBaseActionListener = onEditImageBaseActionListener;
    }

    private EditImageCache(@Nullable ImageViewState imageViewState, @NonNull OnEditImageBaseActionListener onEditImageBaseActionListener, @NonNull EditType editType, EditImagePathLine editImagePathLine) {
        this.imageViewState = imageViewState;
        this.editType = editType;
        this.editImagePathLine = editImagePathLine;
        this.onEditImageBaseActionListener = onEditImageBaseActionListener;
    }

    private EditImageCache(@Nullable ImageViewState imageViewState, @NonNull OnEditImageBaseActionListener onEditImageBaseActionListener, @NonNull EditType editType, EditImagePathRect editImagePathRect) {
        this.imageViewState = imageViewState;
        this.editType = editType;
        this.editImagePathRect = editImagePathRect;
        this.onEditImageBaseActionListener = onEditImageBaseActionListener;
    }

    private EditImageCache(@Nullable ImageViewState imageViewState, @NonNull OnEditImageBaseActionListener onEditImageBaseActionListener, @NonNull EditType editType, EditImagePathCircle editImagePathCircle) {
        this.imageViewState = imageViewState;
        this.editType = editType;
        this.editImagePathCircle = editImagePathCircle;
        this.onEditImageBaseActionListener = onEditImageBaseActionListener;
    }

    public static EditImageCache createPointCache(@Nullable ImageViewState imageViewState, @NonNull OnEditImageBaseActionListener onEditImageBaseActionListener, @NonNull EditImagePath editImagePath) {
        return new EditImageCache(imageViewState, onEditImageBaseActionListener, EditType.PAINT, editImagePath);
    }

    public static EditImageCache createPointLineCache(@Nullable ImageViewState imageViewState, @NonNull OnEditImageBaseActionListener onEditImageBaseActionListener, @NonNull EditImagePathLine editImagePathLine) {
        return new EditImageCache(imageViewState, onEditImageBaseActionListener, EditType.PAINT, editImagePathLine);
    }

    public static EditImageCache createPointRectCache(@Nullable ImageViewState imageViewState, @NonNull OnEditImageBaseActionListener onEditImageBaseActionListener, @NonNull EditImagePathRect editImagePathRect) {
        return new EditImageCache(imageViewState, onEditImageBaseActionListener, EditType.PAINT, editImagePathRect);
    }

    public static EditImageCache createPointCircleCache(@Nullable ImageViewState imageViewState, @NonNull OnEditImageBaseActionListener onEditImageBaseActionListener, @NonNull EditImagePathCircle editImagePathCircle) {
        return new EditImageCache(imageViewState, onEditImageBaseActionListener, EditType.PAINT, editImagePathCircle);
    }

    public static EditImageCache createEraserPointCache(@Nullable ImageViewState imageViewState, @NonNull OnEditImageBaseActionListener onEditImageBaseActionListener, @NonNull EditImagePath editImagePath) {
        return new EditImageCache(imageViewState, onEditImageBaseActionListener, EditType.ERASER, editImagePath);
    }

    public static EditImageCache createTextCache(@Nullable ImageViewState imageViewState, @NonNull OnEditImageBaseActionListener onEditImageBaseActionListener, @NonNull EditImageText editImageText) {
        return new EditImageCache(imageViewState, onEditImageBaseActionListener, EditType.TEXT, editImageText);
    }
}
