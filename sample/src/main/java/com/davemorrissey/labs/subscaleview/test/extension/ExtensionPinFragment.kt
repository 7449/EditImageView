package com.davemorrissey.labs.subscaleview.test.extension

import android.graphics.PointF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.davemorrissey.labs.subscaleview.temp.ImageSource
import com.davemorrissey.labs.subscaleview.temp.setImage
import com.davemorrissey.labs.subscaleview.test.R
import kotlinx.android.synthetic.main.extension_pin_fragment.*

class ExtensionPinFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.extension_pin_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val activity = activity as ExtensionActivity?
        next.setOnClickListener { activity?.next() }
        imageView.setImage(ImageSource.asset("sanmartino.jpg"))
        imageView.setPin(PointF(1602f, 405f))
    }

}
