package com.davemorrissey.labs.subscaleview.test.imagedisplay

import android.util.Log
import com.davemorrissey.labs.subscaleview.test.AbstractFragmentsActivity
import com.davemorrissey.labs.subscaleview.test.Page
import com.davemorrissey.labs.subscaleview.test.R
import com.davemorrissey.labs.subscaleview.test.R.layout.fragments_activity

class ImageDisplayActivity : AbstractFragmentsActivity(R.string.display_title, fragments_activity, listOf(
        Page(R.string.display_p1_subtitle, R.string.display_p1_text),
        Page(R.string.display_p2_subtitle, R.string.display_p2_text),
        Page(R.string.display_p3_subtitle, R.string.display_p3_text)
)) {

    companion object {
        private val FRAGMENTS = listOf(
                ImageDisplayLargeFragment::class.java,
                ImageDisplayRotateFragment::class.java,
                ImageDisplayRegionFragment::class.java
        )
    }

    override fun onPageChanged(page: Int) {
        try {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.frame, FRAGMENTS[page].newInstance())
                    .commit()
        } catch (e: Exception) {
            Log.e(ImageDisplayActivity::class.java.name, "Failed to load fragment", e)
        }
    }

}
