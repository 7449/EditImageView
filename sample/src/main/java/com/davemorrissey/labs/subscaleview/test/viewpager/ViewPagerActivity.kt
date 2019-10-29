package com.davemorrissey.labs.subscaleview.test.viewpager

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.davemorrissey.labs.subscaleview.test.AbstractPagesActivity
import com.davemorrissey.labs.subscaleview.test.Page
import com.davemorrissey.labs.subscaleview.test.R
import kotlinx.android.synthetic.main.view_pager.*

class ViewPagerActivity : AbstractPagesActivity(R.string.pager_title, R.layout.view_pager, listOf(
        Page(R.string.pager_p1_subtitle, R.string.pager_p1_text),
        Page(R.string.pager_p2_subtitle, R.string.pager_p2_text)
)) {

    companion object {
        private val IMAGES = arrayOf("sanmartino.jpg", "swissroad.jpg")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        horizontal_pager.adapter = ScreenSlidePagerAdapter(supportFragmentManager)
        vertical_pager.adapter = ScreenSlidePagerAdapter(supportFragmentManager)
    }

    override fun onBackPressed() {
        val viewPager = findViewById<ViewPager>(if (page == 0) R.id.horizontal_pager else R.id.vertical_pager)
        if (viewPager.currentItem == 0) {
            super.onBackPressed()
        } else {
            viewPager.currentItem = viewPager.currentItem - 1
        }
    }

    override fun onPageChanged(page: Int) {
        if (page == 0) {
            horizontal_pager.visibility = View.VISIBLE
            vertical_pager.visibility = View.GONE
        } else {
            horizontal_pager.visibility = View.GONE
            vertical_pager.visibility = View.VISIBLE
        }
    }

    private inner class ScreenSlidePagerAdapter internal constructor(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            val fragment = ViewPagerFragment()
            fragment.setAsset(IMAGES[position])
            return fragment
        }

        override fun getCount(): Int {
            return IMAGES.size
        }
    }

}
