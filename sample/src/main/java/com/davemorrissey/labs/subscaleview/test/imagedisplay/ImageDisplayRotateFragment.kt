package com.davemorrissey.labs.subscaleview.test.imagedisplay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.temp.getOrientation
import com.davemorrissey.labs.subscaleview.temp.setImage
import com.davemorrissey.labs.subscaleview.temp.setOrientation
import com.davemorrissey.labs.subscaleview.test.R
import kotlinx.android.synthetic.main.imagedisplay_rotate_fragment.*

class ImageDisplayRotateFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.imagedisplay_rotate_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        imageView.setImage(ImageSource.asset("swissroad.jpg"))
        imageView.setOrientation(90)
        val activity = activity as ImageDisplayActivity?
        previous.setOnClickListener { activity?.previous() }
        next.setOnClickListener { activity?.next() }
        rotate.setOnClickListener { imageView.setOrientation((imageView.getOrientation() + 90) % 360) }
    }

}
