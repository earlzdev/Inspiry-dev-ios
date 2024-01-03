package app.inspiry.export.codec

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import app.inspiry.core.data.OriginalAudioTrack
import app.inspiry.export.bass.setEndOfStream
import java.nio.ByteBuffer

class MediaExtractorSource(
    val context: Context,
    private val audioTrack: OriginalAudioTrack
) : DataSource {

    private lateinit var audioExtractor: MediaExtractor
    private lateinit var format: MediaFormat
    private val bufferInfo = MediaCodec.BufferInfo()
    private var isDrained: Boolean = false

    override fun start() {
        audioExtractor = MediaExtractor().also {
            it.setDataSource(context, Uri.parse(audioTrack.path), null)

            for (i in 0 until it.trackCount) {
                val format = it.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME)

                if (mime?.startsWith("audio/") == true) {

                    it.selectTrack(i)

                    it.seekTo(audioTrack.contentOffsetUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC)

                    this.format = format

                    break
                }
            }
        }
        assert(this::format.isInitialized)
    }

    override fun getMediaFormat(): MediaFormat {
        return format
    }

    override fun release() {
        audioExtractor.release()
    }

    @SuppressLint("WrongConstant")
    override fun read(inputBuffer: ByteBuffer): MediaCodec.BufferInfo {
        if (isDrained) return bufferInfo.setEndOfStream()

        val size = audioExtractor.readSampleData(inputBuffer, 0)
        val time = audioExtractor.sampleTime - audioTrack.contentOffsetUs
        val flags = audioExtractor.sampleFlags

        if (size <= 0 || time >= audioTrack.viewDurationUs) {
            isDrained = true
        } else {
            audioExtractor.advance()
        }

        bufferInfo.set(0, size, time, flags)

        return bufferInfo
    }
}