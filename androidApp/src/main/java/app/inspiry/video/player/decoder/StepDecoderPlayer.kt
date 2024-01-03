package app.inspiry.video.player.decoder

import android.content.Context
import android.graphics.SurfaceTexture
import android.media.MediaFormat
import android.os.Handler
import android.view.Surface
import app.inspiry.core.opengl.PlayerParams
import app.inspiry.core.opengl.VideoPlayerParams
import java.nio.ByteBuffer
import kotlin.math.max

/**
 * Used for synchronous step-by-step playback from sourceUri and display on surfaceTexture
 *
 * @param onFrameSkipped(seekFinished: Boolean)
 */
class StepDecoderPlayer(
    context: Context,
    sourceUri: String,
    surfaceTexture: SurfaceTexture,
    surface: Surface,
    params: VideoPlayerParams,
    handler: Handler,
    val onFrameSkipped: (Boolean) -> Unit, val onPlayerFailed: (Exception) -> Unit
) : StepPlayer<VideoPlayerParams>(sourceUri, params), DecoderCallbacks {

    init {
        debug { "init StepDecoderPlayer ${params}" }
    }

    private val extractor = VideoExtractor(context, sourceUri)
    private val decoder =
        VideoDecoder(surfaceTexture, surface, extractor.mediaFormat, logTag, handler, this)

    override val videoDurationUs: Long = extractor.mediaFormat.getLong(MediaFormat.KEY_DURATION)

    private val viewStartTimeUs: Long
        get() = params.viewStartTimeUs

    //supposed to be duration of the view
    private val totalDurationUs: Long
        get() = params.totalDurationUs

    private val videoStartTimeUs
        get() = params.videoStartTimeUs

    override val doLoop: Boolean
        get() = params.isLoopEnabled

    private val isDecodeCompleted: Boolean
        get() = decoder.isOutputCompleted

    override val videoSize = extractor.videoSize

    override val isCompleted: Boolean
        get() = isReachedRightBound

    private val isReachedRightBound: Boolean
        get() = ((currentTimeUs - videoStartTimeUs) >= totalDurationUs && totalDurationUs > 0L) || isDecodeCompleted

    private val frameInfo = FrameInfo()
    private var expectedTimeUs: Long = 0
    private var lastRenderedKeyFrameTime = 0L

    init {
        debug { "init videoStartTime ${(this.params.videoStartTimeUs) / 1000}" }
        if (videoStartTimeUs != 0L)
            extractor.seekTo(videoStartTimeUs)
    }

    var loopTimeOffset: Long = 0

    //TODO:
    // 1. we need to render the first frame at all conditions
    // 2. also reset frame if we seek to left side.

    override fun drawFrame(expectedTimeUs: Long, sequential: Boolean) {

        val isVideoOffsetPlayed = expectedTimeUs < viewStartTimeUs

        var newExpectedTimeUs = expectedTimeUs
        if (doLoop && !isVideoOffsetPlayed) {

            val newLoopTimeOffset: Long
            if (!sequential) {
                val videoDurationUs = videoDurationUs - videoStartTimeUs

                loopTimeOffset =
                    ((expectedTimeUs - viewStartTimeUs) / videoDurationUs) * videoDurationUs
            } else {
                if (isReachedRightBound) {

                    newLoopTimeOffset = loopTimeOffset + (frameInfo.timeUs - videoStartTimeUs)

                    if (newLoopTimeOffset <= newExpectedTimeUs) {
                        restart()
                        loopTimeOffset = newLoopTimeOffset
                    }

                    debug {
                        "isReachedRightBound newLoopTimeOffset ${newLoopTimeOffset / 1000}, " +
                                "lastFrame ${frameInfo.timeUs / 1000}, expectedTime ${newExpectedTimeUs / 1000}"
                    }
                }
            }

            if (newExpectedTimeUs < loopTimeOffset) {
                loopTimeOffset = 0L
            } else {
                newExpectedTimeUs -= loopTimeOffset
            }
        }

        this.expectedTimeUs = newExpectedTimeUs

        debug {
            "drawFrame currentTime ${currentTimeUs / 1000}, expectedTime ${this.expectedTimeUs / 1000} " +
                    "isExtractorCompleted ${extractor.isCompleted}, " +
                    "isReachedRightBound ${isReachedRightBound}, isVideoOffsetPlayed ${isVideoOffsetPlayed}"
        }

        if (isReachedRightBound && sequential) {
            onFrameSkipped(false)
        } else if (isVideoOffsetPlayed && !(!sequential && currentTimeUs >= 50L)) {
            currentTimeUs = 0L
            onFrameSkipped(false)
        } else {

            fun drawFrameIfNeeded() {
                if (isNextFrameEnabled() && ((currentTimeUs - videoStartTimeUs) <= max(newExpectedTimeUs - viewStartTimeUs, 0L))) {
                    decoder.prepareInputBuffer()
                    decoder.prepareOutputBuffer()
                }
            }

            //don't do anything because we seek outside the video
            if (!sequential && !isVideoOffsetPlayed && ((currentTimeUs != 0L && newExpectedTimeUs - viewStartTimeUs > videoDurationUs - videoStartTimeUs))
            ) {
                debug {
                    "drawFrame expectedTime ${newExpectedTimeUs / 1000} currentTime ${currentTimeUs / 1000L} videoDuration ${videoDurationUs / 1000}, decodeCompleted ${isDecodeCompleted}"
                }
                onFrameSkipped(true)
                return
            }


            if (!sequential) {

                val newKeyFrameTime =
                    extractor.neededKeyFrame(max(newExpectedTimeUs + videoStartTimeUs - viewStartTimeUs, 0L))

                debug {
                    "drawFrame seekTo keyInterval ${extractor.keyFrameIntervalUs / 1000} prev ${lastRenderedKeyFrameTime / 1000}, newTime ${newKeyFrameTime / 1000}," +
                            " expectedTime ${newExpectedTimeUs / 1000} currentTime ${currentTimeUs / 1000L}"
                }

                if (lastRenderedKeyFrameTime != newKeyFrameTime || (isVideoOffsetPlayed && currentTimeUs >= 50L)) {
                    lastRenderedKeyFrameTime = newKeyFrameTime
                    extractor.seekTo(newKeyFrameTime)
                    currentTimeUs = 0L
                    decoder.reset()
                } else {
                    drawFrameIfNeeded()
                }

            } else {
                drawFrameIfNeeded()
            }
        }
    }


    override fun isNextFrameEnabled() = !isDecodeCompleted


    override fun restart() {
        super.restart()

        debug {
            "restart, totalDuration = ${params.totalDurationUs}, videoDuration = ${videoDurationUs}, isCompleted ${isDecodeCompleted}"
        }

        loopTimeOffset = 0L
        currentTimeUs = 0L
        expectedTimeUs = 0L
        extractor.seekTo(videoStartTimeUs)
        decoder.reset()
    }

    override fun release() {
        debug { "release isReachedRightBound ${isReachedRightBound} isDecodeCompleted ${isDecodeCompleted} extractorCompleted ${extractor.isCompleted}" }
        extractor.release()
        decoder.release()
    }

    override fun setParams(params: PlayerParams): Boolean {
        if (super.setParams(params)) {
            debug { "set params call, $params" }
            restart()
            return true
        }
        return false
    }

    override fun onInputBufferAvailable(inputBuffer: ByteBuffer): FrameInfo {

        val extractorTime = extractor.currentSampleTime()
        var extractorFinished =
            (extractorTime - videoStartTimeUs > totalDurationUs && totalDurationUs > 0) || extractor.isCompleted

        if (extractorFinished) {
            debug { "onInputBufferAvailable extractorTime ${extractorTime / 1000} finished ${extractorFinished}" }

        } else {
            extractorFinished = !extractor.read(inputBuffer, frameInfo)
            debug { "onInputBufferAvailable extractorTime ${extractorTime / 1000} finished ${extractorFinished}" }
        }
        if (extractorFinished) {
            frameInfo.size = 0
        }
        return frameInfo
    }

    override fun onOutputAvailable(currentTimeUs: Long, isKeyFrame: Boolean): Boolean {
        val needToProcess =
            (currentTimeUs - videoStartTimeUs) <= max(expectedTimeUs - viewStartTimeUs, 0L)

        debug {
            "onOutputAvailable needToProcess ${needToProcess} " +
                    " isDecodeCompleted ${isDecodeCompleted} isKeyFrame ${isKeyFrame}" +
                    " currentTime ${currentTimeUs / 1000}, " +
                    "expectedTime ${expectedTimeUs / 1000}, videoDuration = ${videoDurationUs / 1000}"
        }

        if ((!needToProcess || isDecodeCompleted)) {
            onFrameSkipped(true)
        } else {
            this.currentTimeUs = currentTimeUs
            if (isKeyFrame)
                this.lastRenderedKeyFrameTime = currentTimeUs
        }

        return needToProcess
    }

    override fun onOutputRendered() {

        val processNextFrame = isNextFrameEnabled() && !isReachedRightBound &&
                (currentTimeUs - videoStartTimeUs) <= max(expectedTimeUs - viewStartTimeUs, 0L)

        debug { "onOutputRendered processNextFrame ${processNextFrame}, currentTime ${currentTimeUs / 1000}, expectedTime ${expectedTimeUs / 1000}" }

        if (processNextFrame) {
            decoder.prepareOutputBuffer()
        }
    }

    override fun onFailure(e: Exception) {
        onPlayerFailed(e)
    }

    fun hasAudioTrack(): Boolean {
        return extractor.hasAudioTrack
    }
}