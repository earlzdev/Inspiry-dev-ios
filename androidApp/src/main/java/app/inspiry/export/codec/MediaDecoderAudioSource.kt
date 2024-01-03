package app.inspiry.export.codec

import android.media.MediaCodec
import android.media.MediaFormat
import android.os.Handler
import app.inspiry.export.audio.AudioEncoder
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MediaDecoderAudioSource(
    handler: Handler,
    sourceMediaFormat: MediaFormat,
    dataSource: DataSource,
    onErrorHasHappened: (Exception) -> Unit,
    private val audioVolume: Float

) : MediaDecoderSource(handler, sourceMediaFormat, dataSource, onErrorHasHappened) {

    private var decoderOutputChannelsCount: Int? = null

    override fun putOutputToInput(
        inputBuffer: ByteBuffer,
        decoderOutputBuffer: ByteBuffer,
        outputDecoderInfo: MediaCodec.BufferInfo
    ) {
        if (audioVolume != AudioEncoder.MAX_VOLUME) {
            val channels = decoderOutputChannelsCount!!
            val shortSamples =
                decoderOutputBuffer.order(ByteOrder.nativeOrder()).asShortBuffer()

            val remaining = shortSamples.remaining()

            for (i in 0 until remaining step channels) {

                for (c in 0 until channels) {
                    var sample = shortSamples.get()

                    // Increase volume exponentially
                    sample = (sample * audioVolume).toInt().toShort()

                    // Put into encoder's buffer
                    inputBuffer.putShort(sample)
                }
            }
        } else {
            super.putOutputToInput(inputBuffer, decoderOutputBuffer, outputDecoderInfo)
        }
    }

    override fun initAudioDecoder() {
        super.initAudioDecoder()
        decoderOutputChannelsCount =
            audioDecoder!!.outputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
    }
}