package com.edit.image.sample.aty

import android.os.Bundle
import com.davemorrissey.labs.subscaleview.ImageSource
import kotlinx.android.synthetic.main.activity_edit.*

/**
 * @author y
 * @create 2019-04-23
 */
@Deprecated("")
class ImageLongHorizontalActivity : Base() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Long Picture(Horizontal)"
        viewEdit.setImage(ImageSource.asset("aaa.jpg").tilingDisabled())
    }

}