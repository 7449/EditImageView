package com.edit.image.sample.aty

import android.os.Bundle
import com.davemorrissey.labs.subscaleview.ImageSource
import kotlinx.android.synthetic.main.activity_edit.*

/**
 * @author y
 * @create 2019-04-23
 */
class ImageBigActivity : Base() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Large Picture"
        viewEdit.setImage(ImageSource.asset("111.jpg").tilingDisabled())
    }

}