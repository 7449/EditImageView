package com.edit.image.sample.aty

import android.os.Bundle
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.api.setImage
import kotlinx.android.synthetic.main.activity_edit.*

/**
 * @author y
 * @create 2019-04-23
 */
class ImageLongHorizontalActivity : Base() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Long Picture(Horizontal)"
        view_edit.setImage(ImageSource.asset("aaa.jpg").tilingDisabled())
    }

}