package com.davemorrissey.labs.subscaleview.test.basicfeatures

import android.os.Bundle
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.temp.setImage
import com.davemorrissey.labs.subscaleview.test.AbstractPagesActivity
import com.davemorrissey.labs.subscaleview.test.Page
import com.davemorrissey.labs.subscaleview.test.R
import kotlinx.android.synthetic.main.pages_activity.*

class BasicFeaturesActivity : AbstractPagesActivity(R.string.basic_title, R.layout.pages_activity, listOf(
        Page(R.string.basic_p1_subtitle, R.string.basic_p1_text),
        Page(R.string.basic_p2_subtitle, R.string.basic_p2_text),
        Page(R.string.basic_p3_subtitle, R.string.basic_p3_text),
        Page(R.string.basic_p4_subtitle, R.string.basic_p4_text),
        Page(R.string.basic_p5_subtitle, R.string.basic_p5_text)
)) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageView.setImage(ImageSource.asset("sanmartino.jpg"))
    }

}
