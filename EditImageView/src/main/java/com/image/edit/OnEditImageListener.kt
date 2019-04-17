package com.image.edit

/**
 * @author y
 * @create 2018/11/17
 */
interface OnEditImageListener {
    /**
     * 没有缓存
     */
    fun onLastImageEmpty() = Unit

    /**
     * 缓存已达到最大值
     */
    fun onLastCacheMax() = Unit

    /**
     * 删除了文字
     */
    fun onDeleteText() = Unit
}

open class SimpleOnEditImageListener : OnEditImageListener {
    override fun onLastCacheMax() {}
    override fun onDeleteText() {}
    override fun onLastImageEmpty() {}
}

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
