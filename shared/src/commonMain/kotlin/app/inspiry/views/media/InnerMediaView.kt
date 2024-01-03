package app.inspiry.views.media

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface InnerMediaView {
    fun setInnerImageScale(scaleX: Float, scaleY: Float)
    fun setBlurRadius(blurRadius: Float, async: Boolean)

    fun updateVideoStartTime(originalUri: String, textureIndex: Int, videoStartTimeMs: Int)
    fun updateVideoVolume(volume: Float)

    fun setVideoPositionIgnoreViewTiming()
    fun playVideoIfExists(forcePlay: Boolean)
    fun pauseVideoIfExists()
    fun restartVideoIfExists()
    fun isVideoPlayingState(): StateFlow<Boolean>
    fun videoCurrentTimeMs(): StateFlow<Long>
    fun setColorFilter(color: Int?)
    fun setPickImage(onClick: (() -> Unit)?)
    fun removeInnerMedia()
    fun updateVideoCurrentTimeNoViewTimingMode()

    fun setImageInitial(url: String?, onError: (Throwable?) -> Unit, onSuccess: () -> Unit)

    var framePreparedCallback: (() -> Unit)?

    fun setDisplayVideo(): Boolean

    fun refresh()

    fun setVideoTotalDurationMs(duration: Int)
    fun interruptImageLoading()

    fun drawVideoFrameSync(frame: Int, sequential: Boolean)
    fun getVideoDurationMs(): Long
    fun setUpMatrix()
    fun restoreRenderingInList()
    fun loadNewImage(path: String, textureIndex: Int, onSuccess: () -> Unit)
    fun setTranslateInner(translationX: Float, translationY: Float)
    fun setRecording(value: Boolean)
    fun drawVideoFrameAsync(frame: Int, sequential: Boolean)
    fun setVideoInner(uri: String, textureIndex: Int)
    fun doWhenSizeIsKnown(function: () -> Unit)
    fun isVideoHasAudio(): Boolean
    fun updateBorder() {} //border width and color updating. IOS using
}