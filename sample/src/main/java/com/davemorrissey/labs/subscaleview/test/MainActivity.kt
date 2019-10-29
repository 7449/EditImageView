package com.davemorrissey.labs.subscaleview.test

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.davemorrissey.labs.subscaleview.test.animation.AnimationActivity
import com.davemorrissey.labs.subscaleview.test.basicfeatures.BasicFeaturesActivity
import com.davemorrissey.labs.subscaleview.test.configuration.ConfigurationActivity
import com.davemorrissey.labs.subscaleview.test.eventhandling.EventHandlingActivity
import com.davemorrissey.labs.subscaleview.test.eventhandlingadvanced.AdvancedEventHandlingActivity
import com.davemorrissey.labs.subscaleview.test.extension.ExtensionActivity
import com.davemorrissey.labs.subscaleview.test.imagedisplay.ImageDisplayActivity
import com.davemorrissey.labs.subscaleview.test.viewpager.ViewPagerActivity
import kotlinx.android.synthetic.main.main.*

class MainActivity : Activity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val actionBar = actionBar
        actionBar?.setTitle(R.string.main_title)
        setContentView(R.layout.main)
        basicFeatures.setOnClickListener(this)
        imageDisplay.setOnClickListener(this)
        eventHandling.setOnClickListener(this)
        advancedEventHandling.setOnClickListener(this)
        viewPagerGalleries.setOnClickListener(this)
        animation.setOnClickListener(this)
        extension.setOnClickListener(this)
        configuration.setOnClickListener(this)
        github.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.basicFeatures -> startActivity(BasicFeaturesActivity::class.java)
            R.id.imageDisplay -> startActivity(ImageDisplayActivity::class.java)
            R.id.eventHandling -> startActivity(EventHandlingActivity::class.java)
            R.id.advancedEventHandling -> startActivity(AdvancedEventHandlingActivity::class.java)
            R.id.viewPagerGalleries -> startActivity(ViewPagerActivity::class.java)
            R.id.animation -> startActivity(AnimationActivity::class.java)
            R.id.extension -> startActivity(ExtensionActivity::class.java)
            R.id.configuration -> startActivity(ConfigurationActivity::class.java)
            R.id.github -> openGitHub()
        }
    }

    private fun startActivity(activity: Class<out Activity>) {
        val intent = Intent(this, activity)
        startActivity(intent)
    }

    private fun openGitHub() {
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse("https://github.com/davemorrissey/subsampling-scale-image-view")
        startActivity(i)
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }
}
