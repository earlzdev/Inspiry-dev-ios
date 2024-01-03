package app.inspiry.export.record

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaFormat
import android.os.Handler
import android.view.Surface
import app.inspiry.core.log.KLogger
import org.json.JSONObject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import java.util.*


/**
 * Buffers can be in two modes: write, read. And have 3 properties: position, capacity, limit
 */

/**
 * Stages:
 * 1. Draw canvas to bitmap
 * 2. Take bitmap pixels and convert them to YUV
 * 3. Write bitmap pixels as a frame to MediaCodec
 * 4. Take mediaCodec and write to mediaMuxer to receive file
 */

class VideoEncoder(
    val width: Int,
    val height: Int,
    private val frameRate: Int,
    durationUs: Long,
    handler: Handler, progressWeight: Float,
) : KoinComponent, Encoder(handler, durationUs, progressWeight) {

    private var videoCodec: MediaCodec? = null

    var surface: Surface? = null

    private var currentFrame = 0

    private val pendingVEncoderInfos = LinkedList<MediaCodec.BufferInfo>()
    private val pendingVEncoderIndices = LinkedList<Int>()

    val logger: KLogger by inject {
        parametersOf("video-encoder")
    }

    private fun createVideoFormat(mimeType: String, desiredColorFormat: Int): MediaFormat {
        val mediaFormat =
            MediaFormat.createVideoFormat(mimeType, width, height)
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, ENCODING_VIDEO_BITRATE)
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, this.frameRate)
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, desiredColorFormat)

        return mediaFormat
    }

    private fun findCorrectVideoFormat(): MediaFormat {

        val mimeType = POSSIBLE_MIME_TYPES[0]
        val desiredColorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
        val mediaFormat = createVideoFormat(mimeType, desiredColorFormat)

        val encoderForFormat =
            MediaCodecList(MediaCodecList.REGULAR_CODECS).findEncoderForFormat(mediaFormat)

        if (encoderForFormat == null) {
            logger.info { "encoderForFormatIsNull!!! width = $width, height = $height" }

            videoCodec = MediaCodec.createEncoderByType(mimeType)
        } else {
            videoCodec = MediaCodec.createByCodecName(encoderForFormat)
        }
        val codecInfo = videoCodec!!.codecInfo

        if (codecInfo.isEncoder && codecInfo.supportedTypes.contains(mimeType) &&
            codecInfo.getCapabilitiesForType(mimeType).colorFormats
                .contains(desiredColorFormat)
        ) {

        } else {
            throw IllegalStateException("MediaCodec is wrong = ${codecInfo}")
        }

        val errorMessage = checkIsColorFormatSupported(mediaFormat, desiredColorFormat, mimeType)
        if (errorMessage != null)
            throw IllegalStateException(errorMessage)

        return mediaFormat
    }

    //return error message if false
    private fun checkIsColorFormatSupported(
        mediaFormat: MediaFormat,
        desiredColorFormat: Int,
        mimeType: String
    ): String? {
        var colorFormats = videoCodec!!.codecInfo.getCapabilitiesForType(mimeType).colorFormats
        var colorFormatSize = colorFormats.size
        var counterColorFormat = 0
        val colorFormatCorrect: Boolean
        while (true) {
            if (counterColorFormat >= colorFormatSize) {
                colorFormatCorrect = false
                break
            }
            if (colorFormats[counterColorFormat] == desiredColorFormat) {
                colorFormatCorrect = true
                break
            }
            ++counterColorFormat
        }

        if (!colorFormatCorrect) {
            var message = "NO COLOR FORMAT COMPATIBLE\\n$mediaFormat"
            colorFormats = videoCodec!!.codecInfo.getCapabilitiesForType(mimeType).colorFormats
            colorFormatSize = colorFormats.size
            counterColorFormat = 0
            while (counterColorFormat < colorFormatSize) {
                val sb = StringBuilder()
                sb.append(message)
                sb.append("\\n")
                sb.append(colorFormats[counterColorFormat])
                message = sb.toString()
                logger.debug { message }
                ++counterColorFormat
            }
            return message
        }

        return null

    }

    private fun printVideoCodecInfo() {
        logger.debug {
            val json = JSONObject()
            json.put("codec_name", videoCodec!!.name)
            json.put("codec_info_name", videoCodec!!.codecInfo.name)
            json.put("codec_supported_types", videoCodec!!.codecInfo.supportedTypes)
            json.put("output_width", width)
            json.put("output_height", height)
            json.toString()
        }
    }

    @Throws(Exception::class)
    override fun initialize() {

        val mediaFormat = findCorrectVideoFormat()

        printVideoCodecInfo()

        videoCodec!!.setCallback(object : MediaCodec.Callback() {
            override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {

            }

            override fun onOutputBufferAvailable(
                codec: MediaCodec,
                index: Int,
                info: MediaCodec.BufferInfo
            ) {

                pendingVEncoderIndices.add(index)
                pendingVEncoderInfos.add(info)

                if (canWrite)
                    checkOutputAvailable()
            }

            override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
                onErrorHasHappened(e)
            }

            override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
                onFormatInitialized?.invoke(format)
            }

        }, handler)

        videoCodec!!.configure(
            mediaFormat, null, null,
            MediaCodec.CONFIGURE_FLAG_ENCODE
        )

        surface = videoCodec!!.createInputSurface()

        startVideoCodec()
    }

    override val type: EncoderType
        get() = EncoderType.VIDEO

    override val isCritical: Boolean
        get() = true

    override fun checkOutputAvailable() {
        while (pendingVEncoderIndices.size > 0 &&
            pendingVEncoderInfos.size > 0 && videoCodec != null
        ) {

            val index = pendingVEncoderIndices.removeFirst()
            val info = pendingVEncoderInfos.removeFirst()

            onVideoOutputAvailable(videoCodec!!, index, info)
        }
    }

    private fun onVideoOutputAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {

        if (videoCodec == null)
            return

        try {
            if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {

                codec.releaseOutputBuffer(index, false)
                onWritingFinished()

            } else {
                val outputBuffer = codec.getOutputBuffer(index)!!

                outputBuffer.position(info.offset)
                outputBuffer.limit(info.offset + info.size)

                info.presentationTimeUs = (currentFrame * 1000000L) / frameRate

                if (info.flags != MediaCodec.BUFFER_FLAG_CODEC_CONFIG) {
                    currentFrame++
                }

                logger.info {
                    "videoOutputAvailable time ${info.presentationTimeUs}, flags ${info.flags}," +
                            " size ${info.size}, offset ${info.offset}"
                }

                writeSampleData?.invoke(outputBuffer, info)

                codec.releaseOutputBuffer(index, false)
            }
        } catch (e: Throwable) {
            onErrorHasHappened(e)
        }
    }

    private fun startVideoCodec() {
        videoCodec?.start()
    }

    override fun _releaseInner() {
        super._releaseInner()

        pendingVEncoderInfos.clear()
        pendingVEncoderIndices.clear()
        surface?.release()
        surface = null

        if (videoCodec != null) {
            try {
                videoCodec?.stop()
            } catch (e: IllegalStateException) {
            } finally {
                videoCodec?.release()
                videoCodec = null
            }
        }
    }

    override fun isRelease(): Boolean {
        return videoCodec == null
    }

    fun sendEndOfStreamSurface() {
        try {
            videoCodec?.signalEndOfInputStream()
        } catch (ignored: IllegalStateException) {

        }
    }

    companion object {
        const val ENCODING_VIDEO_BITRATE = 12000000
        val POSSIBLE_MIME_TYPES = arrayOf("video/avc", "video/hevc", "video/x-vnd.on2.vp8")
    }
}