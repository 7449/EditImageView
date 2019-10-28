package com.davemorrissey.labs.subscaleview.test.animation

import android.graphics.PointF
import android.os.Bundle
import android.view.View
import com.davemorrissey.labs.subscaleview.temp.*
import com.davemorrissey.labs.subscaleview.test.AbstractPagesActivity
import com.davemorrissey.labs.subscaleview.test.Page
import com.davemorrissey.labs.subscaleview.test.R.id
import com.davemorrissey.labs.subscaleview.test.R.layout.animation_activity
import com.davemorrissey.labs.subscaleview.test.R.string.*
import com.davemorrissey.labs.subscaleview.test.extension.views.PinView
import java.util.*

class AnimationActivity : AbstractPagesActivity(animation_title, animation_activity, listOf(
        Page(animation_p1_subtitle, animation_p1_text),
        Page(animation_p2_subtitle, animation_p2_text),
        Page(animation_p3_subtitle, animation_p3_text),
        Page(animation_p4_subtitle, animation_p4_text)
)) {

    private var view: PinView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findViewById<View>(id.play).setOnClickListener { this@AnimationActivity.play() }
        view = findViewById(id.imageView)
        view!!.setImage(ImageSource.asset("sanmartino.jpg"))
    }

    override fun onPageChanged(page: Int) {
        if (page == 2) {
            view!!.setPanLimit(ViewValues.PAN_LIMIT_CENTER)
        } else {
            view!!.setPanLimit(ViewValues.PAN_LIMIT_INSIDE)
        }
    }

    private fun play() {
        val random = Random()
        if (view!!.isReady()) {
            val maxScale = view!!.getMaxScale()
            val minScale = view!!.getMinScale()
            val scale = random.nextFloat() * (maxScale - minScale) + minScale
            val center = PointF(random.nextInt(view!!.getSWidth()).toFloat(), random.nextInt(view!!.getSHeight()).toFloat())
            view!!.setPin(center)
            val animationBuilder = view?.animateScaleAndCenter(scale, center)
            if (page == 3) {
                animationBuilder?.withDuration(2000)?.withEasing(ViewValues.EASE_OUT_QUAD)?.withInterruptible(false)?.start()
            } else {
                animationBuilder?.withDuration(750)?.start()
            }
        }
    }

}
