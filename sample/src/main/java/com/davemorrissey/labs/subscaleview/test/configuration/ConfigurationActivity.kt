package com.davemorrissey.labs.subscaleview.test.configuration

import android.graphics.PointF
import android.os.Bundle
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.ViewValues
import com.davemorrissey.labs.subscaleview.temp.*
import com.davemorrissey.labs.subscaleview.test.AbstractPagesActivity
import com.davemorrissey.labs.subscaleview.test.Page
import com.davemorrissey.labs.subscaleview.test.R
import kotlinx.android.synthetic.main.pages_activity.*

class ConfigurationActivity : AbstractPagesActivity(R.string.configuration_title, R.layout.pages_activity, listOf(
        Page(R.string.configuration_p1_subtitle, R.string.configuration_p1_text),
        Page(R.string.configuration_p2_subtitle, R.string.configuration_p2_text),
        Page(R.string.configuration_p3_subtitle, R.string.configuration_p3_text),
        Page(R.string.configuration_p4_subtitle, R.string.configuration_p4_text),
        Page(R.string.configuration_p5_subtitle, R.string.configuration_p5_text),
        Page(R.string.configuration_p6_subtitle, R.string.configuration_p6_text),
        Page(R.string.configuration_p7_subtitle, R.string.configuration_p7_text),
        Page(R.string.configuration_p8_subtitle, R.string.configuration_p8_text),
        Page(R.string.configuration_p9_subtitle, R.string.configuration_p9_text),
        Page(R.string.configuration_p10_subtitle, R.string.configuration_p10_text)
)) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageView.setImage(ImageSource.asset("card.png"))
    }

    override fun onPageChanged(page: Int) {
        if (page == 0) {
            imageView.setMinimumDpi(50)
        } else {
            imageView.setMaxScale(2f)
        }
        if (page == 1) {
            imageView.setMinimumTileDpi(50)
        } else {
            imageView.setMinimumTileDpi(320)
        }
        when (page) {
            4 -> imageView.setDoubleTapZoomStyle(ViewValues.ZOOM_FOCUS_CENTER)
            5 -> imageView.setDoubleTapZoomStyle(ViewValues.ZOOM_FOCUS_CENTER_IMMEDIATE)
            else -> imageView.setDoubleTapZoomStyle(ViewValues.ZOOM_FOCUS_FIXED)
        }
        if (page == 6) {
            imageView.setDoubleTapZoomDpi(240)
        } else {
            imageView.setDoubleTapZoomScale(1f)
        }
        when (page) {
            7 -> imageView.setPanLimit(ViewValues.PAN_LIMIT_CENTER)
            8 -> imageView.setPanLimit(ViewValues.PAN_LIMIT_OUTSIDE)
            else -> imageView.setPanLimit(ViewValues.PAN_LIMIT_INSIDE)
        }
        if (page == 9) {
            imageView.setDebug(true)
        } else {
            imageView.setDebug(false)
        }
        if (page == 2) {
            imageView.setScaleAndCenter(0f, PointF(3900f, 3120f))
            imageView.setPanEnabled(false)
        } else {
            imageView.setPanEnabled(true)
        }
        if (page == 3) {
            imageView.setScaleAndCenter(1f, PointF(3900f, 3120f))
            imageView.setZoomEnabled(false)
        } else {
            imageView.setZoomEnabled(true)
        }
    }

}
