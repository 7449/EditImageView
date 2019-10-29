package com.davemorrissey.labs.subscaleview.test.extension

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.temp.setImage
import com.davemorrissey.labs.subscaleview.test.R
import kotlinx.android.synthetic.main.extension_freehand_fragment.*

class ExtensionFreehandFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.extension_freehand_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val activity = activity as ExtensionActivity?
        previous.setOnClickListener { activity?.previous() }
        imageView.setImage(ImageSource.asset("sanmartino.jpg"))
        reset.setOnClickListener { imageView.reset() }
    }

}
