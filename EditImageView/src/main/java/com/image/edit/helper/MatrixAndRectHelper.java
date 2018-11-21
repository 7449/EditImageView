package com.image.edit.helper;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.NonNull;

import com.image.edit.cache.EditImageText;

/**
 * @author y
 * @create 2018/11/17
 */
public class MatrixAndRectHelper {

    public static void scaleRect(@NonNull RectF rect, float scale) {
        float w = rect.width();
        float h = rect.height();
        float newW = scale * w;
        float newH = scale * h;
        float dx = (newW - w) / 2;
        float dy = (newH - h) / 2;
        rect.left -= dx;
        rect.top -= dy;
        rect.right += dx;
        rect.bottom += dy;
    }

    public static void rotateRect(@NonNull RectF rect, float centerX, float centerY, float rotate) {
        float x = rect.centerX();
        float y = rect.centerY();
        float sinA = (float) Math.sin(Math.toRadians(rotate));
        float cosA = (float) Math.cos(Math.toRadians(rotate));
        float newX = centerX + (x - centerX) * cosA - (y - centerY) * sinA;
        float newY = centerY + (y - centerY) * cosA + (x - centerX) * sinA;
        float dx = newX - x;
        float dy = newY - y;
        rect.offset(dx, dy);
    }

    public static void rectAddV(@NonNull Rect srcRect, @NonNull Rect addRect, int padding) {
        int left = srcRect.left;
        int top = srcRect.top;
        int right = srcRect.right;
        int bottom = srcRect.bottom;
        if (srcRect.width() <= addRect.width()) {
            right = left + addRect.width();
        }
        bottom += padding + Math.max(addRect.height(), 60);
        srcRect.set(left, top, right, bottom);
    }

    public static void refreshRotateAndScale(@NonNull EditImageText editImageText, @NonNull RectF mMoveBoxRect, @NonNull RectF textRotateDstRect, float dx, float dy) {
        float c_x = mMoveBoxRect.centerX();
        float c_y = mMoveBoxRect.centerY();
        float x = textRotateDstRect.centerX();
        float y = textRotateDstRect.centerY();
        float n_x = x + dx;
        float n_y = y + dy;
        float xa = x - c_x;
        float ya = y - c_y;
        float xb = n_x - c_x;
        float yb = n_y - c_y;
        float srcLen = (float) Math.sqrt(xa * xa + ya * ya);
        float curLen = (float) Math.sqrt(xb * xb + yb * yb);
        float scale = curLen / srcLen;
        editImageText.scale *= scale;
        float newWidth = mMoveBoxRect.width() * editImageText.scale;
        if (newWidth < 70) {
            editImageText.scale /= scale;
            return;
        }
        float cos = (xa * xb + ya * yb) / (srcLen * curLen);
        if (cos > 1 || cos < -1)
            return;
        float angle = (float) Math.toDegrees(Math.acos(cos));
        float calMatrix = xa * yb - xb * ya;
        float flag = calMatrix > 0 ? 1 : -1;
        angle = flag * angle;
        editImageText.rotate += angle;
    }

    public static void refreshMatrix(@NonNull Canvas canvas, @NonNull Matrix matrix, @NonNull RefreshMatrixCallBack callBack) {
        float[] data = new float[9];
        matrix.getValues(data);
        Matrix3 cal = new Matrix3(data);
        Matrix3 inverseMatrix = cal.inverseMatrix();
        Matrix m = new Matrix();
        m.setValues(inverseMatrix.getValues());
        float[] f = new float[9];
        m.getValues(f);
        int dx = (int) f[Matrix.MTRANS_X];
        int dy = (int) f[Matrix.MTRANS_Y];
        float scale_x = f[Matrix.MSCALE_X];
        float scale_y = f[Matrix.MSCALE_Y];
        canvas.save();
        canvas.translate(dx, dy);
        canvas.scale(scale_x, scale_y);
        callBack.callback(dx, dy, scale_x, scale_y);
        canvas.restore();
    }

    public interface RefreshMatrixCallBack {
        void callback(int dx, int dy, float scaleX, float scaleY);
    }
}
