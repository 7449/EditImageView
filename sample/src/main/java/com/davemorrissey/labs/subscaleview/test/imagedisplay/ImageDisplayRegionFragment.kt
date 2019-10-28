package com.davemorrissey.labs.subscaleview.test.imagedisplay

import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.davemorrissey.labs.subscaleview.*
import com.davemorrissey.labs.subscaleview.core.ViewValues
import com.davemorrissey.labs.subscaleview.decoder.*
import com.davemorrissey.labs.subscaleview.test.R
import com.davemorrissey.labs.subscaleview.test.R.layout

class ImageDisplayRegionFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(layout.imagedisplay_region_fragment, container, false)
        val imageView = rootView.findViewById<SubsamplingScaleImageView>(R.id.imageView)
        imageView.setBitmapDecoderFactory(CompatDecoderFactory<ImageDecoder>(SkiaImageDecoder::class.java, Bitmap.Config.ARGB_8888))
        imageView.setRegionDecoderFactory(CompatDecoderFactory<ImageRegionDecoder>(SkiaImageRegionDecoder::class.java, Bitmap.Config.ARGB_8888))
        imageView.setOrientation(ViewValues.ORIENTATION_90)
        imageView.setImage(ImageSource.asset("card.png").region(Rect(5200, 651, 8200, 3250)))
        val activity = activity as ImageDisplayActivity?
        if (activity != null) {
            rootView.findViewById<View>(R.id.previous).setOnClickListener { activity.previous() }
        }
        rootView.findViewById<View>(R.id.rotate).setOnClickListener { imageView.setOrientation((imageView.getOrientation() + 90) % 360) }
        return rootView
    }

}
