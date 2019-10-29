package com.davemorrissey.labs.subscaleview.test.extension.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.davemorrissey.labs.subscaleview.temp.isReady
import com.davemorrissey.labs.subscaleview.temp.sourceToViewCoord
import com.davemorrissey.labs.subscaleview.test.R.drawable

class PinView @JvmOverloads constructor(context: Context, attr: AttributeSet? = null) : SubsamplingScaleImageView(context, attr) {

    private val paint = Paint()
    private val vPin = PointF()
    private lateinit var sPin: PointF
    private lateinit var pin: Bitmap

    init {
        initialise()
    }

    fun setPin(sPin: PointF) {
        this.sPin = sPin
        initialise()
        invalidate()
    }

    private fun initialise() {
        val density = resources.displayMetrics.densityDpi.toFloat()
        pin = BitmapFactory.decodeResource(this.resources, drawable.pushpin_blue)
        val w = density / 420f * pin.width
        val h = density / 420f * pin.height
        pin = Bitmap.createScaledBitmap(pin, w.toInt(), h.toInt(), true)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Don't draw pin before image is ready so it doesn't move around during setup.
        if (!isReady()) {
            return
        }

        paint.isAntiAlias = true

        if (::sPin.isInitialized) {
            sourceToViewCoord(sPin, vPin)
            val vX = vPin.x - pin.width / 2
            val vY = vPin.y - pin.height
            canvas.drawBitmap(pin, vX, vY, paint)
        }
    }

}
