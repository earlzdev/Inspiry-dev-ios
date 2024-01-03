package app.inspiry.export.codec

import android.media.MediaCodec
import android.media.MediaFormat
import java.nio.ByteBuffer

interface DataSource {
    fun getMediaFormat(): MediaFormat
    fun release()
    fun read(inputBuffer: ByteBuffer): MediaCodec.BufferInfo
    fun start()
}