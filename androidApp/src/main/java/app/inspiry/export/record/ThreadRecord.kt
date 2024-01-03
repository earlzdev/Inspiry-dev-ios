package app.inspiry.export.record

import android.graphics.Canvas
import android.media.MediaMetadataRetriever
import android.media.MediaMetadataRetriever.METADATA_KEY_DURATION
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.view.View
import app.inspiry.core.data.FPS
import app.inspiry.core.data.frameToTimeUs
import app.inspiry.core.log.KLogger
import app.inspiry.export.audio.AudioEncoderFactory
import app.inspiry.helpers.K
import app.inspiry.projectutils.BuildConfig
import app.inspiry.export.audio.AudioEncoderFactoryImpl
import app.inspiry.utils.printDebug
import app.inspiry.views.template.InspTemplateView
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import java.io.File

class ThreadRecord(
    val templateView: InspTemplateView,
    val recordListener: RecordListener,
    val recordToFile: File,
    val templateAndroidView: View
) : HandlerThread("record"), KoinComponent {

    private var encoder: PipelineEncoder? = null

    // this is not the best architecture. ThreadRecord shouldn't access videoEncoder directly. Instead it is better to create
    // smth like Source which controls reading from templateView and writing to surface.

    private var videoEncoder: VideoEncoder? = null

    private var startTime = 0L

    @Volatile
    private var glLocksToWait = 0
    private var maxFrames = 0

    private val logger: KLogger by inject {
        parametersOf("ThreadRecord")
    }

    private val handler by lazy { createHandler(looper) }
    private fun createHandler(looper: Looper) = object : Handler(looper) {

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                WHAT_START -> {
                    startRecording()
                }
                WHAT_FRAME_RECORDED -> {
                    onFrameRecorded()
                }
                WHAT_GL_DRAWN -> {
                    onGlDrawn()
                }
                WHAT_GL_DRAWN_TIMEDOUT -> {
                    if (BuildConfig.DEBUG) {
                        if (THROW_ERROR_IF_GL_TIMEOUT) {
                            throw IllegalStateException("gl draw timed out, waited $glLocksToWait")
                        } else {
                            logger.warning { "Gl draw is timed out! There's a bug somewhere." }
                        }
                    } else
                        onGlDrawn()
                }
            }
        }
    }

    private fun onGlDrawn() {
        templateView.post {
            drawTemplateOnCanvas()
        }
    }

    override fun start() {
        super.start()
        handler.sendEmptyMessage(WHAT_START)
    }

    private fun drawTemplateOnCanvas() {
        val surface = videoEncoder?.surface

        if (surface == null || !surface.isValid) {
            if (!isInterrupted && encoder?.isReleased == false) {
                onError(
                    NullPointerException(
                        "encoder.surface is ${if (surface == null) "null" else "invalid"}. " +
                                "videoEncoder.isReleased: ${videoEncoder?.isRelease()}, pipeline.isReleased ${encoder?.isReleased}. encoders.size ${encoder?.encodersCounts}, " +
                                "step ${encoder?.currentEncoderIndex}, currentFrame ${templateView.currentFrame}, maxFrames ${templateView.maxFrames}"
                    ), isCritical = true
                )
            }
        } else {
            var canvas: Canvas? = null
            try {
                canvas = surface.lockHardwareCanvas()
                templateAndroidView.draw(canvas)
            } catch (t: Throwable) {
                onError(t, isCritical = true)

            } finally {
                if (canvas != null) {
                    surface.unlockCanvasAndPost(canvas)

                    val progress = (templateView.currentFrame) /
                            (templateView.maxFrames - 1).toFloat()

                    videoEncoder?.onProgress?.invoke(progress)

                    handler.sendEmptyMessage(WHAT_FRAME_RECORDED)
                }
            }
        }
    }

    private fun startRecording() {
        startTime = System.currentTimeMillis()

        val width = templateView.viewWidth
        val height = templateView.viewHeight
        maxFrames = templateView.maxFrames

        val factory: AudioEncoderFactory = AudioEncoderFactoryImpl(templateAndroidView.context, handler)

        val audioEncoder: Encoder? = try {
            factory.createAudioEncoder(templateView, AUDIO_PROGRESS_WEIGHT, onErrorHasHappened = {
                it.printDebug()
                onError(it, isCritical = false)
            })
        } catch (e: Exception) {
            e.printDebug()
            onError(e, isCritical = false)
            null
        }

        val videoEncoder = VideoEncoder(
            width, height, FPS, maxFrames.frameToTimeUs(), handler,
            progressWeight = 1f - (audioEncoder?.progressWeight ?: 0f)
        )

        this.videoEncoder = videoEncoder
        encoder = PipelineEncoder(
            recordToFile.absolutePath,
            ::finishRecording,
            onError = ::onError,
            onProgress = recordListener::onUpdate,
            listOfNotNull(videoEncoder, audioEncoder)
        )

        try {
            recordFrame(true)

        } catch (e: Exception) {
            onError(e, true)
        }
    }

    private fun onError(e: Throwable, isCritical: Boolean) {

        recordListener.onError(e, isCritical)
        if (isCritical) {
            handler.removeCallbacksAndMessages(null)
            release()
        }
    }

    private fun onFrameRecorded() {

        if (!checkVideoRecordingFinished()) {
            recordFrame(false)
        }
    }

    private fun checkLocks(): Boolean {
        glLocksToWait--
        K.d("ThreadRecord") { "checkLocks ${glLocksToWait}" }
        if (glLocksToWait == 0) {
            handler.removeMessages(WHAT_GL_DRAWN_TIMEDOUT)
            handler.sendEmptyMessage(WHAT_GL_DRAWN)
            return true
        }
        return false
    }

    private fun checkVideoRecordingFinished(): Boolean {
        val needToCallNext = templateView.currentFrame < maxFrames - 1 && !isInterrupted

        if (!needToCallNext) {
            videoEncoder?.sendEndOfStreamSurface()
        }
        return !needToCallNext
    }

    private fun recordFrame(firstCall: Boolean) {
        val currentFrame =
            if (firstCall) templateView.currentFrame else templateView.currentFrame + 1

        val numOfGlViewsToDraw: Int = templateView.getNumOfGlViewsToDraw(::checkLocks)
        if (numOfGlViewsToDraw > 0)
            glLocksToWait = numOfGlViewsToDraw + 1

        K.d("ThreadRecord") { "recordFrame ${currentFrame}, numOfGlViewsToDraw ${numOfGlViewsToDraw}" }

        templateView.prepareAnimation(currentFrame)
        templateView.setVideoFrameAsync(currentFrame, true)

        templateView.post {
            templateView.setFrameSync(currentFrame)

            if (numOfGlViewsToDraw == 0) {
                drawTemplateOnCanvas()
            } else {
                templateView.drawTemplateTexturesSync()
                if (checkLocks()) {
                    handler.sendEmptyMessageDelayed(WHAT_GL_DRAWN_TIMEDOUT, GL_DRAWN_TIMEOUT_MS)
                }
            }
        }
    }

    private fun finishRecording() {
        recordListener.onFinish(System.currentTimeMillis() - startTime)
        release()
    }

    fun onViewDestroyed() {
        release()
    }

    private fun release() {
        handler.removeCallbacksAndMessages(null)
        encoder?.release()
        encoder = null

        K.i("music") {
            "recording is finished. duration is ${recordToFile.getMediaDuration()}"
        }

        quitSafely()
    }

    private fun File.getMediaDuration(): Long {
        if (!exists()) return 0

        val retriever = MediaMetadataRetriever()

        try {
            retriever.setDataSource(absolutePath)
            val duration = retriever.extractMetadata(METADATA_KEY_DURATION)
            return duration?.toLongOrNull() ?: 0
        } catch (e: Throwable) {
            e.printDebug()
            return 0
        } finally {
            retriever.release()
        }
    }

    companion object {
        const val WHAT_START = 1
        const val WHAT_FRAME_RECORDED = 2
        const val WHAT_GL_DRAWN = 3
        const val WHAT_GL_DRAWN_TIMEDOUT = 4
        const val THROW_ERROR_IF_GL_TIMEOUT = false
        const val AUDIO_PROGRESS_WEIGHT = 0.2f
        const val GL_DRAWN_TIMEOUT_MS = 2000L
    }
}