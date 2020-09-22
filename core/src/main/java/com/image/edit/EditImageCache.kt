@file:Suppress("UNCHECKED_CAST")

package com.image.edit

data class EditImageCache(
        val onEditImageAction: OnEditImageAction,
        val imageCache: Any,
        val obj1: Any? = Unit,
        val obj2: Any? = Unit,
        val obj3: Any? = Unit,
        val obj4: Any? = Unit,
        val obj5: Any? = Unit,
        val obj6: Any? = Unit,
) {
    fun <T> findObj1() = obj1 as T?
    fun <T> findObj2() = obj2 as T?
    fun <T> findObj3() = obj3 as T?
    fun <T> findObj4() = obj4 as T?
    fun <T> findObj5() = obj5 as T?
    fun <T> findObj6() = obj6 as T?
    fun <T> findCache() = imageCache as T
}