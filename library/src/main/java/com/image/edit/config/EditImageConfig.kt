package com.image.edit.config

import android.graphics.Color
import android.graphics.Paint
import com.image.edit.R

/**
 * 初始化的默认配置
 *
 * @author y
 * @create 2018/11/17
 */
class EditImageConfig {

    /**
     * 最大缓存数
     */
    var maxCacheCount = 1000

    /**
     * 线条画笔颜色
     */
    var pointColor = Color.RED

    /**
     * 线条画笔宽度
     */
    var pointWidth = 20f

    /**
     * 橡皮擦宽度
     */
    var eraserPointWidth = 25f

    /**
     * 橡皮擦痕迹是否添加到缓存中
     */
    var eraserSave = true

    /**
     * 文字画笔颜色
     */
    var textPaintColor = Color.RED

    /**
     * 文字是否可以旋转
     */
    var isTextRotateMode = true

    /**
     * 文字大小
     */
    var textPaintSize = 60f

    /**
     * 文字位置
     */
    var textPaintAlign: Paint.Align = Paint.Align.LEFT

    /**
     * 文字框颜色
     */
    var textFramePaintColor = Color.BLACK

    /**
     * 文字框宽度
     */
    var textFramePaintWidth = 4f

    /**
     * 是否显示文字框
     */
    var showTextMoveBox = true

    /**
     * 文字框删除图片资源Id
     */
    var textDeleteDrawableId = R.drawable.ic_edit_image_delete

    /**
     * 文字框放大图片资源Id
     */
    var textRotateDrawableId = R.drawable.ic_edit_image_rotate

}
