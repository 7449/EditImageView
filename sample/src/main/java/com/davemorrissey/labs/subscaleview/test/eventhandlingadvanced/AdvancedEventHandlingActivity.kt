package com.davemorrissey.labs.subscaleview.test.eventhandlingadvanced

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Toast
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.temp.isReady
import com.davemorrissey.labs.subscaleview.temp.setImage
import com.davemorrissey.labs.subscaleview.temp.viewToSourceCoord
import com.davemorrissey.labs.subscaleview.test.AbstractPagesActivity
import com.davemorrissey.labs.subscaleview.test.Page
import com.davemorrissey.labs.subscaleview.test.R
import kotlinx.android.synthetic.main.pages_activity.*

class AdvancedEventHandlingActivity : AbstractPagesActivity(R.string.advancedevent_title, R.layout.pages_activity, listOf(
        Page(R.string.advancedevent_p1_subtitle, R.string.advancedevent_p1_text),
        Page(R.string.advancedevent_p2_subtitle, R.string.advancedevent_p2_text),
        Page(R.string.advancedevent_p3_subtitle, R.string.advancedevent_p3_text),
        Page(R.string.advancedevent_p4_subtitle, R.string.advancedevent_p4_text),
        Page(R.string.advancedevent_p5_subtitle, R.string.advancedevent_p5_text)
)) {

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                if (imageView.isReady()) {
                    val sCoord = imageView.viewToSourceCoord(e.x, e.y)
                    Toast.makeText(applicationContext, "Single tap: " + sCoord?.x?.toInt() + ", " + sCoord?.y?.toInt(), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(applicationContext, "Single tap: Image not ready", Toast.LENGTH_SHORT).show()
                }
                return true
            }

            override fun onLongPress(e: MotionEvent) {
                if (imageView.isReady()) {
                    val sCoord = imageView.viewToSourceCoord(e.x, e.y)
                    Toast.makeText(applicationContext, "Long press: " + sCoord?.x?.toInt() + ", " + sCoord?.y?.toInt(), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(applicationContext, "Long press: Image not ready", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                if (imageView.isReady()) {
                    val sCoord = imageView.viewToSourceCoord(e.x, e.y)
                    Toast.makeText(applicationContext, "Double tap: " + sCoord?.x?.toInt() + ", " + sCoord?.y?.toInt(), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(applicationContext, "Double tap: Image not ready", Toast.LENGTH_SHORT).show()
                }
                return true
            }
        })

        imageView.setImage(ImageSource.asset("sanmartino.jpg"))
        imageView.setOnTouchListener { _, motionEvent -> gestureDetector.onTouchEvent(motionEvent) }
    }

}
