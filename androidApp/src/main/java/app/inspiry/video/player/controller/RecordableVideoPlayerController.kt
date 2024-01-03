package app.inspiry.video.player.controller

interface RecordableVideoPlayerController: GlVideoPlayerController {

    var onSeekFinished: (() -> Unit)?
    fun drawFrameSync(frameToTimeUs: Long, sequential: Boolean)
}