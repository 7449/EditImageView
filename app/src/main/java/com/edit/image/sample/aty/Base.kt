package com.edit.image.sample.aty

import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.edit.image.sample.NewBitmapDialog
import com.edit.image.sample.R
import com.image.edit.*
import com.image.edit.BuildConfig
import com.image.edit.circle.circleAction
import com.image.edit.circle.getCircleAction
import com.image.edit.circle.setPointColor
import com.image.edit.circle.setPointWidth
import com.image.edit.eraser.eraserAction
import com.image.edit.line.LineAction
import com.image.edit.line.lineAction
import com.image.edit.line.setPointColor
import com.image.edit.line.setPointWidth
import com.image.edit.point.PointAction
import com.image.edit.point.pointAction
import com.image.edit.point.setPointColor
import com.image.edit.point.setPointWidth
import com.image.edit.react.RectAction
import com.image.edit.react.rectAction
import com.image.edit.react.setPointColor
import com.image.edit.react.setPointWidth
import com.image.edit.text.*
import kotlinx.android.synthetic.main.activity_edit.*

/**
 * @author y
 * @create 2019-04-23
 */
abstract class Base : AppCompatActivity() {

    val lineAction: LineAction = LineAction()
    val pointAction: PointAction = PointAction()
    val rectAction: RectAction = RectAction()
    val textAction: TextAction = TextAction()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)
        viewEdit.setDebug(BuildConfig.DEBUG)
        viewEdit.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_START)
        viewEdit.setOnImageEventListener(object : SubsamplingScaleImageView.DefaultOnImageEventListener() {
            override fun onReady() {
                progress.visibility = View.GONE
            }
        })

        btnCancel.setOnClickListener { viewEdit.lastImage() }
        btnClear.setOnClickListener { viewEdit.clearImage() }
        btnQuite.setOnClickListener { viewEdit.noneAction() }
        btnEraser.setOnClickListener { viewEdit.eraserAction() }

        btnText.setOnClickListener {
            if (viewEdit.hasTextAction()) {
                viewEdit.saveText()
            } else {
                viewEdit.textAction(EditImageView::class.java.simpleName + "\n" + EditImageView::class.java.simpleName, textAction)
            }
        }

        btnSave.setOnClickListener {
            AlertDialog.Builder(this).setSingleChoiceItems(arrayOf("只显示绘制痕迹", "新Bitmap"), View.NO_ID) { dialog, which ->
                when (which) {
                    0 -> NewBitmapDialog.new(viewEdit.newBitmap(), supportFragmentManager)
                    1 -> NewBitmapDialog.new(viewEdit.newCanvasBitmap(), supportFragmentManager)
                }
                dialog.dismiss()
            }.show()
        }

        paintSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                viewEdit.getCircleAction()?.setPointWidth(progress.toFloat())
                lineAction.setPointWidth(progress.toFloat())
                pointAction.setPointWidth(progress.toFloat())
                rectAction.setPointWidth(progress.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })
        textSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                textAction.setPaintSize(progress.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.edit, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.edit_paint_color_blue -> {
                lineAction.setPointColor(Color.BLUE)
                pointAction.setPointColor(Color.BLUE)
                rectAction.setPointColor(Color.BLUE)
                viewEdit.getCircleAction()?.setPointColor(Color.BLUE)
            }
            R.id.edit_paint_color_red -> {
                lineAction.setPointColor(Color.RED)
                pointAction.setPointColor(Color.RED)
                rectAction.setPointColor(Color.RED)
                viewEdit.getCircleAction()?.setPointColor(Color.RED)
            }
            R.id.edit_paint_color_black -> {
                lineAction.setPointColor(Color.BLACK)
                pointAction.setPointColor(Color.BLACK)
                rectAction.setPointColor(Color.BLACK)
                viewEdit.getCircleAction()?.setPointColor(Color.BLACK)
            }
            R.id.edit_paint_default -> viewEdit.pointAction(pointAction)
            R.id.edit_paint_line -> viewEdit.lineAction(lineAction)
            R.id.edit_paint_rect -> viewEdit.rectAction(rectAction)
            R.id.edit_paint_circle -> viewEdit.circleAction()
            R.id.edit_text_color_blue -> textAction.setPaintColor(Color.BLUE)
            R.id.edit_text_color_red -> textAction.setPaintColor(Color.RED)
        }
        return super.onOptionsItemSelected(item)
    }

}