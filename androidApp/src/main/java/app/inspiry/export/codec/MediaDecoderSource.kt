package app.inspiry.export.codec

import android.media.MediaCodec
import android.media.MediaFormat
import android.os.Handler
import app.inspiry.video.player.decoder.getMimeType
import java.nio.ByteBuffer
import java.util.*

open class MediaDecoderSource(
    val handler: Handler,
    val sourceMediaFormat: MediaFormat,
    val dataSource: DataSource,
    val onErrorHasHappened: (Exception) -> Unit
) : DataSourceAsync {

    protected var audioDecoder: MediaCodec? = null

    private val pendingADecoderOutputIndices = LinkedList<Int>()
    private val pendingADecoderOutputInfos = LinkedList<MediaCodec.BufferInfo>()

    private var onCanWrite: (() -> Unit)? = null

    override fun start() {
        initAudioDecoder()
        audioDecoder?.start()
    }

    protected open fun initAudioDecoder() {
        audioDecoder = MediaCodec.createDecoderByType(sourceMediaFormat.getMimeType()!!)
        audioDecoder!!.setCallback(object : MediaCodec.Callback() {
            override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {

                if (audioDecoder == null) return

                val buffer: ByteBuffer
                try {
                    buffer = codec.getInputBuffer(index)!!
                } catch (e: IllegalStateException) {
                    onErrorHasHappened(e)
                    return
                }

                val chunk = dataSource.read(buffer)

                codec.queueInputBuffer(
                    index, chunk.offset, chunk.size,
                    chunk.presentationTimeUs, chunk.flags
                )
            }

            override fun onOutputBufferAvailable(
                codec: MediaCodec,
                index: Int,
                info: MediaCodec.BufferInfo
            ) {
                if (index >= 0) {
                    pendingADecoderOutputIndices.add(index)
                    pendingADecoderOutputInfos.add(info)

                    // == 1 because only on change
                    if (pendingADecoderOutputIndices.size == 1) {
                        onCanWrite?.invoke()
                    }
                }
            }

            override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
                onErrorHasHappened(e)
            }

            override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
            }

        }, handler)

        audioDecoder!!.configure(sourceMediaFormat, null, null, 0)
    }

    override fun getMediaFormat() = dataSource.getMediaFormat()

    override fun release() {
        pendingADecoderOutputIndices.clear()
        pendingADecoderOutputInfos.clear()
        audioDecoder?.release()
        audioDecoder = null
    }

    override fun canWrite(): Boolean {
        return pendingADecoderOutputIndices.isNotEmpty() && pendingADecoderOutputInfos.isNotEmpty()
    }

    override fun setCanWriteListener(canWrite: () -> Unit) {
        this.onCanWrite = canWrite
    }

    open fun putOutputToInput(
        inputBuffer: ByteBuffer, decoderOutputBuffer: ByteBuffer, outputDecoderInfo: MediaCodec.BufferInfo) {

        decoderOutputBuffer.position(outputDecoderInfo.offset)
        decoderOutputBuffer.limit(outputDecoderInfo.offset + outputDecoderInfo.size)

        inputBuffer.position(0)
        inputBuffer.put(decoderOutputBuffer)
    }

    override fun read(
        inputBuffer: ByteBuffer
    ): MediaCodec.BufferInfo {

        val outputDecoderIndex = pendingADecoderOutputIndices.removeFirst()
        val outputDecoderInfo = pendingADecoderOutputInfos.removeFirst()

        val decoderOutputBuffer =
            audioDecoder!!.getOutputBuffer(outputDecoderIndex)!!

        putOutputToInput(inputBuffer, decoderOutputBuffer, outputDecoderInfo)
        audioDecoder!!.releaseOutputBuffer(outputDecoderIndex, false)

        return outputDecoderInfo
    }
}