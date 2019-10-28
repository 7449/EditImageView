package com.davemorrissey.labs.subscaleview.test.configuration

import android.graphics.PointF
import android.os.Bundle
import com.davemorrissey.labs.subscaleview.*
import com.davemorrissey.labs.subscaleview.temp.*
import com.davemorrissey.labs.subscaleview.test.AbstractPagesActivity
import com.davemorrissey.labs.subscaleview.test.Page
import com.davemorrissey.labs.subscaleview.test.R.id
import com.davemorrissey.labs.subscaleview.test.R.layout.pages_activity
import com.davemorrissey.labs.subscaleview.test.R.string.*

class ConfigurationActivity : AbstractPagesActivity(configuration_title, pages_activity, listOf(
        Page(configuration_p1_subtitle, configuration_p1_text),
        Page(configuration_p2_subtitle, configuration_p2_text),
        Page(configuration_p3_subtitle, configuration_p3_text),
        Page(configuration_p4_subtitle, configuration_p4_text),
        Page(configuration_p5_subtitle, configuration_p5_text),
        Page(configuration_p6_subtitle, configuration_p6_text),
        Page(configuration_p7_subtitle, configuration_p7_text),
        Page(configuration_p8_subtitle, configuration_p8_text),
        Page(configuration_p9_subtitle, configuration_p9_text),
        Page(configuration_p10_subtitle, configuration_p10_text)
)) {

    private lateinit var view: SubsamplingScaleImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view = findViewById(id.imageView)
        view.setImage(ImageSource.asset("card.png"))
    }

    override fun onPageChanged(page: Int) {
        if (page == 0) {
            view.setMinimumDpi(50)
        } else {
            view.setMaxScale(2f)
        }
        if (page == 1) {
            view.setMinimumTileDpi(50)
        } else {
            view.setMinimumTileDpi(320)
        }
        when (page) {
            4 -> view.setDoubleTapZoomStyle(ViewValues.ZOOM_FOCUS_CENTER)
            5 -> view.setDoubleTapZoomStyle(ViewValues.ZOOM_FOCUS_CENTER_IMMEDIATE)
            else -> view.setDoubleTapZoomStyle(ViewValues.ZOOM_FOCUS_FIXED)
        }
        if (page == 6) {
            view.setDoubleTapZoomDpi(240)
        } else {
            view.setDoubleTapZoomScale(1f)
        }
        when (page) {
            7 -> view.setPanLimit(ViewValues.PAN_LIMIT_CENTER)
            8 -> view.setPanLimit(ViewValues.PAN_LIMIT_OUTSIDE)
            else -> view.setPanLimit(ViewValues.PAN_LIMIT_INSIDE)
        }
        if (page == 9) {
            view.setDebug(true)
        } else {
            view.setDebug(false)
        }
        if (page == 2) {
            view.setScaleAndCenter(0f, PointF(3900f, 3120f))
            view.setPanEnabled(false)
        } else {
            view.setPanEnabled(true)
        }
        if (page == 3) {
            view.setScaleAndCenter(1f, PointF(3900f, 3120f))
            view.setZoomEnabled(false)
        } else {
            view.setZoomEnabled(true)
        }
    }

}
