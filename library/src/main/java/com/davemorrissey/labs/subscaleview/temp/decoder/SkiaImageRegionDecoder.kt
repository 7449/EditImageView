package com.davemorrissey.labs.subscaleview.temp.decoder

import android.content.ContentResolver
import android.content.Context
import android.content.res.AssetManager
import android.content.res.Resources
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import androidx.annotation.Keep
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import java.io.InputStream
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

/**
 * Default implementation of [ImageRegionDecoder]
 * using Android's [BitmapRegionDecoder], based on the Skia library. This
 * works well in most circumstances and has reasonable performance due to the cached decoder instance,
 * however it has some problems with grayscale, indexed and CMYK images.
 *
 *
 * A [ReadWriteLock] is used to delegate responsibility for multi threading behaviour to the
 * [BitmapRegionDecoder] instance on SDK &gt;= 21, whilst allowing this class to block until no
 * tiles are being loaded before recycling the decoder. In practice, [BitmapRegionDecoder] is
 * synchronized internally so this has no real impact on performance.
 */
class SkiaImageRegionDecoder(bitmapConfig: Bitmap.Config?) : ImageRegionDecoder {

    companion object {
        private const val FILE_PREFIX = "file://"
        private const val ASSET_PREFIX = "$FILE_PREFIX/android_asset/"
        private const val RESOURCE_PREFIX = ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
    }

    private val decoderLock = ReentrantReadWriteLock(true)
    private val bitmapConfig: Bitmap.Config
    private var decoder: BitmapRegionDecoder? = null

    @Keep
    constructor() : this(null)

    init {
        val globalBitmapConfig = SubsamplingScaleImageView.preferredBitmapConfig
        when {
            bitmapConfig != null -> this.bitmapConfig = bitmapConfig
            globalBitmapConfig != null -> this.bitmapConfig = globalBitmapConfig
            else -> this.bitmapConfig = Bitmap.Config.RGB_565
        }
    }

    @Throws(Exception::class)
    override fun init(context: Context, uri: Uri): Point {
        val uriString = uri.toString()
        when {
            uriString.startsWith(RESOURCE_PREFIX) -> {
                val res: Resources
                val packageName = uri.authority
                res = if (context.packageName == packageName) {
                    context.resources
                } else {
                    val pm = context.packageManager
                    pm.getResourcesForApplication(packageName ?: "")
                }
                var id = 0
                val segments = uri.pathSegments
                val size = segments.size
                if (size == 2 && segments[0] == "drawable") {
                    val resName = segments[1]
                    id = res.getIdentifier(resName, "drawable", packageName)
                } else if (size == 1 && TextUtils.isDigitsOnly(segments[0])) {
                    try {
                        id = Integer.parseInt(segments[0])
                    } catch (ignored: NumberFormatException) {
                    }
                }
                decoder = BitmapRegionDecoder.newInstance(context.resources.openRawResource(id), false)
            }
            uriString.startsWith(ASSET_PREFIX) -> {
                val assetName = uriString.substring(ASSET_PREFIX.length)
                decoder = BitmapRegionDecoder.newInstance(context.assets.open(assetName, AssetManager.ACCESS_RANDOM), false)
            }
            uriString.startsWith(FILE_PREFIX) -> decoder = BitmapRegionDecoder.newInstance(uriString.substring(FILE_PREFIX.length), false)
            else -> {
                var inputStream: InputStream? = null
                try {
                    val contentResolver = context.contentResolver
                    inputStream = contentResolver.openInputStream(uri)
                    decoder = BitmapRegionDecoder.newInstance(inputStream, false)
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close()
                        } catch (e: Exception) { /* Ignore */
                        }

                    }
                }
            }
        }
        return Point(decoder?.width ?: 0, decoder?.height ?: 0)
    }

    override fun decodeRegion(sRect: Rect, sampleSize: Int): Bitmap {
        decodeLock.lock()
        try {
            if (decoder?.isRecycled == false) {
                val options = BitmapFactory.Options()
                options.inSampleSize = sampleSize
                options.inPreferredConfig = bitmapConfig
                return decoder?.decodeRegion(sRect, options)
                        ?: throw RuntimeException("Skia image decoder returned null bitmap - image format may not be supported")
            } else {
                throw IllegalStateException("Cannot decode region after decoder has been recycled")
            }
        } finally {
            decodeLock.unlock()
        }
    }

    @Synchronized
    override fun recycle() {
        decoderLock.writeLock().lock()
        try {
            decoder?.recycle()
            decoder = null
        } finally {
            decoderLock.writeLock().unlock()
        }
    }

    override val isReady: Boolean @Synchronized get() = decoder?.isRecycled == false

    /**
     * Before SDK 21, BitmapRegionDecoder was not synchronized internally. Any attempt to decode
     * regions from multiple threads with one decoder instance causes a segfault. For old versions
     * use the write lock to enforce single threaded decoding.
     */
    private val decodeLock: Lock
        get() = if (Build.VERSION.SDK_INT < 21) {
            decoderLock.writeLock()
        } else {
            decoderLock.readLock()
        }
}
