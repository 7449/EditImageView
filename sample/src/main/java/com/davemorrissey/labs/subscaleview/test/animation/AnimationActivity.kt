package com.davemorrissey.labs.subscaleview.test.animation

import android.graphics.PointF
import android.os.Bundle
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.ViewValues
import com.davemorrissey.labs.subscaleview.anim.animateScaleAndCenter
import com.davemorrissey.labs.subscaleview.temp.*
import com.davemorrissey.labs.subscaleview.test.AbstractPagesActivity
import com.davemorrissey.labs.subscaleview.test.Page
import com.davemorrissey.labs.subscaleview.test.R
import kotlinx.android.synthetic.main.animation_activity.*
import java.util.*

class AnimationActivity : AbstractPagesActivity(R.string.animation_title, R.layout.animation_activity, listOf(
        Page(R.string.animation_p1_subtitle, R.string.animation_p1_text),
        Page(R.string.animation_p2_subtitle, R.string.animation_p2_text),
        Page(R.string.animation_p3_subtitle, R.string.animation_p3_text),
        Page(R.string.animation_p4_subtitle, R.string.animation_p4_text)
)) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        play.setOnClickListener { this@AnimationActivity.play() }
        imageView.setImage(ImageSource.asset("sanmartino.jpg"))
    }

    override fun onPageChanged(page: Int) {
        if (page == 2) {
            imageView.setPanLimit(ViewValues.PAN_LIMIT_CENTER)
        } else {
            imageView.setPanLimit(ViewValues.PAN_LIMIT_INSIDE)
        }
    }

    private fun play() {
        val random = Random()
        if (imageView.isReady()) {
            val maxScale = imageView.getMaxScale()
            val minScale = imageView.getMinScale()
            val scale = random.nextFloat() * (maxScale - minScale) + minScale
            val center = PointF(random.nextInt(imageView.getSWidth()).toFloat(), random.nextInt(imageView.getSHeight()).toFloat())
            imageView.setPin(center)
            val animationBuilder = imageView?.animateScaleAndCenter(scale, center)
            if (page == 3) {
                animationBuilder?.withDuration(2000)?.withEasing(ViewValues.EASE_OUT_QUAD)?.withInterruptible(false)?.start()
            } else {
                animationBuilder?.withDuration(750)?.start()
            }
        }
    }

}
