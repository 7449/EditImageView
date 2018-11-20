package com.image.edit.simple;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.text.TextPaint;
import android.text.TextUtils;

import com.image.edit.EditImageView;
import com.image.edit.EditType;
import com.image.edit.OnEditImageListener;
import com.image.edit.action.OnEditImageTextActionListener;
import com.image.edit.cache.EditImageCache;
import com.image.edit.cache.EditImageText;
import com.image.edit.helper.EditTextType;
import com.image.edit.helper.MatrixAndRectHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author y
 * @create 2018/11/20
 */
@SuppressWarnings("ConstantConditions")
public class SimpleOnEditImageTextActionListener implements OnEditImageTextActionListener {
    private static final int STICKER_BTN_HALF_SIZE = 30;
    private static final int PADDING = 30;
    private static final int TEXT_TOP_PADDING = 10;
    private static final int CHAR_MIN_HEIGHT = 60;

    private PointF textPointF = new PointF();
    private Rect textRect = new Rect();
    private Rect textTempRect = new Rect();
    private Rect textDeleteRect = new Rect();
    private Rect textRotateRect = new Rect();
    private RectF textDeleteDstRect = new RectF(0, 0, STICKER_BTN_HALF_SIZE << 1, STICKER_BTN_HALF_SIZE << 1);
    private RectF textRotateDstRect = new RectF(0, 0, STICKER_BTN_HALF_SIZE << 1, STICKER_BTN_HALF_SIZE << 1);
    private RectF mMoveBoxRect = new RectF();
    private List<String> textContents = new ArrayList<>(2);

    public SimpleOnEditImageTextActionListener() {
    }

    @Override
    public void init(@NonNull EditImageView editImageView) {
        Bitmap textDeleteBitmap = editImageView.getTextDeleteBitmap();
        Bitmap textRotateBitmap = editImageView.getTextRotateBitmap();
        textDeleteRect.set(0, 0, textDeleteBitmap.getWidth(), textDeleteBitmap.getHeight());
        textRotateRect.set(0, 0, textRotateBitmap.getWidth(), textRotateBitmap.getHeight());
    }

    @Override
    public void onDraw(@NonNull EditImageView editImageView, @NonNull Canvas canvas) {
        drawContentText(canvas, editImageView, editImageView.getEditImageText());
    }

    @Override
    public void onDown(@NonNull EditImageView editImageView, float x, float y) {
        if (textDeleteDstRect.contains(x, y)) {
            editImageView.setEditTextType(EditTextType.NONE);
        } else if (textRotateDstRect.contains(x, y)) {
            editImageView.setEditTextType(EditTextType.ROTATE);
            textPointF.set(textRotateDstRect.centerX(), textRotateDstRect.centerY());
        } else if (mMoveBoxRect.contains(x, y)) {
            editImageView.setEditTextType(EditTextType.MOVE);
            textPointF.set(x, y);
        } else {
            editImageView.setEditTextType(EditTextType.NONE);
        }
    }

    @Override
    public void onMove(@NonNull EditImageView editImageView, float x, float y) {
        EditTextType editTextType = editImageView.getEditTextType();
        EditImageText editImageText = editImageView.getEditImageText();
        if (editTextType == EditTextType.MOVE) {
            editImageText.pointF.x += x - textPointF.x;
            editImageText.pointF.y += y - textPointF.y;
        } else if (editTextType == EditTextType.ROTATE) {
            MatrixAndRectHelper.refreshRotateAndScale(editImageText, mMoveBoxRect, textRotateDstRect, x - textPointF.x, y - textPointF.y);
        }
        textPointF.set(x, y);
        editImageView.refresh();
    }

    @Override
    public void onUp(@NonNull EditImageView editImageView, float x, float y) {

    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onLastImage(@NonNull EditImageView editImageView, @NonNull EditImageCache editImageCache) {
        TextPaint textPaint = editImageView.getTextPaint();
        textPaint.setColor(editImageCache.editImageText.color);
        textPaint.setTextSize(editImageCache.editImageText.textSize);
        MatrixAndRectHelper.refreshTextMatrix(editImageView.getNewBitmapCanvas(), editImageView.getSupperMatrix());
        onDrawText(editImageView, editImageCache.editImageText, editImageView.getNewBitmapCanvas());
        editImageView.getNewBitmapCanvas().restore();
    }

    @Override
    public void onDrawText(@NonNull EditImageView editImageView, @NonNull EditImageText editImageText, @NonNull Canvas canvas) {
        textContents.clear();
        String[] splits = editImageText.text.split("\n");
        Collections.addAll(textContents, splits);
        if (textContents.isEmpty()) return;
        int textHeight = 0;
        textRect.set(0, 0, 0, 0);
        for (String textContent : textContents) {
            editImageView.getTextPaint().getTextBounds(textContent, 0, textContent.length(), textTempRect);
            textHeight = Math.max(CHAR_MIN_HEIGHT, textTempRect.height());
            MatrixAndRectHelper.rectAddV(textRect, textTempRect, TEXT_TOP_PADDING);
        }
        textRect.offset((int) editImageText.pointF.x, (int) editImageText.pointF.y - textHeight);
        mMoveBoxRect.set(textRect.left - PADDING, textRect.top - PADDING, textRect.right + PADDING, textRect.bottom + PADDING);
        MatrixAndRectHelper.scaleRect(mMoveBoxRect, editImageText.scale);
        canvas.save();
        canvas.scale(editImageText.scale, editImageText.scale, mMoveBoxRect.centerX(), mMoveBoxRect.centerY());
        canvas.rotate(editImageText.rotate, mMoveBoxRect.centerX(), mMoveBoxRect.centerY());
        int draw_text_y = (int) editImageText.pointF.y;
        for (String textContent : textContents) {
            canvas.drawText(textContent, editImageText.pointF.x, draw_text_y, editImageView.getTextPaint());
            draw_text_y += textHeight + TEXT_TOP_PADDING;
        }
        canvas.restore();
    }

    @Override
    public void onSaveText(@NonNull EditImageView editImageView) {
        LinkedList<EditImageCache> cacheArrayList = editImageView.getCacheArrayList();
        OnEditImageListener onEditImageListener = editImageView.getOnEditImageListener();
        if (cacheArrayList.size() >= editImageView.getEditImageConfig().maxCacheCount) {
            onEditImageListener.onLastCacheMax();
            return;
        }
        Canvas newBitmapCanvas = editImageView.getNewBitmapCanvas();
        Matrix supperMatrix = editImageView.getSupperMatrix();
        EditImageText editImageText = editImageView.getEditImageText();
        MatrixAndRectHelper.refreshTextMatrix(newBitmapCanvas, supperMatrix);
        onDrawText(editImageView, editImageText, newBitmapCanvas);
        newBitmapCanvas.restore();
        cacheArrayList.add(EditImageCache.createTextCache(editImageView.getState(), editImageText));
        editImageView.setEditType(EditType.NONE);
    }

    private void drawContentText(Canvas canvas, EditImageView editImageView, EditImageText editImageText) {
        if (!editImageView.getEditType().equals(EditType.TEXT)) {
            return;
        }
        if (editImageText == null) {
            return;
        }
        if (TextUtils.isEmpty(editImageText.text)) {
            return;
        }
        onDrawText(editImageView, editImageView.getEditImageText(), canvas);
        if (!editImageView.getEditImageConfig().isTextRotateMode) {
            return;
        }
        int offsetValue = ((int) textDeleteDstRect.width()) >> 1;
        textDeleteDstRect.offsetTo(mMoveBoxRect.left - offsetValue, mMoveBoxRect.top - offsetValue);
        textRotateDstRect.offsetTo(mMoveBoxRect.right - offsetValue, mMoveBoxRect.bottom - offsetValue);
        MatrixAndRectHelper.rotateRect(textDeleteDstRect, mMoveBoxRect.centerX(), mMoveBoxRect.centerY(), editImageText.rotate);
        MatrixAndRectHelper.rotateRect(textRotateDstRect, mMoveBoxRect.centerX(), mMoveBoxRect.centerY(), editImageText.rotate);
        if (!editImageView.getEditImageConfig().showTextMoveBox) {
            return;
        }
        canvas.save();
        canvas.rotate(editImageText.rotate, mMoveBoxRect.centerX(), mMoveBoxRect.centerY());
        canvas.drawRoundRect(mMoveBoxRect, 10, 10, editImageView.getFramePaint());
        canvas.restore();
        canvas.drawBitmap(editImageView.getTextDeleteBitmap(), textDeleteRect, textDeleteDstRect, null);
        canvas.drawBitmap(editImageView.getTextRotateBitmap(), textRotateRect, textRotateDstRect, null);
    }
}
