package com.edit.image.sample.simple

import com.image.edit.OnEditImageListener

class SimpleOnEditImageListenerKt {
    private var onLastCacheMax: (() -> Unit)? = null
    private var onDeleteText: (() -> Unit)? = null
    private var onLastImageEmpty: (() -> Unit)? = null

    fun onLastCacheMax(onLastCacheMax: () -> Unit) {
        this.onLastCacheMax = onLastCacheMax
    }

    fun onDeleteText(onDeleteText: () -> Unit) {
        this.onDeleteText = onDeleteText
    }

    fun onLastImageEmpty(onLastImageEmpty: () -> Unit) {
        this.onLastImageEmpty = onLastImageEmpty
    }

    internal fun build(): OnEditImageListener {
        return object : OnEditImageListener {
            override fun onLastCacheMax() {
                onLastCacheMax?.invoke()
            }

            override fun onDeleteText() {
                onDeleteText?.invoke()
            }

            override fun onLastImageEmpty() {
                onDeleteText?.invoke()
            }
        }
    }
}
