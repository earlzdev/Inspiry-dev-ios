package app.inspiry.video.player.controller

import android.content.Context
import app.inspiry.core.data.frameToTimeMillis
import app.inspiry.core.manager.AppViewModel
import app.inspiry.core.opengl.VideoPlayerParams
import app.inspiry.core.log.KLogger
import app.inspiry.video.player.creator.VideoPlayerCreator
import app.inspiry.video.player.decoder.TextureSize
import app.inspiry.video.player.decoder.VideoInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import kotlin.math.min

/*
    mPlayerParams.totalDurationUs can be 0. If it is so, then totalDurationUs goes out of the equation
 */
abstract class RealtimeVideoPlayerControllerImpl(
    val context: Context,
    override val playerCreator: VideoPlayerCreator,
    override var callback: GlVideoPlayerController.Callback?
) : RealtimeVideoPlayerController, VideoInfo, KoinComponent {

    private var shouldPlay: Boolean = false

    private var lastCurFrame: Int = 0

    protected var mPlayerParams: VideoPlayerParams = playerCreator.playerParams

    //updated in getCurrentPlayingPositionMillis
    private var currentLoopCycle = 0L

    override val videoInfo: VideoInfo
        get() = this

    override var isReleasing: Boolean = false

    override val playerParams: VideoPlayerParams
        get() = mPlayerParams

    protected val isPlayingState = MutableStateFlow(false)

    private var currentTimeMsFlow: MutableStateFlow<Long>? = null

    private val logger: KLogger by inject { parametersOf("ExoVideoController ${playerCreator.sourceUri}") }

    private val appViewModel: AppViewModel by inject()

    protected var hasAudioTrack: Boolean = false

    protected var modeIgnoreViewTiming: Boolean = false

    init {
        appViewModel.applicationScope.launch(Dispatchers.Main) {
            initPlayer()
            setVolume()
        }
    }

    abstract fun initPlayer()
    abstract fun setVolume()
    abstract fun releaseInner()
    abstract fun seekTo(timeMillis: Long)
    abstract fun innerPlay()
    abstract fun innerPause()
    abstract fun getCurrentPositionMillis(): Long
    abstract fun getVideoDurationMillis(): Long
    abstract val isPlaying: Boolean

    override fun setParamsAsync(playerParams: VideoPlayerParams) {
        val oldParams = mPlayerParams
        mPlayerParams = playerParams

        logger.info { "setParamsAsync $playerParams" }

        if (playerParams.volume != oldParams.volume) {
            setVolume()
        }

        if (playerParams.videoStartTimeUs != oldParams.videoStartTimeUs) {
            setVideoPositionIgnoreViewTiming()
        }

        if (playerParams.viewStartTimeUs != oldParams.viewStartTimeUs ||
                playerParams.isLoopEnabled != oldParams.isLoopEnabled ||
                playerParams.totalDurationUs != oldParams.totalDurationUs) {

            //timing has changed
            drawFrame(lastCurFrame, false)
        }
    }

    override fun setVideoPositionIgnoreViewTiming() {
        logger.info { "setPosition ignoreViewTiming" }

        modeIgnoreViewTiming = true
        val time = mPlayerParams.videoStartTimeUs / 1000L
        pause()
        seekTo(time)
    }

    override fun release() {
        isReleasing = true
        appViewModel.applicationScope.launch(Dispatchers.Main) {
            releaseInner()
            callback?.onPlayerReleased(playerCreator.sourceUri)
        }
    }

    //-1 means there's no valid position
    private fun getCurrentPlayingPositionMillis(currentFrame: Int): Long {

        val videoDuration = getVideoDurationMillis()
        if (videoDuration <= 0L) return POSITION_INVALID_NOTHING

        val currentMillis = currentFrame.frameToTimeMillis() - mPlayerParams.viewStartTimeUs / 1000L

        if (currentMillis < 0) return POSITION_INVALID_BEFORE_START

        if (mPlayerParams.totalDurationUs != 0L && currentMillis >= mPlayerParams.totalDurationUs / 1000L) return POSITION_INVALID_AFTER_END


        val realVideoDuration = videoDuration - mPlayerParams.videoStartTimeUs / 1000L

        if (mPlayerParams.isLoopEnabled) {

            currentLoopCycle = currentMillis / realVideoDuration

            val res = (currentMillis % realVideoDuration) + mPlayerParams.videoStartTimeUs / 1000L

            assert(res <= videoDuration) {
                "currentPosMillis ${res}, videoDuration ${videoDuration}, realVideoDuration ${realVideoDuration}"
            }

            return res

        } else {

            if (currentMillis < realVideoDuration) {
                return currentMillis + mPlayerParams.videoStartTimeUs / 1000L
            } else {
                return POSITION_INVALID_AFTER_END
            }
        }
    }

    protected fun getTextureSize(w: Int, h: Int, rotation: Int): TextureSize {
        val switchedAspectRatio = rotation != 0 && (rotation % 90) == 0

        return TextureSize(if (switchedAspectRatio) h else w, if (switchedAspectRatio) w else h, rotation.toFloat())
    }

    private fun seekToCurrentPlayingPosition(position: Long) {

        when (position) {
            POSITION_INVALID_BEFORE_START -> {
                seekTo(mPlayerParams.videoStartTimeUs / 1000L)

            }
            POSITION_INVALID_AFTER_END -> {

                val seekTime = if (mPlayerParams.totalDurationUs == 0L) {
                    getVideoDurationMillis()
                } else {
                    min(
                        getVideoDurationMillis(),
                        mPlayerParams.totalDurationUs / 1000L + mPlayerParams.videoStartTimeUs / 1000L
                    )
                }
                seekTo(seekTime)

            }
            else -> {
                seekTo(position)
            }
        }
    }

    override fun restart(currentFrame: Int) {
        lastCurFrame = currentFrame
        seekToPositionAndTogglePlay(currentFrame)
    }

    private fun seekToPositionAndTogglePlay(currentFrame: Int) {
        val position = getCurrentPlayingPositionMillis(currentFrame)

        logger.info { "seekToPositionAndTogglePlay ${position}, ${currentFrame}" }

        if (position == POSITION_INVALID_NOTHING)
            return

        seekToCurrentPlayingPosition(position)

        if (position < 0) {
            innerPause()
        } else if (shouldPlay) {
            innerPlay()
        }
    }

    override fun play(currentFrame: Int, forcePlay: Boolean) {
        lastCurFrame = currentFrame
        shouldPlay = true

        if (modeIgnoreViewTiming || forcePlay) {
            setVideoPositionIgnoreViewTiming()
            innerPlay()
        } else {
            seekToPositionAndTogglePlay(currentFrame)
        }
    }

    override fun pause() {
        shouldPlay = false
        innerPause()
    }

    override fun getIsPlayingState(): StateFlow<Boolean> {
        return isPlayingState
    }

    override fun getCurrentTimeMs(): StateFlow<Long> {
        if (currentTimeMsFlow == null) {
            currentTimeMsFlow = MutableStateFlow(getCurrentPositionMillis())
        }
        return currentTimeMsFlow!!
    }

    protected fun notifySeekChanged() {
        if (modeIgnoreViewTiming)
            updateCurrentTimeNoViewTimingMode()
    }

    override fun updateCurrentTimeNoViewTimingMode() {
        val curTime = getCurrentPositionMillis()
        if (curTime >= (mPlayerParams.totalDurationUs + mPlayerParams.videoStartTimeUs) / 1000L) {
            pause()
        }
        currentTimeMsFlow?.value = curTime
    }

    override fun drawFrame(frame: Int, sequential: Boolean) {
        lastCurFrame = frame
        modeIgnoreViewTiming = false

        val oldLoopCycle = currentLoopCycle
        val currentPlayingPosition = getCurrentPlayingPositionMillis(frame)

        logger.info { "drawFrame $frame, sequential: $sequential, " +
                " currentPlayingPosition ${currentPlayingPosition}, $mPlayerParams" }

        if (currentPlayingPosition == POSITION_INVALID_NOTHING)
            return

        if (!sequential) {
            seekToCurrentPlayingPosition(currentPlayingPosition)

        } else {

            if (currentPlayingPosition < 0) {
                innerPause()

            } else {

                if (oldLoopCycle != currentLoopCycle) {
                    seekTo(currentPlayingPosition)
                }

                if (shouldPlay && !isPlaying) {
                    innerPlay()
                }
            }
        }
    }

    override val videoDurationUs: Long
        get() = getVideoDurationMillis() * 1000

}
private const val POSITION_INVALID_BEFORE_START = -1L
private const val POSITION_INVALID_AFTER_END = -2L
private const val POSITION_INVALID_NOTHING = -3L