package app.inspiry.video.player.controller

import android.content.Context
import app.inspiry.helpers.K
import app.inspiry.video.parseAssetsPathForAndroid
import app.inspiry.video.player.creator.VideoPlayerCreator
import app.inspiry.video.player.decoder.TextureSize
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DefaultDataSource

class ExoPlayerController(
    context: Context,
    playerCreator: VideoPlayerCreator,
    callback: GlVideoPlayerController.Callback?): RealtimeVideoPlayerControllerImpl(context, playerCreator, callback) {

    private var player: ExoPlayer? = null

    override fun seekTo(timeMillis: Long) {
        player?.seekTo(timeMillis)
    }

    override fun innerPlay() {
        player?.play()
    }

    override fun innerPause() {
        player?.pause()
    }

    override fun getCurrentPositionMillis(): Long {
        return player?.currentPosition ?: 0
    }

    override fun getVideoDurationMillis(): Long {
        return player?.duration ?: 0
    }

    override val isPlaying: Boolean
        get() = player?.isPlaying == true

    override fun initPlayer() {
        player = ExoPlayer.Builder(context).build()
        player?.setVideoSurface(playerCreator.surface)
        setListener()
        prepare()
    }

    private fun setListener() {

        player?.addListener(object : Player.Listener {
            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                notifySeekChanged()
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    callback?.onPlayerCreated(playerCreator.sourceUri, videoInfo)
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                callback?.onPlayerFailure(playerCreator.sourceUri, error)
                release()
            }

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                super.onPlayWhenReadyChanged(playWhenReady, reason)
                isPlayingState.value = playWhenReady
            }

            override fun onTracksChanged(
                trackGroups: TrackGroupArray,
                trackSelections: TrackSelectionArray
            ) {
                var innerHasAudio = false
                outer@ for (arrayIndex in 0 until trackGroups.length){
                    inner@ for (groupIndex in 0 until trackGroups[arrayIndex].length){
                        val sampleMimeType = trackGroups[arrayIndex].getFormat(groupIndex).sampleMimeType
                        if (sampleMimeType != null && sampleMimeType.contains("audio") ){
                            innerHasAudio = true
                            break@outer
                        }
                    }
                }
                hasAudioTrack = innerHasAudio
            }
        })
    }


    /*
    player.audioFormat is not available instantly after video has
    been prepared. Therefore we use hasAudioTrack which is
    initialized in onTracksChanged.
     */
    override fun hasAudioTrack(): Boolean {
        return player?.audioFormat != null || hasAudioTrack
    }

    override val videoSize: TextureSize
        get() {
            val videoSize = player?.videoSize
            val rotation = player?.videoFormat?.rotationDegrees ?: 0

            K.d("exoVideoController") {
                "videoSize ${videoSize}, rotation $rotation"
            }

            val w = videoSize?.width ?: 0
            val h = videoSize?.height ?: 0

            return getTextureSize(w, h, rotation)
        }


    override fun setVolume() {
        player?.volume = mPlayerParams.volume
    }

    override fun releaseInner() {
        player?.release()
    }

    private fun prepare() {
        val dataSourceFactory = DefaultDataSource.Factory(context)

        val videoSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(
                MediaItem.Builder()
                    .setUri(playerCreator.sourceUri.parseAssetsPathForAndroid())
                    .build()
            )

        player?.setMediaSource(videoSource)
        player?.prepare()
        player?.seekToDefaultPosition()
    }
}