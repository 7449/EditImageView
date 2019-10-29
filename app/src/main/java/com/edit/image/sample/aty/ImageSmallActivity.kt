package com.edit.image.sample.aty

import android.os.Bundle
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.api.setImage
import com.edit.image.sample.R
import kotlinx.android.synthetic.main.activity_edit.*

/**
 * @author y
 * @create 2019-04-23
 */
class ImageSmallActivity : Base() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Small Picture"
        view_edit.setImage(ImageSource.resource(R.drawable.icon).tilingDisabled())
    }

}