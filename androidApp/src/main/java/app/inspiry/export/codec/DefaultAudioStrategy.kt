package app.inspiry.export.codec

import android.media.MediaCodecInfo
import android.media.MediaFormat

/**
 * @param setMaxInputSize is necessary when using mediaExtractor. Because
 * some sources can write more data than capacity of inputBuffer.
 */
class DefaultAudioStrategy(val setMaxInputSize: Boolean = false): TrackStrategy {

    override fun createTrackStrategy(input: List<MediaFormat>): MediaFormat {

        val channelCount = input.maxOfOrNull { it.getInteger(MediaFormat.KEY_CHANNEL_COUNT) } ?: DEFAULT_CHANNEL_COUNT

        var bitrate = input.maxOfOrNull { it.getInteger(MediaFormat.KEY_BIT_RATE) } ?: DEFAULT_BIT_RATE
        if (bitrate > MAX_ENCODE_BITRATE) {
            bitrate = MAX_ENCODE_BITRATE
        }

        val outputMimeType = MediaFormat.MIMETYPE_AUDIO_AAC

        val sampleRate = input.maxOfOrNull { it.getInteger(MediaFormat.KEY_SAMPLE_RATE) } ?: DEFAULT_SAMPLE_RATE

        val outputFormat = MediaFormat.createAudioFormat(
            outputMimeType, sampleRate,
            channelCount
        )
        if (setMaxInputSize)
            outputFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 1048576)

        outputFormat.setInteger(
            MediaFormat.KEY_AAC_PROFILE,
            MediaCodecInfo.CodecProfileLevel.AACObjectLC
        )
        outputFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate)

        return outputFormat
    }

    companion object {
        const val DEFAULT_BIT_RATE = 256000
        const val MAX_ENCODE_BITRATE = 320000
        const val DEFAULT_CHANNEL_COUNT = 2
        const val DEFAULT_SAMPLE_RATE = 44100
    }
}