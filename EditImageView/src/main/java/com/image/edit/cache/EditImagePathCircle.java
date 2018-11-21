package com.image.edit.cache;

import android.graphics.PointF;

/**
 * @author y
 * @create 2018/11/21
 */
public class EditImagePathCircle {

    public PointF startPointF;
    public PointF endPointF;
    public float radius;
    public float width;
    public int color;

    public EditImagePathCircle(PointF startPointF, PointF endPointF, float radius, float width, int color) {
        this.startPointF = startPointF;
        this.endPointF = endPointF;
        this.radius = radius;
        this.width = width;
        this.color = color;
    }
}
