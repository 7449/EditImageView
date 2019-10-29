package com.davemorrissey.labs.subscaleview.test.viewpager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.api.setImage
import com.davemorrissey.labs.subscaleview.test.R
import kotlinx.android.synthetic.main.view_pager_page.*

class ViewPagerFragment : Fragment() {

    companion object {
        private const val BUNDLE_ASSET = "asset"
    }

    private var asset: String? = null

    fun setAsset(asset: String) {
        this.asset = asset
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.view_pager_page, container, false)
        savedInstanceState?.let {
            if (asset == null && it.containsKey(BUNDLE_ASSET)) {
                asset = it.getString(BUNDLE_ASSET)
            }
        }
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        asset?.let { imageView.setImage(ImageSource.asset(it)) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        view?.let { outState.putString(BUNDLE_ASSET, asset) }
    }
}
