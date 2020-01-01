@file:Suppress("UNCHECKED_CAST")

package com.image.edit.impl

import android.graphics.Bitmap
import android.graphics.Matrix
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView

internal fun SubsamplingScaleImageView.findPrivateBitmap() = findPrivateField<Bitmap>("bitmap")

internal fun SubsamplingScaleImageView.findPrivateMatrix() = findPrivateField<Matrix>("matrix")

internal fun <T> SubsamplingScaleImageView.findPrivateField(name: String): T? {
    return try {
        val declaredField = SubsamplingScaleImageView::class.java.getDeclaredField(name)
        declaredField.isAccessible = true
        declaredField.get(this) as T?
    } catch (ig: Exception) {
        null
    }
}