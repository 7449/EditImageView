package com.davemorrissey.labs.subscaleview.test.eventhandling

import android.os.Bundle
import android.widget.Toast

import com.davemorrissey.labs.subscaleview.temp.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.davemorrissey.labs.subscaleview.temp.setImage
import com.davemorrissey.labs.subscaleview.test.AbstractPagesActivity
import com.davemorrissey.labs.subscaleview.test.Page
import com.davemorrissey.labs.subscaleview.test.R.id

import com.davemorrissey.labs.subscaleview.test.R.layout.pages_activity
import com.davemorrissey.labs.subscaleview.test.R.string.event_p1_subtitle
import com.davemorrissey.labs.subscaleview.test.R.string.event_p1_text
import com.davemorrissey.labs.subscaleview.test.R.string.event_p2_subtitle
import com.davemorrissey.labs.subscaleview.test.R.string.event_p2_text
import com.davemorrissey.labs.subscaleview.test.R.string.event_p3_subtitle
import com.davemorrissey.labs.subscaleview.test.R.string.event_p3_text
import com.davemorrissey.labs.subscaleview.test.R.string.event_title

class EventHandlingActivity : AbstractPagesActivity(event_title, pages_activity, listOf(
        Page(event_p1_subtitle, event_p1_text),
        Page(event_p2_subtitle, event_p2_text),
        Page(event_p3_subtitle, event_p3_text)
)) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val imageView = findViewById<SubsamplingScaleImageView>(id.imageView)
        imageView.setImage(ImageSource.asset("sanmartino.jpg"))
        imageView.setOnClickListener { v -> Toast.makeText(v.context, "Clicked", Toast.LENGTH_SHORT).show() }
        imageView.setOnLongClickListener { v ->
            Toast.makeText(v.context, "Long clicked", Toast.LENGTH_SHORT).show()
            true
        }
    }

}
