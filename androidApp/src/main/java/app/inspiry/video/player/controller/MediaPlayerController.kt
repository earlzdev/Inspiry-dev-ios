package app.inspiry.video.player.controller

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import app.inspiry.helpers.K
import app.inspiry.video.parseAssetsPathForAndroid
import app.inspiry.video.player.creator.VideoPlayerCreator
import app.inspiry.video.player.decoder.TextureSize

/**
 * todo: video is often black or white completely randomly
 * I have no idea why.
 * ExoPlayer doesn't have the issue
 */
class MediaPlayerController(
    context: Context,
    playerCreator: VideoPlayerCreator,
    callback: GlVideoPlayerController.Callback?
) : RealtimeVideoPlayerControllerImpl(context, playerCreator, callback) {

    private var mediaPlayer: MediaPlayer? = null

    private var videoRotation = 0

    init {
        getVideoRotation()
    }

    override fun initPlayer() {

        K.i("mediaPlayer") {
            "initMediaPlayer is called ${playerCreator.sourceUri}"
        }

        mediaPlayer = MediaPlayer().also {
            it.setSurface(playerCreator.surface)
            it.setDataSource(
                context,
                Uri.parse(playerCreator.sourceUri.parseAssetsPathForAndroid())
            )
            it.setOnPreparedListener {

                hasAudioTrack = mediaPlayer?.trackInfo?.any {
                    it.trackType == MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_AUDIO
                } ?: false

                callback?.onPlayerCreated(playerCreator.sourceUri, videoInfo)
            }
            it.setOnErrorListener { mp, what, extra ->

                isPlayingState.value = false

                callback?.onPlayerFailure(
                    playerCreator.sourceUri,
                    IllegalStateException("error with what $what, extra ${extra}")
                )
                release()
                true
            }
            it.setOnCompletionListener {
                isPlayingState.value = false
            }

            if (Build.VERSION.SDK_INT >= 28) {
                it.setOnMediaTimeDiscontinuityListener { mp, mts ->
                    notifySeekChanged()
                }
            }

            it.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                    .build()
            )
            it.prepareAsync()

        }
    }

    private fun getVideoRotation() {
        MediaMetadataRetriever().use {
            it.setDataSource(
                context,
                Uri.parse(playerCreator.sourceUri.parseAssetsPathForAndroid())
            )
            val rotationStr =
                it.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION) ?: return
            try {
                videoRotation = rotationStr.toInt()

            } catch (ignored: NumberFormatException) {
            }
        }
    }

    override fun setVolume() {
        mediaPlayer?.setVolume(mPlayerParams.volume, mPlayerParams.volume)
    }

    override fun releaseInner() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun seekTo(timeMillis: Long) {
        K.i("mediaPlayer") {
            "seekTo ${timeMillis}. isOutOfBounds ${timeMillis < 0 || timeMillis > getVideoDurationMillis()}"
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mediaPlayer?.seekTo(timeMillis, MediaPlayer.SEEK_CLOSEST)
        } else {
            mediaPlayer?.seekTo(timeMillis.toInt())
        }
    }

    override fun innerPlay() {
        if (mediaPlayer?.isPlaying == false)
            mediaPlayer?.start()
        isPlayingState.value = true
    }

    override fun innerPause() {
        if (mediaPlayer?.isPlaying == true)
            mediaPlayer?.pause()
        isPlayingState.value = false
    }

    override fun getCurrentPositionMillis(): Long {
        return (mediaPlayer?.currentPosition ?: 0).toLong()
    }

    override fun getVideoDurationMillis(): Long {
        return (mediaPlayer?.duration ?: 0).toLong()
    }

    override val isPlaying: Boolean
        get() = mediaPlayer?.isPlaying == true

    override fun hasAudioTrack(): Boolean {
        return hasAudioTrack
    }

    override val videoSize: TextureSize
        get() {
            val height = mediaPlayer?.videoHeight ?: 0
            val width = mediaPlayer?.videoWidth ?: 0

            return getTextureSize(width, height, videoRotation)
        }
}