package app.inspiry.video.player.decoder

import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Build
import android.os.Handler
import android.view.Surface
import app.inspiry.helpers.K
import app.inspiry.utils.printDebug
import java.nio.ByteBuffer
import java.util.*

/**
 * Used to decode video data
 */
class VideoDecoder(
    private val surfaceTexture: SurfaceTexture,
    private val surface: Surface,
    private var mediaFormat: MediaFormat,
    private val logTag: String,
    private val handler: Handler,
    private val callbacks: DecoderCallbacks
) : MediaCodec.Callback() {

    private var decoder = createAndStartDecoder(surface)
    private var hasSyncFrame = false
    private var inputBufferIds = LinkedList<Int>()

    //bufferIndex, timeUs, flags
    private var outputBufferIds = LinkedList<Triple<Int, Long, Int>>()
    private var isInputCompleted = false
    private var lastFrameTimeUs: Long = -1L
    private var released: Boolean = false

    var isOutputCompleted = false
        private set

    var waitForOutput: Boolean = false

    private fun createAndStartDecoder(surface: Surface?): MediaCodec {

        val mimeType = mediaFormat.getMimeType()!!
        fun create() = MediaCodec.createDecoderByType(mimeType)
            .apply {
                configure(mediaFormat, surface, null, 0)
                debug { outputFormat.text() }
                debug { "maxSupportedInstances ${codecInfo.getCapabilitiesForType(mimeType).maxSupportedInstances}" }
                setCallback(this@VideoDecoder, handler)
                start()
            }

        try {
            return create()
        } catch (e: IllegalStateException) {
            debug { "was not able to create MediaCodec initially. Try again after 500ms, error ${e.message}" }
            Thread.sleep(500L)
            return create()
        }
    }


    private fun releaseOutputBuffer(isRender: Boolean = true): Boolean {
        var hadException = true

        while (hadException && outputBufferIds.isNotEmpty()) {
            try {
                val (outputBufferId, currentTimeUs, flags) = outputBufferIds.first
                val isDecodeCompleted = flags.isDecodeCompleted()

                if (isDecodeCompleted)
                    this.isOutputCompleted = isDecodeCompleted

                if (isDecodeCompleted) {

                    val reachedEndOfData =
                        lastFrameTimeUs != -1L && currentTimeUs >= lastFrameTimeUs

                    if (isDecodeCompleted || reachedEndOfData) {
                        debug {
                            "send end of stream. isDecodeCompleted ${isDecodeCompleted}," +
                                    " reachedAndOfData ${reachedEndOfData}, lastFrameTime ${lastFrameTimeUs / 1000}"
                        }
                    }
                }

                //TODO: call it?
                val needToProcess = callbacks.onOutputAvailable(currentTimeUs, flags.isKeyFrame())
                if (needToProcess || isDecodeCompleted) {
                    outputBufferIds.removeFirst()
                    if (isRender && surface.isValid) decoder.releaseOutputBuffer(outputBufferId, currentTimeUs * 1000)
                    else decoder.releaseOutputBuffer(outputBufferId, false)
                    hadException = false

                } else {
                    break
                }
            } catch (e: Exception) {
                e.printDebug()
                hadException = true
            }
        }

        return !hadException
    }

    fun prepareOutputBuffer() {

        if (outputBufferIds.isEmpty()) {
            waitForOutput = true
        } else {
            waitForOutput = false

            val outputRendered = releaseOutputBuffer(true)
            if (outputRendered) {
                callbacks.onOutputRendered()
            }
        }
    }


    /**
     * The main method for decoding
     */
    fun prepareInputBuffer(): Boolean {
        var processed = false
        while (inputBufferIds.isNotEmpty()) {
            val res = feedInputBuffer()
            if (!res)
                break
            processed = res || processed
        }
        return processed
    }


    private fun feedInputBuffer(): Boolean {
        //take inputBuffers until valid. Sometimes we get an exception:
        //getBufferAndFormat - invalid operation (the index 2 is not owned by client)
        var inputBufferId: Int = -1
        var newInputBuffer: ByteBuffer? = null

        while (inputBufferIds.isNotEmpty() && newInputBuffer == null) {
            inputBufferId = inputBufferIds.first
            newInputBuffer = try {
                decoder.getInputBuffer(inputBufferId)
            } catch (e: java.lang.IllegalStateException) {
                e.printDebug()
                inputBufferIds.removeFirst()
                null
            }
        }
        if (newInputBuffer == null) return false

        val frameInfo = callbacks.onInputBufferAvailable(newInputBuffer)

        if ((!hasSyncFrame && frameInfo.flags != MediaExtractor.SAMPLE_FLAG_SYNC)) {
            return false
        } else {
            hasSyncFrame = true
            val inputFed = setupInputData(frameInfo, inputBufferId)

            inputBufferIds.removeFirst()

            return inputFed
        }
    }

    private fun setupInputData(dataInfo: FrameInfo, inputBufferId: Int): Boolean {
        if (dataInfo.size <= 0) {
            decoder.queueInputBuffer(inputBufferId, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
            isInputCompleted = true
            lastFrameTimeUs = dataInfo.timeUs
            return false
        } else {

            val mediaCodecFlags =
                if (dataInfo.flags == MediaExtractor.SAMPLE_FLAG_SYNC)
                    MediaCodec.BUFFER_FLAG_KEY_FRAME
                else if (dataInfo.flags == MediaExtractor.SAMPLE_FLAG_PARTIAL_FRAME && Build.VERSION.SDK_INT >= 26)
                    MediaCodec.BUFFER_FLAG_PARTIAL_FRAME
                else 0

            decoder.queueInputBuffer(inputBufferId, 0, dataInfo.size, dataInfo.timeUs, mediaCodecFlags)
            return true
        }
    }

    private fun MediaCodec.BufferInfo.text() = StringBuilder()
        .append("flags - ").append(flags).append(", ")
        .append("offset - ").append(offset).append(", ")
        .append("presentationTimeUs - ").append(presentationTimeUs).append(", ")
        .append("size - ").append(size)
        .toString()

    private fun Int.isDecodeCompleted() =
        (this and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0

    private fun Int.isKeyFrame() =
        (this and MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0


    private fun isDrawEnabled() = surface.isValid && hasSyncFrame

    /**
     * Reset the current state of the decoder.
     */

    var pendingFlush = 0
    fun reset() {
        debug { "reset" }
        outputBufferIds.clear()
        inputBufferIds.clear()
        isInputCompleted = false
        isOutputCompleted = false
        hasSyncFrame = false
        lastFrameTimeUs = -1L
        debug { "flush, pendingFlush ${pendingFlush}" }
        decoder.flush()
        pendingFlush++

        handler.postDelayed({
            pendingFlush--

            debug { "flush, pendingFlushRemove ${pendingFlush}" }
            if (pendingFlush == 0 && !released) {

                isInputCompleted = false
                isOutputCompleted = false
                hasSyncFrame = false
                lastFrameTimeUs = -1L

                decoder.start()
            }

        }, 1L)
    }

    fun release() {
        debug { "release" }
        released = true
        outputBufferIds.clear()
        inputBufferIds.clear()
        decoder.release()
        surface.release()
        surfaceTexture.release()
    }

    private inline fun debug(msg: () -> String) {
        K.d(K.TAG_VIDEO_DECODER) { "$logTag ${msg()}" }
    }

    private inline fun verbose(msg: () -> String) {
        K.v(K.TAG_VIDEO_DECODER) { "$logTag ${msg()}" }
    }

    override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
        //debug { "onInputBufferAvailable $index" }

        if (pendingFlush > 0) {
            debug { "onInputBufferAvailable while flush $index, pending $pendingFlush" }
        }
        try {
            if (index >= 0 && pendingFlush == 0) {
                inputBufferIds.add(index)

                if (!isOutputCompleted && !isInputCompleted)
                    prepareInputBuffer()
            }
        } catch (e: Exception) {
            callbacks.onFailure(e)
        }

    }


    override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
        //debug { "onOutputBufferAvailable $index" }
        try {
            //has syncFrame check is needed
            if (index >= 0 && hasSyncFrame && pendingFlush == 0) {
                outputBufferIds.add(Triple(index, info.presentationTimeUs, info.flags))

                if (!isOutputCompleted) {
                    prepareOutputBuffer()
                }
            }
        } catch (e: Exception) {
            callbacks.onFailure(e)
        }
    }

    override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
        callbacks.onFailure(e)
    }

    override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
        mediaFormat = format
        debug { "onOutputFormatChanged" }
    }
}

interface DecoderCallbacks {
    fun onInputBufferAvailable(inputBuffer: ByteBuffer): FrameInfo

    //return true if need to render it
    fun onOutputAvailable(currentTimeUs: Long, isKeyFrame: Boolean): Boolean
    fun onOutputRendered()
    fun onFailure(e: Exception)
}