package com.davemorrissey.labs.subscaleview.temp

import android.graphics.Bitmap
import android.graphics.Rect

class Tile {

    var sRect: Rect? = null
    var sampleSize: Int = 0
    var bitmap: Bitmap? = null
    var loading: Boolean = false
    var visible: Boolean = false

    // Volatile fields instantiated once then updated before use to reduce GC.
    var vRect: Rect? = null
    var fileSRect: Rect? = null

}