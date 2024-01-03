package app.inspiry.video.player

import android.graphics.SurfaceTexture
import android.view.Surface
import app.inspiry.ap
import app.inspiry.helpers.K
import app.inspiry.core.opengl.PlayerParams
import app.inspiry.core.opengl.VideoPlayerParams
import app.inspiry.video.player.controller.ExoPlayerController
import app.inspiry.video.player.creator.BasePlayerCreator
import app.inspiry.video.player.creator.VideoPlayerCreator
import app.inspiry.video.player.decoder.VideoInfo
import app.inspiry.video.player.controller.GlVideoPlayerController
import app.inspiry.video.player.controller.MediaPlayerController
import app.inspiry.video.player.controller.StepVideoController
import java.util.concurrent.CopyOnWriteArrayList

class PlayerCore(private val callback: GlVideoPlayerController.Callback, isRecording: Boolean) :
    GlVideoPlayerController.Callback {

    private val players = CopyOnWriteArrayList<GlVideoPlayerController>()
    var isRecording: Boolean = isRecording
        set(value) {
            val changed = field != value
            field = value
            if (changed)
                switchPlayersRecordMode(value)
        }

    fun registerVideoPlayerAsync(
        sourceUri: String,
        surfaceTexture: SurfaceTexture,
        surface: Surface,
        playerParams: VideoPlayerParams
    ) {
        registerPlayerAsync(VideoPlayerCreator(sourceUri, surfaceTexture, surface, playerParams))
    }

    private fun registerPlayerAsync(stepPlayerCreator: BasePlayerCreator<*>) {
        synchronized(CREATE_RELEASE_PLAYER_LOCK) {
            val sourceUri = stepPlayerCreator.sourceUri
            K.d(K.TAG_CREATE_REMOVE_PLAYER) { "try register player $sourceUri" }
            val existPlayer = findPlayer(sourceUri)
            if (existPlayer == null || existPlayer.isReleasing) {
                K.d(K.TAG_CREATE_REMOVE_PLAYER) { "register player $sourceUri" }

                players.add(
                    createPlayerController(stepPlayerCreator, isRecording)
                )

                debug { "register async $sourceUri, currentSize = ${players.size}" }
            } else K.d(K.TAG_CREATE_REMOVE_PLAYER) { "player $sourceUri is exists" }
        }
    }

    private fun createPlayerController(
        stepPlayerCreator: BasePlayerCreator<*>,
        recording: Boolean
    ): GlVideoPlayerController {
        if (recording) {
            return StepVideoController(stepPlayerCreator as VideoPlayerCreator, callback)
                .apply { start() }
        } else {
            return ExoPlayerController(ap, stepPlayerCreator as VideoPlayerCreator, this@PlayerCore)
        }
    }

    fun setParamsAsync(uri: String, playerParams: PlayerParams) {
        findPlayer(uri)?.setParamsAsync(playerParams as VideoPlayerParams)
    }

    private fun findPlayer(sourceUri: String) =
        players.firstOrNull { it.sourceUri == sourceUri }

    fun unregisterPlayerAsync(sourceUri: String) {
        synchronized(CREATE_RELEASE_PLAYER_LOCK) {
            K.d(K.TAG_CREATE_REMOVE_PLAYER) { "unregister player $sourceUri" }
            debug { "unregister async $sourceUri, currentSize = ${players.size}" }
            findPlayer(sourceUri)?.release()
        }
    }

    private fun switchPlayersRecordMode(isRecord: Boolean) {
        players.mapTo(players) {

            it.callback = null
            it.release()

            createPlayerController(it.playerCreator, isRecord)
        }
    }

    fun execute(sourceUri: String, block: (GlVideoPlayerController) -> Unit) {
        findPlayer(sourceUri)?.let(block)
    }

    fun play(sourceUri: String, currentFrame: Int, forcePlay: Boolean) {
        findPlayer(sourceUri)?.play(currentFrame, forcePlay)
    }

    fun getVideoInfo(sourceUri: String) =
        findPlayer(sourceUri)?.videoInfo

    override fun onFrameSkipped(sourceUri: String) {
        callback.onFrameSkipped(sourceUri)
    }

    override fun onPlayerCreated(sourceUri: String, videoInfo: VideoInfo) {
        K.d(K.TAG_CREATE_REMOVE_PLAYER) { "player was created: $sourceUri" }
        callback.onPlayerCreated(sourceUri, videoInfo)
    }

    override fun onPlayerFailure(sourceUri: String, e: Exception) {
        callback.onPlayerFailure(sourceUri, e)
    }

    override fun onPlayerReleased(sourceUri: String) {
        synchronized(CREATE_RELEASE_PLAYER_LOCK) {
            val removePlayers = players.filter { it.isReleasing }
            players.removeAll(removePlayers)
            K.d(K.TAG_CREATE_REMOVE_PLAYER) { "player was released: $sourceUri, currentSize = ${players.size}" }
        }
        callback.onPlayerReleased(sourceUri)
    }

    private inline fun debug(msg: () -> String) {
        K.d(K.TAG_STEP_MULTI_VIDEO_PLAYER) { msg() }
    }

    companion object {
        private val CREATE_RELEASE_PLAYER_LOCK = Any()
    }
}