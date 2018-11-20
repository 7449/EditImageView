package com.image.edit.helper;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

/**
 * @author y
 * @create 2018/11/20
 */
public class BitmapHelper {

    public static void recycle(@Nullable Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

}
