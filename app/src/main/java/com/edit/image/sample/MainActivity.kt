package com.edit.image.sample

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.image.edit.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var editImageView: EditImageView
    private lateinit var newImageView: SubsamplingScaleImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        editImageView = findViewById(R.id.edit_image)
        newImageView = findViewById(R.id.edit_new_image)
        editImageView.paint().setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_START)

        findViewById<View>(R.id.btn_display).setOnClickListener(this)
        findViewById<View>(R.id.btn_save).setOnClickListener(this)
        findViewById<View>(R.id.btn_cancel).setOnClickListener(this)
        findViewById<View>(R.id.btn_clear).setOnClickListener(this)
        findViewById<View>(R.id.btn_text).setOnClickListener(this)
        findViewById<View>(R.id.btn_eraser).setOnClickListener(this)
        findViewById<View>(R.id.btn_paint).setOnClickListener(this)
        findViewById<View>(R.id.btn_quite).setOnClickListener(this)

        findViewById<View>(R.id.btn_paint).setOnLongClickListener {
            AlertDialog.Builder(this@MainActivity).setSingleChoiceItems(arrayOf("蓝色", "红色", "黑色"), 0
            ) { dialog, which ->
                when (which) {
                    0 -> editImageView.editImageConfig = editImageView.editImageConfig.apply { pointColor = Color.BLUE }
                    1 -> editImageView.editImageConfig = editImageView.editImageConfig.apply { pointColor = Color.RED }
                    2 -> editImageView.editImageConfig = editImageView.editImageConfig.apply { pointColor = Color.BLACK }
                }
                dialog.dismiss()
            }.show()
            true
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_display -> Glide.with(this).asBitmap().load(R.drawable.icon).into(object : SimpleTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    editImageView.setImage(ImageSource.cachedBitmap(resource))
                }
            })
            R.id.btn_save -> newImageView.setImage(ImageSource.cachedBitmap(editImageView.newBitmap))
            R.id.btn_cancel -> editImageView.lastImage()
            R.id.btn_clear -> editImageView.clearImage()
            R.id.btn_paint -> AlertDialog.Builder(this).setSingleChoiceItems(arrayOf("普通", "直线", "矩形", "圆形"), 0) { dialog, which ->
                when (which) {
                    0 -> editImageView.pointAction()
                    1 -> editImageView.lineAction()
                    2 -> editImageView.rectAction()
                    3 -> editImageView.circleAction()
                }
                dialog.dismiss()
            }.show()
            R.id.btn_eraser -> editImageView.eraserAction()
            R.id.btn_text -> if (editImageView.hasTextAction()) {
                editImageView.saveText()
            } else {
                editImageView.textAction(EditImageView::class.java.simpleName + "\n" + EditImageView::class.java.simpleName)
            }
            R.id.btn_quite -> editImageView.noneAction()
        }
    }
}
