package com.image.edit;

import android.graphics.Color;
import android.graphics.Paint;

/**
 * 初始化的默认配置
 *
 * @author y
 * @create 2018/11/17
 */
public class EditImageConfig {

    /**
     * 最大缓存数
     */
    public int maxCacheCount = 1000;

    /**
     * 线条画笔颜色
     */
    public int pointColor = Color.RED;

    /**
     * 线条画笔宽度
     */
    public float pointWidth = 20;

    /**
     * 橡皮擦宽度
     */
    public float eraserPointWidth = 25;

    /**
     * 橡皮擦痕迹是否添加到缓存中
     */
    public boolean eraserSave = true;

    /**
     * 文字画笔颜色
     */
    public int textPaintColor = Color.RED;

    /**
     * 文字是否可以旋转
     */
    public boolean isTextRotateMode = true;

    /**
     * 文字大小
     */
    public float textPaintSize = 60;

    /**
     * 文字位置
     */
    public Paint.Align textPaintAlign = Paint.Align.LEFT;

    /**
     * 文字框颜色
     */
    public int textFramePaintColor = Color.BLACK;

    /**
     * 文字框宽度
     */
    public float textFramePaintWidth = 4;

    /**
     * 是否显示文字框
     */
    public boolean showTextMoveBox = true;

    /**
     * 文字框删除图片资源Id
     */
    public int textDeleteDrawableId = R.drawable.ic_edit_image_delete;

    /**
     * 文字框放大图片资源Id
     */
    public int textRotateDrawableId = R.drawable.ic_edit_image_rotate;

}
