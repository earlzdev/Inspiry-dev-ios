package app.inspiry.export.audio

import android.media.MediaCodec
import android.media.MediaFormat
import app.inspiry.export.codec.DataSourceAsync
import app.inspiry.export.codec.TrackStrategy
import app.inspiry.video.player.decoder.getMimeType
import app.inspiry.export.record.Encoder
import app.inspiry.export.record.EncoderType
import java.util.*

class AudioEncoder(
    handler: android.os.Handler,
    durationUs: Long,
    progressWeight: Float,
    private val dataSource: DataSourceAsync,
    private val trackStrategy: TrackStrategy

) : Encoder(handler, durationUs, progressWeight) {

    var audioEncoder: MediaCodec? = null

    private val pendingAEncoderOutputIndices = LinkedList<Int>()
    private val pendingAEncoderOutputInfos = LinkedList<MediaCodec.BufferInfo>()
    private val pendingAEncoderInputIndices = LinkedList<Int>()

    var finishedEncodingAudio = false

    var onRelease: (() -> Unit)? = null

    private fun startAudioCodec() {
        audioEncoder?.start()
    }

    override fun initialize() {
        dataSource.setCanWriteListener {
            tryFillEncoderInput()
        }
        dataSource.start()
        initAudio()
        startAudioCodec()
    }

    override val type: EncoderType
        get() = EncoderType.AUDIO

    /**
     * Android 4.3 also introduced MediaMuxer, which allows the output of the AVC codec
     * (a raw H.264 elementary stream) to be converted to .MP4 format,
     * with or without an associated audio stream.
     */

    private fun initAudio() {
        initAudioEncoder(
            dataSource.getMediaFormat()
        )
    }

    override val isCritical: Boolean
        get() = false

    override fun checkOutputAvailable() {
        tryFillEncoderInput()
        tryFillEncoderOutput()
    }

    override fun _releaseInner() {
        super._releaseInner()

        pendingAEncoderOutputIndices.clear()
        pendingAEncoderOutputInfos.clear()
        pendingAEncoderInputIndices.clear()

        audioEncoder?.release()
        audioEncoder = null

        dataSource.release()

        onRelease?.invoke()
    }

    override fun isRelease(): Boolean {
        return audioEncoder == null
    }

    override fun onErrorHasHappened(t: Throwable) {
        super.onErrorHasHappened(t)
        finishedEncodingAudio = true
    }

    private fun initAudioEncoder(
        inputFormat: MediaFormat
    ) {

        val output = trackStrategy.createTrackStrategy(listOf(inputFormat))

        audioEncoder = MediaCodec.createEncoderByType(output.getMimeType()!!).also {
            it.setCallback(object : MediaCodec.Callback() {

                override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {

                    if (finishedEncodingAudio || audioEncoder == null) {
                        return
                    }

                    pendingAEncoderInputIndices.add(index)
                    if (canWrite) {
                        tryFillEncoderInput()
                    }
                }

                override fun onOutputBufferAvailable(
                    codec: MediaCodec,
                    index: Int,
                    info: MediaCodec.BufferInfo
                ) {
                    pendingAEncoderOutputInfos.add(info)
                    pendingAEncoderOutputIndices.add(index)

                    if (canWrite)
                        tryFillEncoderOutput()
                }

                override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
                    onErrorHasHappened(e)
                }

                override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
                    this@AudioEncoder.onFormatInitialized?.invoke(format)
                }

            }, handler)
            it.configure(output, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        }
    }

    private fun tryFillEncoderInput() {
        if (pendingAEncoderInputIndices.isNotEmpty() && dataSource.canWrite()) {
            val inputBufferIndex = pendingAEncoderInputIndices.removeFirst()
            val buffer = audioEncoder?.getInputBuffer(inputBufferIndex)
            if (buffer != null) {
                val info = dataSource.read(buffer)
                try {
                    audioEncoder?.queueInputBuffer(
                        inputBufferIndex,
                        info.offset,
                        info.size,
                        info.presentationTimeUs,
                        info.flags
                    )
                } catch (e: IllegalStateException) {
                    onErrorHasHappened(e)
                }
            }
        }
    }

    private fun tryFillEncoderOutput() {
        while (pendingAEncoderOutputIndices.size > 0 &&
            audioEncoder != null &&
            pendingAEncoderOutputInfos.size > 0
        ) {

            val index = pendingAEncoderOutputIndices.removeFirst()
            val info = pendingAEncoderOutputInfos.removeFirst()
            onEncoderOutputAvailable(index, info, audioEncoder!!)
        }
    }

    private fun onEncoderOutputAvailable(
        index: Int,
        info: MediaCodec.BufferInfo,
        codec: MediaCodec
    ) {
        try {
            if (info.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                audioEncoder?.releaseOutputBuffer(index, false)
                finishedEncodingAudio = true
                onWritingFinished()

            } else if (index >= 0) {

                if (info.size > 0) {
                    val buffer = codec.getOutputBuffer(index)!!

                    buffer.position(info.offset)
                    buffer.limit(info.offset + info.size)

                    onProgress?.invoke((info.presentationTimeUs / durationUs.toDouble()).toFloat())
                    writeSampleData?.invoke(buffer, info)
                }

                audioEncoder?.releaseOutputBuffer(index, false)
            }
        } catch (t: Throwable) {
            onErrorHasHappened(t)
        }
    }


    companion object {
        const val MAX_VOLUME = 1f
    }
}