package com.image.edit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.image.edit.action.OnEditImageEraserActionListener;
import com.image.edit.action.OnEditImagePointActionListener;
import com.image.edit.action.OnEditImageTextActionListener;
import com.image.edit.cache.EditImageCache;
import com.image.edit.cache.EditImageText;
import com.image.edit.helper.BitmapHelper;
import com.image.edit.helper.EditTextType;
import com.image.edit.simple.SimpleOnEditImageEraserActionListener;
import com.image.edit.simple.SimpleOnEditImageListener;
import com.image.edit.simple.SimpleOnEditImagePointActionListener;
import com.image.edit.simple.SimpleOnEditImageTextActionListener;

import java.util.LinkedList;

/**
 * @author y
 * @create 2018/11/17
 */
@SuppressWarnings({"unused", "FieldCanBeLocal", "ClickableViewAccessibility", "ConstantConditions", "UnusedReturnValue"})
public class EditImageView extends SubsamplingScaleImageView {
    private static final String TAG = EditImageView.class.getSimpleName();

    private LinkedList<EditImageCache> cacheArrayList = new LinkedList<>();
    private EditType editType = EditType.NONE;
    private EditTextType editTextType = EditTextType.NONE;

    @NonNull
    private OnEditImageListener onEditImageListener = new SimpleOnEditImageListener();
    private OnEditImagePointActionListener onEditImagePointActionListener = new SimpleOnEditImagePointActionListener();
    private OnEditImageEraserActionListener onEditImageEraserActionListener = new SimpleOnEditImageEraserActionListener();
    private OnEditImageTextActionListener onEditImageTextActionListener = new SimpleOnEditImageTextActionListener();

    /**
     * 全局配置
     */
    private EditImageConfig editImageConfig;

    private EditImageText editImageText;

    /**
     * 线条
     */
    private Paint paint;
    private Paint eraserPaint;

    /**
     * 文字
     */
    private TextPaint textPaint;
    private Paint framePaint;
    private Bitmap textDeleteBitmap;
    private Bitmap textRotateBitmap;

    /**
     * 临时bitmap
     */
    private Bitmap newBitmap;
    private Canvas newBitmapCanvas;

    public EditImageView(Context context, AttributeSet attr) {
        super(context, attr);
    }

    public EditImageView(Context context) {
        super(context);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (editType.equals(EditType.NONE) || !isReady()) {
            return super.onTouchEvent(event);
        }
        getParent().requestDisallowInterceptTouchEvent(!editType.equals(EditType.TEXT));
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                eventDown(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                eventMove(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_UP:
                eventUp(event.getX(), event.getY());
                break;
        }
        if (!editType.equals(EditType.TEXT)) return true;
        return !editTextType.equals(EditTextType.NONE) || super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isReady() || getSupperMatrix() == null) {
            return;
        }
        if (editImageConfig == null) {
            throw new NullPointerException("setEditImageConfig");
        }
        switch (editType) {
            case PAINT:
                onEditImagePointActionListener.onDraw(this, canvas);
                break;
            case TEXT:
                onEditImageTextActionListener.onDraw(this, canvas);
                break;
            case ERASER:
                onEditImageEraserActionListener.onDraw(this, canvas);
                break;
        }
        if (newBitmap != null) {
            canvas.drawBitmap(newBitmap, getSupperMatrix(), null);
        }
    }

    private void eventDown(float x, float y) {
        switch (editType) {
            case PAINT:
                onEditImagePointActionListener.onDown(this, x, y);
                break;
            case ERASER:
                onEditImageEraserActionListener.onDown(this, x, y);
                break;
            case TEXT:
                onEditImageTextActionListener.onDown(this, x, y);
                break;
        }
    }

    private void eventMove(float x, float y) {
        switch (editType) {
            case PAINT:
                onEditImagePointActionListener.onMove(this, x, y);
                break;
            case ERASER:
                onEditImageEraserActionListener.onMove(this, x, y);
                break;
            case TEXT:
                onEditImageTextActionListener.onMove(this, x, y);
                break;
        }
    }

    private void eventUp(float x, float y) {
        switch (editType) {
            case PAINT:
                onEditImagePointActionListener.onUp(this, x, y);
                break;
            case ERASER:
                onEditImageEraserActionListener.onUp(this, x, y);
                break;
            case TEXT:
                onEditImageTextActionListener.onUp(this, x, y);
                break;
        }
    }

    /**
     * 注册回调
     *
     * @param onEditImageListener 所有触发事件回调
     * @return this
     */
    public EditImageView setOnEditImageListener(@NonNull OnEditImageListener onEditImageListener) {
        this.onEditImageListener = onEditImageListener;
        return this;
    }

    /**
     * 初始化画笔和配置
     *
     * @param onEditImageInitializeListener 注册事件
     * @return this
     */
    public EditImageView setOnEditImageInitializeListener(@NonNull OnEditImageInitializeListener onEditImageInitializeListener) {
        this.paint = onEditImageInitializeListener.initPointPaint(this);
        this.eraserPaint = onEditImageInitializeListener.initEraserPaint(this);
        this.textPaint = onEditImageInitializeListener.initTextPaint(this);
        this.framePaint = onEditImageInitializeListener.initTextFramePaint(this);
        return this;
    }

    /**
     * 自定义画笔
     *
     * @param onEditImagePointActionListener {@link SimpleOnEditImagePointActionListener}
     * @return this
     */
    public EditImageView setOnEditImagePointActionListener(@NonNull OnEditImagePointActionListener onEditImagePointActionListener) {
        this.onEditImagePointActionListener = onEditImagePointActionListener;
        return this;
    }

    /**
     * 自定义橡皮擦
     *
     * @param onEditImageEraserActionListener {@link SimpleOnEditImageEraserActionListener}
     * @return this
     */
    public EditImageView setOnEditImageEraserActionListener(@NonNull OnEditImageEraserActionListener onEditImageEraserActionListener) {
        this.onEditImageEraserActionListener = onEditImageEraserActionListener;
        return this;
    }

    /**
     * 自定义文字绘制
     *
     * @param onEditImageTextActionListener {@link SimpleOnEditImageTextActionListener}
     * @return this
     */
    public EditImageView setOnEditImageTextActionListener(@NonNull OnEditImageTextActionListener onEditImageTextActionListener) {
        this.onEditImageTextActionListener = onEditImageTextActionListener;
        return this;
    }

    /**
     * 设置各种画笔模式,只设置一次
     *
     * @param editImageConfig {@link EditImageConfig}
     */
    public EditImageView setEditImageConfig(@NonNull EditImageConfig editImageConfig) {
        if (this.editImageConfig != null) {
            throw new RuntimeException();
        }
        this.editImageConfig = editImageConfig;
        refreshConfig();
        return this;
    }

    /**
     * 设置{@link EditType}
     * <p>
     * 该方法会强制刷新页面.如果设置之前是{@link EditType#TEXT}则会放弃当前绘制的文字,
     * 如果需要请判断{@link #getEditType()}模式然后{@link #saveText()}
     *
     * @param editType {@link EditType}
     */
    public EditImageView setEditType(@NonNull EditType editType) {
        this.editType = editType;
        refresh();
        return this;
    }

    /**
     * 刷新页面
     */
    public EditImageView refresh() {
        invalidate();
        return this;
    }

    /**
     * 设置Text
     *
     * @param text {@link String}
     * @return this
     */
    public EditImageView setText(@NonNull String text) {
        editImageText = new EditImageText(new PointF(getMeasuredWidth() / 2, getMeasuredWidth() / 2), 1, 0, text, textPaint.getColor(), textPaint.getTextSize());
        return this;
    }

    /**
     * 设置Text
     *
     * @param editImageText {@link EditImageText}
     * @return this
     */
    public EditImageView setText(@NonNull EditImageText editImageText) {
        this.editImageText = editImageText;
        return this;
    }

    /**
     * 保存当前需要绘制的Text
     */
    public EditImageView saveText() {
        if (editImageText == null || !editType.equals(EditType.TEXT) || getSupperMatrix() == null) {
            return this;
        }
        onEditImageTextActionListener.onSaveText(this);
        return this;
    }

    /**
     * 获取绘制的bitmap
     * <p>
     * 这个方法只会返回当前的绘制Bitmap,相当于{@link #setImage(ImageSource)}同等大小的一个只有绘制痕迹的Bitmap
     *
     * @return {@link Bitmap}
     */
    @NonNull
    public Bitmap getDrawBitmap() {
        return newBitmap;
    }

    /**
     * 回收{@link #newBitmap}
     */
    public void recycleDrawBitmap() {
        BitmapHelper.recycle(newBitmap);
    }

    /**
     * 返回绘制之后的Bitmap
     *
     * @return {@link Bitmap}
     */
    @NonNull
    public Bitmap getNewBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(getSWidth(), getSHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        if (getBitmap() != null)
            canvas.drawBitmap(getBitmap(), 0, 0, null);
        if (newBitmap != null)
            canvas.drawBitmap(newBitmap, 0, 0, null);
        canvas.save();
        return bitmap;
    }

    /**
     * 获取编辑状态
     *
     * @return {@link EditType}
     */
    public EditType getEditType() {
        return editType;
    }

    /**
     * 恢复初始状态
     */
    public void clearImage() {
        if (cacheArrayList.isEmpty()) {
            onEditImageListener.onLastImageEmpty();
            return;
        }
        cacheArrayList.clear();
        reset();
        setEditType(EditType.NONE);
    }

    /**
     * 回退上一步
     */
    public void lastImage() {
        if (cacheArrayList.isEmpty()) {
            onEditImageListener.onLastImageEmpty();
            return;
        }
        cacheArrayList.removeLast();
        reset();
        for (EditImageCache editImageCache : cacheArrayList) {
            switch (editImageCache.editType) {
                case TEXT:
                    onEditImageTextActionListener.onLastImage(this, editImageCache);
                    break;
                case PAINT:
                    onEditImagePointActionListener.onLastImage(this, editImageCache);
                    break;
                case ERASER:
                    onEditImageEraserActionListener.onLastImage(this, editImageCache);
                    break;
            }
        }
        setEditType(EditType.NONE);
    }

    @NonNull
    public Canvas getNewBitmapCanvas() {
        return newBitmapCanvas;
    }

    @NonNull
    public Paint getPointPaint() {
        return paint;
    }

    @NonNull
    public Paint getEraserPaint() {
        return eraserPaint;
    }

    @NonNull
    public TextPaint getTextPaint() {
        return textPaint;
    }

    @NonNull
    public Paint getFramePaint() {
        return framePaint;
    }

    @NonNull
    public LinkedList<EditImageCache> getCacheArrayList() {
        return cacheArrayList;
    }

    @NonNull
    public EditImageText getEditImageText() {
        return editImageText;
    }

    @NonNull
    public Bitmap getTextDeleteBitmap() {
        return textDeleteBitmap;
    }

    @NonNull
    public Bitmap getTextRotateBitmap() {
        return textRotateBitmap;
    }

    @NonNull
    public EditImageConfig getEditImageConfig() {
        return editImageConfig;
    }

    @NonNull
    public OnEditImageListener getOnEditImageListener() {
        return onEditImageListener;
    }

    @NonNull
    public EditTextType getEditTextType() {
        return editTextType;
    }

    public void setEditTextType(@NonNull EditTextType editTextType) {
        this.editTextType = editTextType;
    }

    @Override
    protected void onReady() {
        reset();
    }

    private void reset() {
        BitmapHelper.recycle(newBitmap);
        newBitmap = Bitmap.createBitmap(getSWidth(), getSHeight(), Bitmap.Config.ARGB_8888);
        newBitmapCanvas = new Canvas(newBitmap);
    }

    private void refreshConfig() {
        paint.setColor(editImageConfig.pointColor);
        paint.setStrokeWidth(editImageConfig.pointWidth);
        eraserPaint.setStrokeWidth(editImageConfig.eraserPointWidth);
        textPaint.setTextAlign(editImageConfig.textPaintAlign);
        textPaint.setTextSize(editImageConfig.textPaintSize);
        textPaint.setColor(editImageConfig.textPaintColor);
        framePaint.setStrokeWidth(editImageConfig.textFramePaintWidth);
        framePaint.setColor(editImageConfig.textFramePaintColor);
        textDeleteBitmap = BitmapFactory.decodeResource(getResources(), editImageConfig.textDeleteDrawableId);
        textRotateBitmap = BitmapFactory.decodeResource(getResources(), editImageConfig.textRotateDrawableId);
        onEditImageTextActionListener.init(this);
        onEditImageEraserActionListener.init(this);
        onEditImagePointActionListener.init(this);
    }
}
