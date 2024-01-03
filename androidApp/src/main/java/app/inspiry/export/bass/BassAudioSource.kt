package app.inspiry.export.bass

import android.content.Context
import android.media.AudioManager
import android.media.MediaCodec
import android.media.MediaFormat
import app.inspiry.BuildConfig
import com.un4seen.bass.BASS
import com.un4seen.bass.BASSmix
import app.inspiry.core.data.OriginalAudioData
import app.inspiry.export.codec.DataSource
import java.lang.Integer.min
import java.nio.ByteBuffer

class BassAudioSource(
    private val audioData: OriginalAudioData,
    private val context: Context,
    private val onErrorHasHappened: (Exception) -> Unit,
) : DataSource {

    private val bufferInfo = MediaCodec.BufferInfo()
    private var outputAudioProperties: BassAudioProperties? = null
    private var writtenLength: Long = 0L
    private var channels: List<BassAudioInput>? = null
    private var nextBufferEndOfStream: Boolean = false
    private var totalDurationBytes: Long = 0L

    var mixChan: Int = 0

    private fun prepareBass() {
        var outSampleRateInt = 48000
        val outSampleRate = (context.getSystemService(Context.AUDIO_SERVICE) as AudioManager)
            .getProperty("android.media.property.OUTPUT_SAMPLE_RATE")
        if (outSampleRate != null) {
            try {
                outSampleRateInt = outSampleRate.toInt()
            } catch (e: NumberFormatException) {
                e.printStackTrace()
            }
        }

        BASS.BASS_SetConfig(BASS.BASS_CONFIG_ANDROID_AAUDIO, 1)
        BASS.BASS_Init(-1, outSampleRateInt, 0)
        BASS.BASS_Start()

        BASS.BASS_SetConfig(BASS.BASS_CONFIG_FLOATDSP, 1)
    }

    override fun start() {
        if (!isBassInitialized) {
            prepareBass()
            isBassInitialized = true
        }

        initializeMixer()
    }

    private fun initAudioProperties(channels: List<BassAudioInput>): BassAudioProperties {

        val info = BASS.BASS_CHANNELINFO()
        val floatVal = BASS.FloatValue()
        val properties = channels.map { it.getAudioProperties(floatVal, info) }

        var outputSampleRate = -1
        var outputBitRate: Int? = null
        var outputChannelCount = -1

        for (it in properties) {
            if (it.sampleRate > outputSampleRate) outputSampleRate = it.sampleRate
            if (it.channelCount > outputChannelCount) outputChannelCount = it.channelCount
            if (it.bitrate != null && (outputBitRate == null || it.bitrate > outputBitRate))
                outputBitRate = it.bitrate
        }

        if (outputSampleRate <= 0) {
            outputSampleRate = DEFAULT_SAMPLE_RATE
        }
        if (outputChannelCount <= 0) {
            outputChannelCount = DEFAULT_CHANNEL_COUNT
        }

        return BassAudioProperties(outputSampleRate, outputChannelCount, outputBitRate)
    }

    private fun initializeMixer() {
        val cr = context.contentResolver

        // throw if 0 channels will be created or if debug

        var channelCreationException: Exception? = null
        val channels = audioData.audioTracks.mapNotNull {
            try {
                BassAudioInput(it, cr)
            } catch (e: Exception) {
                channelCreationException = e
                null
            }
        }
        if (channelCreationException != null &&
            (channels.isEmpty() || BuildConfig.DEBUG)
        ) {

            throw channelCreationException!!
        }

        val outputAudioProperties = initAudioProperties(channels)

        this.outputAudioProperties = outputAudioProperties

        mixChan = BASSmix.BASS_Mixer_StreamCreate(
            outputAudioProperties.sampleRate,
            outputAudioProperties.channelCount,
            BASS.BASS_STREAM_DECODE
        )

        if (mixChan == 0) {
            throw BassException(BASS.BASS_ErrorGetCode())
        }

        channels.forEach {
            BASSmix.BASS_Mixer_StreamAddChannelEx(
                mixChan,
                it.chan,
                BASS.BASS_STREAM_DECODE,
                it.getViewStartBytes(),
                it.getViewLengthBytes(audioData.totalDurationUs)
            )
        }

        totalDurationBytes = BASS.BASS_ChannelSeconds2Bytes(mixChan, audioData.totalDurationUs.usToSeconds())

        this.channels = channels
    }

    override fun getMediaFormat(): MediaFormat {
        val mediaFormat = MediaFormat()
        mediaFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, outputAudioProperties!!.sampleRate)
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, outputAudioProperties!!.sampleRate)
        mediaFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, outputAudioProperties!!.channelCount)
        return mediaFormat
    }

    override fun release() {
        if (mixChan != 0)
            BASS.BASS_StreamFree(mixChan)
        channels?.forEach {
            it.release()
        }
    }

    override fun read(inputBuffer: ByteBuffer): MediaCodec.BufferInfo {
        if (nextBufferEndOfStream) {
            return bufferInfo.setEndOfStream()
        }

        val canWriteMaxBytesAmount = totalDurationBytes - writtenLength

        if (canWriteMaxBytesAmount <= 0) {
            return bufferInfo.setEndOfStream()
        }

        val initialBufferCapacity = inputBuffer.capacity()
        val requiredReadLength = min(canWriteMaxBytesAmount.toInt(), initialBufferCapacity)

        val c = BASS.BASS_ChannelGetData(
            mixChan,
            inputBuffer,
            requiredReadLength
        )

        if (c == 0) {
            // no data
            return bufferInfo.setEndOfStream()
        } else if (c == -1) {
            val errorCode = BASS.BASS_ErrorGetCode()

            if (errorCode != BASS.BASS_ERROR_ENDED) {
                onErrorHasHappened(BassException(errorCode))
            }
            return bufferInfo.setEndOfStream()
        }

        writtenLength += c

        val currentPositionSec = BASS.BASS_ChannelBytes2Seconds(mixChan, writtenLength)
        val currentTimeUs = (currentPositionSec * 1000000).toLong()

        if (currentTimeUs >= audioData.totalDurationUs) {
            nextBufferEndOfStream = true
        }

        bufferInfo.size = c
        bufferInfo.offset = 0
        bufferInfo.presentationTimeUs = currentTimeUs
        bufferInfo.flags = 0

        return bufferInfo
    }
}

fun MediaCodec.BufferInfo.setEndOfStream(): MediaCodec.BufferInfo {
    size = 0
    offset = 0
    flags = MediaCodec.BUFFER_FLAG_END_OF_STREAM
    return this
}

private const val TAG_LOG = "bass_audio"
private var isBassInitialized = false
private const val DEFAULT_SAMPLE_RATE = 44100
private const val DEFAULT_CHANNEL_COUNT = 2