package com.edit.image.sample

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.edit.image.sample.aty.ImageBigActivity
import com.edit.image.sample.aty.ImageLongHorizontalActivity
import com.edit.image.sample.aty.ImageLongVerticalActivity
import com.edit.image.sample.aty.ImageSmallActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        image_small.setOnClickListener { startActivity(ImageSmallActivity::class.java) }
        image_big.setOnClickListener { startActivity(ImageBigActivity::class.java) }
        image_long_horizontal.setOnClickListener { startActivity(ImageLongHorizontalActivity::class.java) }
        image_long_vertical.setOnClickListener { startActivity(ImageLongVerticalActivity::class.java) }
    }

    private fun startActivity(clazz: Class<*>) {
        startActivity(Intent(this, clazz))
    }
}
