package com.edit.image.sample

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.image.edit.EditImageView
import com.image.edit.EditType
import com.image.edit.simple.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var editImageView: EditImageView
    private lateinit var newImageView: SubsamplingScaleImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val viewPager = findViewById<ViewPager>(R.id.edit_image_view_pager)
        newImageView = findViewById(R.id.edit_new_image)

        viewPager.adapter = object : PagerAdapter() {
            override fun getCount(): Int {
                return 3
            }

            override fun isViewFromObject(view: View, o: Any): Boolean {
                return view === o
            }

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                val editImageView = EditImageView(container.context)
                editImageView
                        .apply {
                            onEditImageCustomActionListener = SimpleOnEditImageCustomActionListener()
                            setOnEditImageInitializeListener(SimpleOnEditImageInitializeListener())
                        }
                        .setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_START)
                container.addView(editImageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                return editImageView
            }

            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                container.removeView(`object` as EditImageView)
            }

            override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
                super.setPrimaryItem(container, position, `object`)
                editImageView = `object` as EditImageView
            }
        }


        findViewById<View>(R.id.btn_display).setOnClickListener(this)
        findViewById<View>(R.id.btn_save).setOnClickListener(this)
        findViewById<View>(R.id.btn_cancel).setOnClickListener(this)
        findViewById<View>(R.id.btn_clear).setOnClickListener(this)
        findViewById<View>(R.id.btn_text).setOnClickListener(this)
        findViewById<View>(R.id.btn_eraser).setOnClickListener(this)
        findViewById<View>(R.id.btn_paint).setOnClickListener(this)
        findViewById<View>(R.id.btn_quite).setOnClickListener(this)
        findViewById<View>(R.id.btn_custom).setOnClickListener(this)

        findViewById<View>(R.id.btn_paint).setOnLongClickListener {
            AlertDialog.Builder(this@MainActivity)
                    .setSingleChoiceItems(arrayOf("蓝色", "红色", "黑色"), 0
                    ) { dialog, which ->
                        when (which) {
                            0 -> editImageView.pointPaint.color = Color.BLUE
                            1 -> editImageView.pointPaint.color = Color.RED
                            2 -> editImageView.pointPaint.color = Color.BLACK
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
                    0 -> editImageView.onEditImagePointActionListener = SimpleOnEditImagePointActionListener()
                    1 -> editImageView.onEditImagePointActionListener = SimpleOnEditImageLineActionListener()
                    2 -> editImageView.onEditImagePointActionListener = SimpleOnEditImageRectActionListener()
                    3 -> editImageView.onEditImagePointActionListener = SimpleOnEditImageCircleActionListener()
                }
                editImageView.editType = EditType.PAINT
                dialog.dismiss()
            }.show()
            R.id.btn_eraser -> editImageView.editType = EditType.ERASER
            R.id.btn_text -> if (editImageView.editType == EditType.TEXT) {
                editImageView.saveText()
            } else {
                editImageView.apply {
                    setText(EditImageView::class.java.simpleName)
                    editType = EditType.TEXT
                }
            }
            R.id.btn_quite -> editImageView.editType = EditType.NONE
            R.id.btn_custom -> editImageView.editType = EditType.CUSTOM
        }
    }
}
