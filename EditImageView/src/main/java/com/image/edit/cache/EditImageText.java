package com.image.edit.cache;

import android.graphics.PointF;

/**
 * @author y
 * @create 2018/11/17
 */
public class EditImageText {
    public PointF pointF;
    public float scale;
    public float rotate;
    public String text;
    public int color;
    public float textSize;

    public EditImageText(PointF pointF, float scale, float rotate, String text, int color, float textSize) {
        this.pointF = pointF;
        this.scale = scale;
        this.rotate = rotate;
        this.text = text;
        this.color = color;
        this.textSize = textSize;
    }
}
