package app.inspiry.video.player.decoder

import java.nio.ByteBuffer

/**
 * Used to transfer data between VideoExtractor and VideoDecoder
 */
class FrameInfo(
    var size: Int = 0,
    var timeUs: Long = 0,
    var flags: Int = 0,
) {

    private lateinit var data: ByteBuffer

    override fun toString() = StringBuilder()
        .append("size - ").append(size).append(", ")
        .append("timeUs - ").append(timeUs).append(", ")
        .append("flags - ").append(flags)
        .toString()


    fun setValues(size: Int, timeUs: Long, flags: Int, buffer: ByteBuffer) {
        this.size = size
        this.timeUs = timeUs
        this.flags = flags
        this.data = buffer
    }
}