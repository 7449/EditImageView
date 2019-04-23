package com.edit.image.sample.aty

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.davemorrissey.labs.subscaleview.ImageSource
import kotlinx.android.synthetic.main.activity_edit.*

/**
 * @author y
 * @create 2019-04-23
 */
class ImageLongVerticalActivity : Base() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Long Picture(Vertical)"
        Glide.with(this).asBitmap().load(Uri.parse("file:///android_asset/ccc.jpg")).into(object : SimpleTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                view_edit.setImage(ImageSource.cachedBitmap(resource))
            }
        })
    }

}