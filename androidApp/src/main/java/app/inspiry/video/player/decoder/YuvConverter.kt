package app.inspiry.video.player.decoder

import android.graphics.Bitmap
import android.media.Image

class YuvConverter(private val bitmap: Bitmap) {

    private val width = bitmap.width
    private val height = bitmap.height
    /**
     * Used to avoid unnecessary creations
     */
    private val yuvData = Array(3) { byteArrayOf() } // YUV - 4:1:1
    private var argbData = IntArray(width * height) // ARGB, ARGB, ARGB, ...

    fun convert(image: Image) {
        convertYuvImageToRgb(image)
        bitmap.setPixels(argbData, 0, width, 0, 0, width, height)
    }

    private fun convertYuvImageToRgb(image: Image) {
        val planes = image.planes
        yuvData.fillData(planes)
        val yRowStride = planes[0].rowStride
        val uvRowStride = planes[1].rowStride
        val uvPixelStride = planes[1].pixelStride
        convertYuv420ToArgb888(yRowStride, uvRowStride, uvPixelStride)
    }

    private fun Array<ByteArray>.fillData(planes: Array<Image.Plane>) {
        for (i in planes.indices) {
            val buffer = planes[i].buffer
            val size = buffer.capacity()
            if (this[i].size != size) this[i] = ByteArray(size)
            buffer.get(this[i])
        }
    }

    /**
     * YUV - (4:1:1)
     * Y value used for each pixel
     * U, V value used for 4 ARGB pixels (used shr operation)
     */
    private fun convertYuv420ToArgb888(
            yRowStride: Int,
            uvRowStride: Int,
            uvPixelStride: Int
    ) {
        val yArray = yuvData[0]
        val uArray = yuvData[1]
        val vArray = yuvData[2]
        var i = 0
        for (row in 0 until height) {
            val yRowOffset = yRowStride * row
            val uvRowOffset = uvRowStride * (row shr 1)
            for (column in 0 until width) {
                val uvColumn = uvPixelStride * (column shr 1)
                argbData[i++] = toARGB(
                        y = yArray.getUInt(yRowOffset + column),
                        u = uArray.getUInt(uvRowOffset + uvColumn),
                        v = vArray.getUInt(uvRowOffset + uvColumn)
                )
            }
        }
    }

    /**
     * This is the floating point equivalent. We do the conversion in integer
     * because some Android devices do not have floating point in hardware
     * r = 1.164 * y + 2.018 * u
     * g = 1.164 * y - 0.813 * v - 0.391 * u
     * b = 1.164 * y + 1.596 * v
     */
    @Suppress("NAME_SHADOWING")
    private fun toARGB(y: Int, u: Int, v: Int): Int {
        var y = y - 16
        val u = u - 128
        val v = v - 128
        if (y < 0) y = 0
        // Calculate (multiply by 10)
        var r = 1192 * y + 1634 * v
        var g = 1192 * y - 833 * v - 400 * u
        var b = 1192 * y + 2066 * u
        // Limit values
        r = r.coerceIn(0, RGB_MAX_CHANNEL_VALUE_POW_10)
        g = g.coerceIn(0, RGB_MAX_CHANNEL_VALUE_POW_10)
        b = b.coerceIn(0, RGB_MAX_CHANNEL_VALUE_POW_10)
        // Restore (divide by 10)
        r = (r shr 10) and 0xff
        g = (g shr 10) and 0xff
        b = (b shr 10) and 0xff
        return 0xff000000.toInt() or (r shl 16) or (g shl 8) or b
    }

    private fun ByteArray.getUInt(position: Int) = this[position].toInt() and 0xFF

    companion object {
        /**
         * This value is 2 ^ 18 - 1, and is used to clamp the RGB values before their ranges
         * are normalized to eight bits.
         */
        private const val RGB_MAX_CHANNEL_VALUE_POW_10 = 262143
    }
}