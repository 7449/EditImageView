@file:Suppress("UNCHECKED_CAST")

package com.image.edit

fun <T> EditImageCache.findObj1() = obj1 as T?

fun <T> EditImageCache.findObj2() = obj2 as T?

fun <T> EditImageCache.findObj3() = obj3 as T?

fun <T> EditImageCache.findObj4() = obj4 as T?

fun <T> EditImageCache.findObj5() = obj5 as T?

fun <T> EditImageCache.findCache() = imageCache as T

fun OnEditImageAction.createCache(callback: OnEditImageCallback, imageCache: Any): EditImageCache {
    return EditImageCache(callback.obj1, callback.obj2, callback.obj3, callback.obj4, callback.obj5, copy(), imageCache)
}