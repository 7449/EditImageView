package com.davemorrissey.labs.subscaleview.test

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.FragmentActivity

abstract class AbstractFragmentsActivity protected constructor(private val title: Int, private val layout: Int, private val notes: List<Page>) : FragmentActivity() {

    companion object {
        private const val BUNDLE_PAGE = "page"
    }

    private var page: Int = 0

    protected abstract fun onPageChanged(page: Int)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout)
        val actionBar = actionBar
        actionBar?.title = getString(title)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        if (savedInstanceState != null && savedInstanceState.containsKey(BUNDLE_PAGE)) {
            page = savedInstanceState.getInt(BUNDLE_PAGE)
        }
    }

    override fun onResume() {
        super.onResume()
        updateNotes()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(BUNDLE_PAGE, page)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }

    fun next() {
        page++
        updateNotes()
    }

    fun previous() {
        page--
        updateNotes()
    }

    private fun updateNotes() {
        if (page > notes.size - 1) {
            return
        }
        val actionBar = actionBar
        actionBar?.setSubtitle(notes[page].subtitle)
        onPageChanged(page)
    }

}
