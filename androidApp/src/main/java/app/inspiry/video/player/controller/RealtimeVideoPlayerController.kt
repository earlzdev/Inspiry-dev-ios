package app.inspiry.video.player.controller

import kotlinx.coroutines.flow.StateFlow

interface RealtimeVideoPlayerController: GlVideoPlayerController {

    fun getIsPlayingState(): StateFlow<Boolean>
    fun getCurrentTimeMs(): StateFlow<Long>
    fun updateCurrentTimeNoViewTimingMode()
    fun setVideoPositionIgnoreViewTiming()
}