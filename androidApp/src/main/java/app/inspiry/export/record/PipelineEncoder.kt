package app.inspiry.export.record

import android.media.MediaMuxer
import app.inspiry.utils.printDebug

class PipelineEncoder(
    filePath: String,
    val onFinish: () -> Unit,
    val onError: (Throwable, isCritical: Boolean) -> Unit,
    val onProgress: (Float, EncoderType) -> Unit,
    encoders: List<Encoder>
) {
    private val encoders: MutableList<Encoder> = encoders.toMutableList()

    val encodersCounts: Int
        get() = this.encoders.size

    var isReleased = false
        private set

    var currentEncoderIndex = 0
        private set

    private val currentEncoder: Encoder
        get() = encoders[currentEncoderIndex]

    private val muxerTrackIndexes = mutableMapOf<Encoder, Int>()
    private var mediaMuxer: MediaMuxer =
        MediaMuxer(filePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

    private val allFormatsInited: Boolean
        get() = encoders.all { muxerTrackIndexes.containsKey(it) }

    private var previousEncoderProgress: Float = 0f

    init {

        val iterator = this.encoders.listIterator()

        while (iterator.hasNext()) {
            val encoder = iterator.next()

            encoder.onFormatInitialized = {

                val index = mediaMuxer.addTrack(it)
                muxerTrackIndexes[encoder] = index
                checkFormatsInited()
            }

            encoder.writeSampleData = { byteBuff, info ->
                mediaMuxer.writeSampleData(muxerTrackIndexes[encoder]!!, byteBuff, info)
            }

            encoder.onError = {
                onEncoderError(encoder, it)
            }
            encoder.onFinished = ::nextStep

            encoder.onProgress = {
                onProgress(previousEncoderProgress + (encoder.progressWeight * it), encoder.type)
            }

            try {
                encoder.initialize()
            } catch (t: Throwable) {
                t.printDebug()
                encoder.release()
                if (!encoder.isCritical) {
                    iterator.remove()
                    checkFormatsInited()
                }
                onError(t, encoder.isCritical)
            }
        }
    }

    private fun onEncoderError(encoder: Encoder, it: Throwable) {
        onError(it, encoder.isCritical)
        if (!encoder.isCritical) {
            nextStep()
        }
    }

    private fun nextStep() {
        val next = currentEncoderIndex < encoders.size - 1

        if (next) {
            previousEncoderProgress += currentEncoder.progressWeight
            currentEncoderIndex++
            if (currentEncoder.isRelease()) {
                // this encoder was released already
                nextStep()
            } else {
                currentEncoder.canWrite = true
            }
        } else {
            release()
            onFinish()
        }
    }

    private fun checkFormatsInited() {
        if (allFormatsInited) {
            mediaMuxer.start()
            currentEncoder.canWrite = true
        }
    }

    fun release() {
        if (isReleased) return

        isReleased = true
        encoders.forEach {
            it.release()
        }
        try {
            mediaMuxer.release()
        } catch (ignored: IllegalStateException) {
        }
    }
}