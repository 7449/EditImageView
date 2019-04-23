package com.edit.image.sample.aty

import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.edit.image.sample.NewBitmapDialog
import com.edit.image.sample.R
import com.image.edit.*
import kotlinx.android.synthetic.main.activity_edit.*


/**
 * @author y
 * @create 2019-04-23
 */
abstract class Base : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)
        view_edit.setDebug(BuildConfig.DEBUG)
        view_edit.paint().setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_START)
        view_edit.setOnImageEventListener(object : SubsamplingScaleImageView.OnImageEventListener {
            override fun onImageLoaded() {
            }

            override fun onReady() {
                progress.visibility = View.GONE
            }

            override fun onTileLoadError(p0: Exception?) {
            }

            override fun onPreviewReleased() {
            }

            override fun onImageLoadError(p0: Exception?) {
            }

            override fun onPreviewLoadError(p0: Exception?) {
            }
        })

        btn_cancel.setOnClickListener { view_edit.lastImage() }
        btn_clear.setOnClickListener { view_edit.clearImage() }
        btn_quite.setOnClickListener { view_edit.noneAction() }

        btn_eraser.setOnClickListener { view_edit.eraserAction() }
        btn_text.setOnClickListener {
            if (view_edit.hasTextAction()) {
                view_edit.saveText()
            } else {
                view_edit.textAction(EditImageView::class.java.simpleName + "\n" + EditImageView::class.java.simpleName)
            }
        }
        btn_save.setOnClickListener {
            AlertDialog.Builder(this).setSingleChoiceItems(arrayOf("只显示绘制痕迹", "新Bitmap"), View.NO_ID) { dialog, which ->
                when (which) {
                    0 -> NewBitmapDialog.new(view_edit.newBitmap, supportFragmentManager)
                    1 -> NewBitmapDialog.new(view_edit.newCanvasBitmap(), supportFragmentManager)
                }
                dialog.dismiss()
            }.show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.edit, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.edit_paint_color_blue -> view_edit.editImageConfig = view_edit.editImageConfig.apply { pointColor = Color.BLUE }
            R.id.edit_paint_color_red -> view_edit.editImageConfig = view_edit.editImageConfig.apply { pointColor = Color.RED }
            R.id.edit_paint_color_black -> view_edit.editImageConfig = view_edit.editImageConfig.apply { pointColor = Color.BLACK }

            R.id.edit_paint_default -> view_edit.pointAction()
            R.id.edit_paint_line -> view_edit.lineAction()
            R.id.edit_paint_rect -> view_edit.rectAction()
            R.id.edit_paint_circle -> view_edit.circleAction()
        }
        return super.onOptionsItemSelected(item)
    }

}