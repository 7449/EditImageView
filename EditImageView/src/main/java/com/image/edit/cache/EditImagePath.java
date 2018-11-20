package com.image.edit.cache;


import android.graphics.Path;

/**
 * @author y
 * @create 2018/11/17
 */
public class EditImagePath {
    public Path path;
    public float width;
    public int color;

    public EditImagePath(Path path, float width, int color) {
        this.path = path;
        this.width = width;
        this.color = color;
    }
}
