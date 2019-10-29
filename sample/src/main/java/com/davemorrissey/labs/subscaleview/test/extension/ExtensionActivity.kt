package com.davemorrissey.labs.subscaleview.test.extension


import android.util.Log
import com.davemorrissey.labs.subscaleview.test.AbstractFragmentsActivity
import com.davemorrissey.labs.subscaleview.test.Page
import com.davemorrissey.labs.subscaleview.test.R
import com.davemorrissey.labs.subscaleview.test.imagedisplay.ImageDisplayActivity

class ExtensionActivity : AbstractFragmentsActivity(R.string.extension_title, R.layout.fragments_activity, listOf(
        Page(R.string.extension_p1_subtitle, R.string.extension_p1_text),
        Page(R.string.extension_p2_subtitle, R.string.extension_p2_text),
        Page(R.string.extension_p3_subtitle, R.string.extension_p3_text)
)) {

    companion object {
        private val FRAGMENTS = listOf(
                ExtensionPinFragment::class.java,
                ExtensionCircleFragment::class.java,
                ExtensionFreehandFragment::class.java
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
