package com.image.edit.cache;

import android.graphics.PointF;

/**
 * @author y
 * @create 2018/11/21
 */
public class EditImagePathRect {

    public PointF startPointF;
    public PointF endPointF;
    public float width;
    public int color;

    public EditImagePathRect(PointF startPointF, PointF endPointF, float width, int color) {
        this.startPointF = startPointF;
        this.endPointF = endPointF;
        this.width = width;
        this.color = color;
    }
}
