package com.davemorrissey.labs.subscaleview

import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import java.io.File
import java.io.UnsupportedEncodingException
import java.net.URLDecoder

/**
 * Helper class used to set the source and additional attributes from a variety of sources. Supports
 * use of a bitmap, asset, resource, external file or any other URI.
 *
 *
 * When you are using a preview image, you must set the dimensions of the full size image on the
 * ImageSource object for the full size image using the [.dimensions] method.
 */
class ImageSource {

    companion object {
        const val DEFAULT_RESOURCE = -11
        const val FILE_SCHEME = "file:///"
        const val ASSET_SCHEME = "file:///android_asset/"

        /**
         * Create an instance from a resource. The correct resource for the device screen resolution will be used.
         *
         * @param resId resource ID.
         * @return an [ImageSource] instance.
         */
        fun resource(resId: Int): ImageSource = ImageSource(resId)

        /**
         * Create an instance from an asset name.
         *
         * @param assetName asset name.
         * @return an [ImageSource] instance.
         */
        fun asset(assetName: String): ImageSource = uri(ASSET_SCHEME + assetName)

        /**
         * Create an instance from a URI. If the URI does not start with a scheme, it's assumed to be the URI
         * of a file.
         *
         * @param uri image URI.
         * @return an [ImageSource] instance.
         */
        fun uri(uri: String): ImageSource {
            if (uri.contains("://")) {
                return ImageSource(Uri.parse(uri))
            }
            var newUri = ""
            if (uri.contains("/")) {
                newUri = uri.substring(1)
            }
            return ImageSource(Uri.parse(FILE_SCHEME + newUri))
        }

        /**
         * Create an instance from a URI.
         *
         * @param uri image URI.
         * @return an [ImageSource] instance.
         */
        fun uri(uri: Uri): ImageSource = ImageSource(uri)

        /**
         * Provide a loaded bitmap for display.
         *
         * @param bitmap bitmap to be displayed.
         * @return an [ImageSource] instance.
         */
        fun bitmap(bitmap: Bitmap): ImageSource = ImageSource(bitmap, false)

        /**
         * Provide a loaded and cached bitmap for display. This bitmap will not be recycled when it is no
         * longer needed. Use this method if you loaded the bitmap with an image loader such as Picasso
         * or Volley.
         *
         * @param bitmap bitmap to be displayed.
         * @return an [ImageSource] instance.
         */
        fun cachedBitmap(bitmap: Bitmap): ImageSource = ImageSource(bitmap, true)
    }

    private var uri: Uri? = null
    private var bitmap: Bitmap? = null
    private var resource: Int = DEFAULT_RESOURCE
    private var tile: Boolean = false
    private var sWidth: Int = 0
    private var sHeight: Int = 0
    private var sRegion: Rect? = null
    private var cached: Boolean = false

    constructor(bitmap: Bitmap, cached: Boolean) {
        this.bitmap = bitmap
        this.uri = null
        this.resource = DEFAULT_RESOURCE
        this.tile = false
        this.sWidth = bitmap.width
        this.sHeight = bitmap.height
        this.cached = cached
    }

    constructor(uri: Uri) {
        // #114 If file doesn't exist, attempt to url decode the URI and try again
        val uriString = uri.toString()
        var newUri: Uri? = null
        if (uriString.startsWith(FILE_SCHEME)) {
            val uriFile = File(uriString.substring(FILE_SCHEME.length - 1))
            if (!uriFile.exists()) {
                try {
                    newUri = Uri.parse(URLDecoder.decode(uriString, "UTF-8"))
                } catch (e: UnsupportedEncodingException) {
                    // Fallback to encoded URI. This exception is not expected.
                }
            }
        }
        this.bitmap = null
        this.uri = newUri
        this.resource = DEFAULT_RESOURCE
        this.tile = true
    }

    constructor(resource: Int) {
        this.bitmap = null
        this.uri = null
        this.resource = resource
        this.tile = true
    }

    /**
     * Enable tiling of the image. This does not apply to preview images which are always loaded as a single bitmap.,
     * and tiling cannot be disabled when displaying a region of the source image.
     *
     * @return this instance for chaining.
     */
    fun tilingEnabled(): ImageSource = tiling(true)

    /**
     * Disable tiling of the image. This does not apply to preview images which are always loaded as a single bitmap,
     * and tiling cannot be disabled when displaying a region of the source image.
     *
     * @return this instance for chaining.
     */
    fun tilingDisabled(): ImageSource = tiling(false)

    /**
     * Enable or disable tiling of the image. This does not apply to preview images which are always loaded as a single bitmap,
     * and tiling cannot be disabled when displaying a region of the source image.
     *
     * @param tile whether tiling should be enabled.
     * @return this instance for chaining.
     */
    fun tiling(tile: Boolean): ImageSource {
        this.tile = tile
        return this
    }

    /**
     * Use a region of the source image. Region must be set independently for the full size image and the preview if
     * you are using one.
     *
     * @param sRegion the region of the source image to be displayed.
     * @return this instance for chaining.
     */
    fun region(sRegion: Rect): ImageSource {
        this.sRegion = sRegion
        setInvariants()
        return this
    }

    /**
     * Declare the dimensions of the image. This is only required for a full size image, when you are specifying a URI
     * and also a preview image. When displaying a bitmap object, or not using a preview, you do not need to declare
     * the image dimensions. Note if the declared dimensions are found to be incorrect, the view will reset.
     *
     * @param sWidth  width of the source image.
     * @param sHeight height of the source image.
     * @return this instance for chaining.
     */
    fun dimensions(sWidth: Int, sHeight: Int): ImageSource {
        if (bitmap == null) {
            this.sWidth = sWidth
            this.sHeight = sHeight
        }
        setInvariants()
        return this
    }

    private fun setInvariants() {
        if (this.sRegion != null) {
            this.tile = true
            this.sWidth = this.sRegion?.width() ?: 0
            this.sHeight = this.sRegion?.height() ?: 0
        }
    }

    fun getUri(): Uri? = uri

    fun getBitmap(): Bitmap? = bitmap

    fun getResource(): Int = resource

    fun getTile(): Boolean = tile

    fun getSWidth(): Int = sWidth

    fun getSHeight(): Int = sHeight

    fun getSRegion(): Rect? = sRegion

    fun isCached(): Boolean = cached
}
