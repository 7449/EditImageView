package com.image.edit

interface OnEditImageListener {
    /**
     * 没有缓存
     */
    fun onLastImageEmpty() = Unit

    /**
     * 缓存已达到最大值
     */
    fun onLastCacheMax() = Unit
}