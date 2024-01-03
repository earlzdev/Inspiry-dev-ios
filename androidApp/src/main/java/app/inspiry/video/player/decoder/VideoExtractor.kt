package app.inspiry.video.player.decoder

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.os.Build
import app.inspiry.helpers.K
import app.inspiry.core.util.getFileNameWithParent
import app.inspiry.video.SourceUtils
import app.inspiry.video.parseAssetsPathForAndroid
import java.nio.ByteBuffer

/**
 * Used to read video data from uri file
 */
class VideoExtractor(context: Context, uri: String) {

    private val logTag = uri.getFileNameWithParent()
    private val extractor: MediaExtractor
    val mediaFormat: MediaFormat
    val videoSize: TextureSize
    private val mimeType: String
    private var isFirstFrame = true
    var currentKeyFrameTime: Long = 0L
        private set

    var keyFrameIntervalUs: Long

    /**
     * If true - the file has reached the end
     */
    var isCompleted = false
        private set

    var hasAudioTrack: Boolean = false

    init {
        extractor = createVideoExtractor(context, uri)
        mediaFormat = extractor.selectTrackWithVideoFormat()
            .apply {
                debug { text() }
                videoSize = getDisplayedSize()
                mimeType = getMimeType()!!
                keyFrameIntervalUs = keyFrameIntervalUs()
            }
    }

    private fun estimateKeyFrameInterval() {
        K.debugTime(logTag, { "time spend on estimateKeyFrameInterval %d" }) {
            val current = extractor.sampleTime

            extractor.seekTo(0L, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
            val previous = extractor.sampleTime

            extractor.seekTo(100L, MediaExtractor.SEEK_TO_NEXT_SYNC)
            val next = extractor.sampleTime

            isFirstFrame = true

            //return to the old seek
            if (current > 0) {
                extractor.seekTo(current, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
            }

            keyFrameIntervalUs = next - previous
        }
    }

    fun neededKeyFrame(timeUs: Long): Long {
        if (keyFrameIntervalUs == -1L) {
            estimateKeyFrameInterval()
            debug { "estimateKeyFrameInterval ${keyFrameIntervalUs / 1000}" }
        }

        if (keyFrameIntervalUs != 0L) {
            return (timeUs / keyFrameIntervalUs) * keyFrameIntervalUs
        }
        return 0L
    }

    private fun createVideoExtractor(context: Context, path: String) = MediaExtractor()
        .apply {
            val assetFileDescriptor = SourceUtils.getAssetFileDescriptor(path, context)

            if (assetFileDescriptor != null) {
                setDataSource(assetFileDescriptor.fileDescriptor,
                    assetFileDescriptor.startOffset, assetFileDescriptor.length)
            } else setDataSource(context, Uri.parse(path.parseAssetsPathForAndroid()), null)
        }

    private fun MediaExtractor.selectTrackWithVideoFormat(): MediaFormat {
        var foundVideoFormat: MediaFormat? = null
        for (trackId in 0 until trackCount) {
            val format = getTrackFormat(trackId)
            if (foundVideoFormat == null && format.isVideo()) {
                selectTrack(trackId)
                foundVideoFormat = format
            } else if (!hasAudioTrack && format.isAudio()) {
                hasAudioTrack = true
            }
        }
        debug { "selectTrackWithVideoFormat hasAudioTrack ${hasAudioTrack}" }
        return foundVideoFormat ?: throw DecoderException("No video track found")
    }

    private fun MediaFormat.isVideo() =
        getMimeType()?.startsWith("video/") == true

    private fun MediaFormat.isAudio() =
        getMimeType()?.startsWith("audio/") == true

    private fun MediaFormat.getDisplayedSize(): TextureSize {
        var width = getInteger(MediaFormat.KEY_WIDTH)
        if (containsKey("crop-left") && containsKey("crop-right")) {
            width = getInteger("crop-right") + 1 - getInteger("crop-left");
        }
        var height = getInteger(MediaFormat.KEY_HEIGHT);
        if (containsKey("crop-top") && containsKey("crop-bottom")) {
            height = getInteger("crop-bottom") + 1 - getInteger("crop-top");
        }
        val rotation = try {
            getInteger(MediaFormat.KEY_ROTATION)
        } catch (e: Exception) {
            0
        }

        debug { "sourceFormat: width = $width, height = $height" }
        return TextureSize(width, height, rotation.toFloat())
    }

    //returns always 0
    private fun MediaFormat.keyFrameIntervalUs(): Long {

        if (containsKey(MediaFormat.KEY_I_FRAME_INTERVAL)) {
            if (Build.VERSION.SDK_INT >= 25) {
                return (getFloat(MediaFormat.KEY_I_FRAME_INTERVAL) * 1000000).toLong()
            } else return getInteger(MediaFormat.KEY_I_FRAME_INTERVAL) * 1000000L
        }
        return -1L
    }

    fun read(buffer: ByteBuffer, frameInfo: FrameInfo): Boolean {
        if (isCompleted) return false
        if (isFirstFrame) {
            isFirstFrame = false
            currentKeyFrameTime = extractor.sampleTime
        } else {
            extractor.advance()
        }
        val size = extractor.readSampleData(buffer, 0)
        if (size >= 0) {
            buffer.limit(size)
            val timeUs = extractor.sampleTime

            if (extractor.sampleFlags == MediaExtractor.SAMPLE_FLAG_SYNC)
                currentKeyFrameTime = timeUs

            frameInfo.setValues(size, timeUs, extractor.sampleFlags, buffer)
            verbose { frameInfo.toString() }
            return true
        }
        isCompleted = true
        debug { "end of data" }
        return false
    }

    fun currentSampleTime() = extractor.sampleTime

    fun seekTo(timeUs: Long) {
        debug { "seekTo ${timeUs / 1000}, current ${extractor.sampleTime / 1000}" }
        extractor.seekTo(timeUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
        isCompleted = false
        isFirstFrame = true
    }

    fun release() {
        debug { "release" }
        extractor.release()
    }

    private inline fun debug(msg: () -> String) {
        K.d(K.TAG_VIDEO_EXTRACTOR) { "$logTag ${msg()}" }
    }

    private inline fun verbose(msg: () -> String) {
        K.v(K.TAG_VIDEO_EXTRACTOR) { "$logTag ${msg()}" }
    }
}

fun MediaFormat.text(): String {
    val builder = StringBuilder()

    fun add(key: String, block: ((key: String) -> String?)? = null) = apply {
        if (containsKey(key)) {
            val value = if (block != null) block(key)
            else getInteger(key).toString()
            builder.append('\n')
                .append(key)
                .append(" - ")
                .append(value)
        }
    }

    add(MediaFormat.KEY_WIDTH)
    add(MediaFormat.KEY_HEIGHT)
    add(MediaFormat.KEY_MIME) { getString(it) }
    add(MediaFormat.KEY_COLOR_FORMAT)
    add(MediaFormat.KEY_ROTATION)
    add(MediaFormat.KEY_FRAME_RATE)
    add(MediaFormat.KEY_BITRATE_MODE)
    add(MediaFormat.KEY_I_FRAME_INTERVAL)

    return builder.toString()
}

fun MediaFormat.getMimeType(): String? =
    getString(MediaFormat.KEY_MIME)