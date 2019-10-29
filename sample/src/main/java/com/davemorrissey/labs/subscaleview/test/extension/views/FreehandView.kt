package com.davemorrissey.labs.subscaleview.test.extension.views

import android.content.Context
import android.graphics.*
import android.graphics.Paint.Cap
import android.graphics.Paint.Style
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.davemorrissey.labs.subscaleview.api.isReady
import com.davemorrissey.labs.subscaleview.api.sourceToViewCoord
import com.davemorrissey.labs.subscaleview.api.viewToSourceCoord
import java.util.*
import kotlin.math.abs

class FreehandView @JvmOverloads constructor(context: Context, attr: AttributeSet? = null) : SubsamplingScaleImageView(context, attr), OnTouchListener {

    private val paint = Paint()
    private val vPath = Path()
    private val vPoint = PointF()
    private var vPrev = PointF()
    private var vPrevious: PointF? = null
    private var vStart: PointF? = null
    private var drawing = false

    private var strokeWidth: Int = 0

    private var sPoints: MutableList<PointF>? = null

    init {
        setOnTouchListener(this)
        val density = resources.displayMetrics.densityDpi.toFloat()
        strokeWidth = (density / 60f).toInt()
    }

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        return false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (sPoints != null && !drawing) {
            return super.onTouchEvent(event)
        }
        var consumed = false
        val touchCount = event.pointerCount
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (event.actionIndex == 0) {
                    vStart = PointF(event.x, event.y)
                    vPrevious = PointF(event.x, event.y)
                } else {
                    vStart = null
                    vPrevious = null
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val sCurrentF = viewToSourceCoord(event.x, event.y)

                sCurrentF ?: return super.onTouchEvent(event)

                val sCurrent = PointF(sCurrentF.x, sCurrentF.y)

                var sStart: PointF? = null

                vStart?.let { vStart ->
                    sStart = PointF(viewToSourceCoord(vStart)?.x
                            ?: 0F, viewToSourceCoord(vStart)?.y
                            ?: 0F)
                }

                if (touchCount == 1 && vStart != null) {
                    vPrevious?.let { vPrevious ->
                        val vDX = abs(event.x - vPrevious.x)
                        val vDY = abs(event.y - vPrevious.y)
                        if (vDX >= strokeWidth * 5 || vDY >= strokeWidth * 5) {
                            if (sPoints == null) {
                                sPoints = ArrayList()
                                sStart?.let { sPoints?.add(it) }
                            }
                            sPoints?.add(sCurrent)
                            vPrevious.x = event.x
                            vPrevious.y = event.y
                            drawing = true
                        }
                        consumed = true
                        invalidate()
                    }
                } else if (touchCount == 1) {
                    // Consume all one touch drags to prevent odd panning effects handled by the superclass.
                    consumed = true
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                invalidate()
                drawing = false
                vPrevious = null
                vStart = null
            }
        }
        // Use parent to handle pinch and two-finger pan.
        return consumed || super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Don't draw anything before image is ready.
        if (!isReady()) {
            return
        }

        paint.isAntiAlias = true

        sPoints?.let { sPoints ->
            if (sPoints.size >= 2) {
                vPath.reset()
                sourceToViewCoord(sPoints[0].x, sPoints[0].y, vPrev)
                vPath.moveTo(vPrev.x, vPrev.y)
                for (i in 1 until sPoints.size) {
                    sourceToViewCoord(sPoints[i].x, sPoints[i].y, vPoint)
                    vPath.quadTo(vPrev.x, vPrev.y, (vPoint.x + vPrev.x) / 2, (vPoint.y + vPrev.y) / 2)
                    vPrev = vPoint
                }
                paint.style = Style.STROKE
                paint.strokeCap = Cap.ROUND
                paint.strokeWidth = (strokeWidth * 2).toFloat()
                paint.color = Color.BLACK
                canvas.drawPath(vPath, paint)
                paint.strokeWidth = strokeWidth.toFloat()
                paint.color = Color.argb(255, 51, 181, 229)
                canvas.drawPath(vPath, paint)
            }
        }
    }

    fun reset() {
        this.sPoints = null
        invalidate()
    }

}