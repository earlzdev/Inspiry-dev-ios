package app.inspiry.video.player.controller

import app.inspiry.core.opengl.VideoPlayerParams
import app.inspiry.video.player.creator.VideoPlayerCreator
import app.inspiry.video.player.decoder.VideoInfo
import kotlinx.coroutines.flow.StateFlow

interface GlVideoPlayerController {

    val playerCreator: VideoPlayerCreator
    var callback: Callback?
    val videoInfo: VideoInfo?
    var isReleasing: Boolean

    val playerParams: VideoPlayerParams?

    val sourceUri: String
        get() = playerCreator.sourceUri

    fun restart(currentFrame: Int)
    fun setParamsAsync(playerParams: VideoPlayerParams)
    fun release()
    fun play(currentFrame: Int, forcePlay: Boolean)
    fun pause()

    fun updateDecoderParamsAsync(action: (VideoPlayerParams) -> Unit) {
        val current = playerParams
        if (current != null) {
            val newParams = current.copy()
            action(newParams)
            setParamsAsync(newParams)
        }
    }

    fun drawFrame(frame: Int, sequential: Boolean)

    fun hasAudioTrack(): Boolean

    interface Callback {
        //only for record mode.
        fun onFrameSkipped(sourceUri: String)
        fun onPlayerCreated(sourceUri: String, videoInfo: VideoInfo)
        fun onPlayerReleased(sourceUri: String)
        fun onPlayerFailure(sourceUri: String, t: Exception)
    }
}