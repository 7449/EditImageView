package com.edit.image.sample;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.image.edit.EditImageConfig;
import com.image.edit.EditImageView;
import com.image.edit.EditType;
import com.image.edit.simple.SimpleOnEditImageCircleActionListener;
import com.image.edit.simple.SimpleOnEditImageInitializeListener;
import com.image.edit.simple.SimpleOnEditImageLineActionListener;
import com.image.edit.simple.SimpleOnEditImagePointActionListener;
import com.image.edit.simple.SimpleOnEditImageRectActionListener;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditImageView editImageView;
    private SubsamplingScaleImageView newImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editImageView = findViewById(R.id.edit_image);
        newImageView = findViewById(R.id.edit_new_image);
        editImageView
                .setOnEditImageListener(new SimpleListener())
                .setOnEditImageInitializeListener(new SimpleOnEditImageInitializeListener())
                .setEditImageConfig(new EditImageConfig());
        findViewById(R.id.btn_display).setOnClickListener(this);
        findViewById(R.id.btn_save).setOnClickListener(this);
        findViewById(R.id.btn_cancel).setOnClickListener(this);
        findViewById(R.id.btn_clear).setOnClickListener(this);
        findViewById(R.id.btn_text).setOnClickListener(this);
        findViewById(R.id.btn_eraser).setOnClickListener(this);
        findViewById(R.id.btn_paint).setOnClickListener(this);
        findViewById(R.id.btn_quite).setOnClickListener(this);

        findViewById(R.id.btn_paint).setOnLongClickListener(v -> {
            new AlertDialog.Builder(MainActivity.this)
                    .setSingleChoiceItems(new String[]{"蓝色", "红色", "黑色"}, 0,
                            (dialog, which) -> {
                                switch (which) {
                                    case 0:
                                        editImageView.getPointPaint().setColor(Color.BLUE);
                                        break;
                                    case 1:
                                        editImageView.getPointPaint().setColor(Color.RED);
                                        break;
                                    case 2:
                                        editImageView.getPointPaint().setColor(Color.BLACK);
                                        break;
                                }
                                dialog.dismiss();
                            }).create().show();
            return true;
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_display:
                Glide.with(this).asBitmap().load(R.drawable.icon).into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        editImageView.setImage(ImageSource.cachedBitmap(resource));
                    }
                });
                break;
            case R.id.btn_save:
                newImageView.setImage(ImageSource.cachedBitmap(editImageView.getDrawBitmap()));
                break;
            case R.id.btn_cancel:
                editImageView.lastImage();
                break;
            case R.id.btn_clear:
                editImageView.clearImage();
                break;
            case R.id.btn_paint:
                new AlertDialog.Builder(this).setSingleChoiceItems(new String[]{"普通", "直线", "矩形", "圆形"}, 0, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            editImageView.setOnEditImagePointActionListener(new SimpleOnEditImagePointActionListener());
                            break;
                        case 1:
                            editImageView.setOnEditImagePointActionListener(new SimpleOnEditImageLineActionListener());
                            break;
                        case 2:
                            editImageView.setOnEditImagePointActionListener(new SimpleOnEditImageRectActionListener());
                            break;
                        case 3:
                            editImageView.setOnEditImagePointActionListener(new SimpleOnEditImageCircleActionListener());
                            break;
                    }
                    editImageView.setEditType(EditType.PAINT);
                    dialog.dismiss();
                }).create().show();
                break;
            case R.id.btn_eraser:
                editImageView.setEditType(EditType.ERASER);
                break;
            case R.id.btn_text:
                if (editImageView.getEditType() == EditType.TEXT) {
                    editImageView.saveText();
                } else {
                    editImageView.setText(EditImageView.class.getSimpleName()).setEditType(EditType.TEXT);
                }
                break;
            case R.id.btn_quite:
                editImageView.setEditType(EditType.NONE);
                break;
        }
    }
}
