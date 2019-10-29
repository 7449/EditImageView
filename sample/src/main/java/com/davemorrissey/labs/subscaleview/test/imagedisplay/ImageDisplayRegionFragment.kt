package com.davemorrissey.labs.subscaleview.test.imagedisplay

import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.ViewValues
import com.davemorrissey.labs.subscaleview.decoder.*
import com.davemorrissey.labs.subscaleview.temp.*
import com.davemorrissey.labs.subscaleview.test.R
import kotlinx.android.synthetic.main.imagedisplay_region_fragment.*

class ImageDisplayRegionFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.imagedisplay_region_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        imageView.setBitmapDecoderFactory(CompatDecoderFactory<ImageDecoder>(SkiaImageDecoder::class.java, Bitmap.Config.ARGB_8888))
        imageView.setRegionDecoderFactory(CompatDecoderFactory<ImageRegionDecoder>(SkiaImageRegionDecoder::class.java, Bitmap.Config.ARGB_8888))
        imageView.setOrientation(ViewValues.ORIENTATION_90)
        imageView.setImage(ImageSource.asset("card.png").region(Rect(5200, 651, 8200, 3250)))
        val activity = activity as ImageDisplayActivity?
        previous.setOnClickListener { activity?.previous() }
        rotate.setOnClickListener { imageView.setOrientation((imageView.getOrientation() + 90) % 360) }
    }

}
