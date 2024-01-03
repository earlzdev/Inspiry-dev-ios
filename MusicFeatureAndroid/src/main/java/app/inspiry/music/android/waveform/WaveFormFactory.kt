package app.inspiry.music.android.waveform

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
import app.inspiry.core.data.InspResponse
import app.inspiry.core.data.InspResponseData
import app.inspiry.core.data.InspResponseLoading
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.ByteArrayOutputStream

/**
 * Factory class to build [WaveFormData]
 *
 * Note : It build data asynchronously
 */
object WaveFormFactory {

    private fun MediaExtractor.selectAudioTrack(): Int {
        if (trackCount == 0) throw Exception("No track")
        val audioTrackIndex = getAudioTrackIndex()
        if (audioTrackIndex == -1) throw Exception("No audio track")
        selectTrack(audioTrackIndex)
        return audioTrackIndex
    }

    private fun MediaExtractor.getAudioTrackIndex(): Int {
        for (i in 0 until trackCount) {
            // select audio track
            if (getTrackFormat(i).getString(MediaFormat.KEY_MIME)!!.contains("audio/")) {
                return i
            }
        }
        return -1
    }

    /**
     * Build a data using constructor params
     *
     * Note : It works asynchronously and takes several seconds
     */
    fun build(initExtractor: (MediaExtractor) -> Unit): Flow<InspResponse<WaveFormData>> {

        return flow {

            var codec: MediaCodec? = null
            var extractor: MediaExtractor? = null

            try {
                extractor = MediaExtractor()
                initExtractor(extractor)

                val audioTrackIndex = extractor.selectAudioTrack()

                val format = extractor.getTrackFormat(audioTrackIndex)

                codec = MediaCodec.createDecoderByType(format.getString(MediaFormat.KEY_MIME)!!)
                codec.configure(format, null, null, 0)
                val outFormat = codec.outputFormat

                val estimateSize =
                    format.getLong(MediaFormat.KEY_DURATION) / 1000000f * format.getInteger(
                        MediaFormat.KEY_CHANNEL_COUNT
                    ) * format.getInteger(MediaFormat.KEY_SAMPLE_RATE) * 2f

                val startTime = System.currentTimeMillis()
                codec.start()
                Log.i("WaveFormFactory", "Start building data.")


                var EOS = false
                val stream = ByteArrayOutputStream()
                val info = MediaCodec.BufferInfo()

                while (!EOS) {
                    val inputBufferId = codec.dequeueInputBuffer(10)
                    if (inputBufferId >= 0) {
                        val inputBuffer = codec.getInputBuffer(inputBufferId)
                        if (inputBuffer != null) {
                            val readSize = extractor.readSampleData(inputBuffer, 0)
                            extractor.advance()
                            codec.queueInputBuffer(
                                inputBufferId,
                                0,
                                if (readSize > 0) readSize else 0,
                                extractor.sampleTime,
                                if (readSize > 0) 0 else MediaCodec.BUFFER_FLAG_END_OF_STREAM
                            )
                        }

                    }

                    val outputBufferId = codec.dequeueOutputBuffer(info, 10)
                    if (outputBufferId >= 0) {
                        val outputBuffer = codec.getOutputBuffer(outputBufferId)
                        if (outputBuffer != null) {
                            val buffer = ByteArray(outputBuffer.remaining())
                            outputBuffer.get(buffer)
                            stream.write(buffer)
                            codec.releaseOutputBuffer(outputBufferId, false)
                            emit(InspResponseLoading(stream.size() / estimateSize * 100))
                        }
                        if (info.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                            EOS = true
                        }
                    }
                }

                Log.i(
                    "WaveFormFactory",
                    "Built data in " + (System.currentTimeMillis() - startTime) + "ms"
                )
                val data = WaveFormData(
                    outFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE),
                    outFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT),
                    extractor.getTrackFormat(audioTrackIndex)
                        .getLong(MediaFormat.KEY_DURATION) / 1000,
                    stream
                )
                emit(InspResponseData(data))
            } finally {
                codec?.stop()
                codec?.release()
                extractor?.release()
            }

        }
    }
}