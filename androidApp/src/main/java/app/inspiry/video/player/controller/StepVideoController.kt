package app.inspiry.video.player.controller

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import app.inspiry.ap
import app.inspiry.core.data.frameToTimeUs
import app.inspiry.helpers.K
import app.inspiry.core.opengl.VideoPlayerParams
import app.inspiry.core.util.getFileNameWithParent
import app.inspiry.video.player.creator.BasePlayerCreator
import app.inspiry.video.player.creator.VideoPlayerCreator
import app.inspiry.video.player.decoder.StepDecoderPlayer
import app.inspiry.video.player.decoder.StepPlayer
import app.inspiry.video.player.decoder.VideoInfo

class StepVideoController(
    override val playerCreator: VideoPlayerCreator,
    override var callback: GlVideoPlayerController.Callback?
) : HandlerThread("PlayerThread"), RecordableVideoPlayerController {

    private var player: StepPlayer<*>? = null
    private val handler by lazy { createHandler(looper) }

    override val videoInfo: VideoInfo?
        get() = player

    @Volatile
    override var isReleasing = false

    override val playerParams: VideoPlayerParams?
        get() = player?.params as? VideoPlayerParams?


    @Volatile
    private var isPlaying = false

    override fun start() {
        super.start()
        sendAction(MSG_CREATE)
    }

    private fun requirePlayer() = player!!

    override var onSeekFinished: (() -> Unit)? = null

    private fun createHandler(looper: Looper) = object : Handler(looper) {

        override fun handleMessage(msg: Message) {
            try {
                handleMessage(msg.what, msg.obj)
            } catch (e: Exception) {
                onPlayerException(e)
            }
        }
    }

    private fun onPlayerException(e: Exception) {
        player?.release()
        quit()
        callback?.onPlayerFailure(sourceUri, e)
    }

    override fun drawFrameSync(expectedTimeUs: Long, sequential: Boolean) {
        try {
            if (!isReleasing && player != null) {
                requirePlayer().drawFrame(expectedTimeUs, sequential)
            }
        } catch (ex: Exception) {
            throw ex
        } finally {
        }
    }


    private fun restartSync() {
        if (!isReleasing) {
            player?.restart()
        }
    }

    private fun setParamsSync(playerParams: VideoPlayerParams) {
        if (!isReleasing) {
            player?.setParams(playerParams)
            playerCreator.playerParams = playerParams
        }
    }

   private fun BasePlayerCreator<*>.createStepPlayer(context: Context, handler: Handler,
                                                     onFrameSkipped: (Boolean) -> Unit, onFailure: (Exception) -> Unit) =
        StepDecoderPlayer(context, sourceUri,
            surfaceTexture, surface, playerParams as VideoPlayerParams, handler, onFrameSkipped, onFailure)

    private fun handleMessage(actionId: Int, data: Any? = null) {
        when (actionId) {
            MSG_CREATE -> {
                if (!isReleasing && player == null) {
                    player =
                        playerCreator.createStepPlayer(ap, handler, {
                            callback?.onFrameSkipped(sourceUri)

                            if (onSeekFinished != null) {
                                onSeekFinished?.invoke()
                                onSeekFinished = null
                            }
                        }, ::onPlayerException)

                    callback?.onPlayerCreated(sourceUri, requirePlayer())
                }
            }
            MSG_RELEASE -> {
                if (isReleasing && player != null) {
                    requirePlayer().release()
                    quit()
                    callback?.onPlayerReleased(sourceUri)
                    player = null
                }
            }
            MSG_RESTART -> {
                restartSync()
            }
            MSG_DRAW_FRAME -> {
                val (expectedTimeUs, sequential) = data as Pair<Long, Boolean>
                drawFrameSync(expectedTimeUs, sequential)
            }
            MSG_UPDATE_PARAMS -> {
                setParamsSync(data as VideoPlayerParams)
            }
        }
    }

    override fun release() {
        isReleasing = true
        sendAction(MSG_RELEASE)
    }

    override fun play(currentFrame: Int, forcePlay: Boolean) {
        isPlaying = true
    }

    override fun pause() {
        isPlaying = false
    }

    private fun sendAction(actionId: Int, data: Any? = null) {

        verbose { "action $actionId was scheduled isIgnored ${isIgnoreAction(actionId)} isAlive ${isAlive}" }
        if (isIgnoreAction(actionId)) return
        if (isAlive) {
            handler.sendMessage(handler.obtainMessage(actionId, data))
        }
    }

    private fun isIgnoreAction(actionId: Int) = isReleasing && actionId != MSG_RELEASE

    override fun restart(currentFrame: Int) {
        sendAction(MSG_RESTART)
    }

    override fun setParamsAsync(playerParams: VideoPlayerParams) {
        sendAction(MSG_UPDATE_PARAMS, playerParams)
    }

    override fun drawFrame(frame: Int, sequential: Boolean) {
        sendAction(MSG_DRAW_FRAME, frame.frameToTimeUs() to sequential)
    }

    override fun hasAudioTrack(): Boolean {
        return (player as? StepDecoderPlayer?)?.hasAudioTrack() ?: false
    }

    private inline fun debug(msg: () -> String) {
        K.d(K.TAG_STEP_PLAYER_THREAD) { "${sourceUri.getFileNameWithParent()} ${msg()}" }
    }

    private inline fun verbose(msg: () -> String) {
        K.v(K.TAG_STEP_PLAYER_THREAD) { "${sourceUri.getFileNameWithParent()} ${msg()}" }
    }

    companion object {
        private const val MSG_CREATE = 0
        private const val MSG_RELEASE = 1
        private const val MSG_RESTART = 2
        private const val MSG_DRAW_FRAME = 3
        private const val MSG_UPDATE_PARAMS = 4
        private const val MSG_SYNC_RESTART_TIME = 5
    }
}