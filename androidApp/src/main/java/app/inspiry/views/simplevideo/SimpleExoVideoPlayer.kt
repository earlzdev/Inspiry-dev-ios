package app.inspiry.views.simplevideo

import android.content.Context
import android.view.TextureView
import android.widget.Toast
import app.inspiry.core.log.KLogger
import app.inspiry.core.util.parseAssetsPath
import app.inspiry.utils.printDebug
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.AssetDataSource
import com.google.android.exoplayer2.upstream.DataSource
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

class SimpleExoVideoPlayer(val context: Context, val textureView: TextureView): BaseVideoPlayer, KoinComponent {

    private var exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build()

    override var onError: ((Throwable) -> Unit)? = null
    override var onPrepared: (() -> Unit)? = null

    val logger: KLogger by inject {
        parametersOf("exo-player")
    }

    override fun prepare(url: String) {
        val dataSourceFactory = DataSource.Factory { AssetDataSource(context) }

        val videoSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.Builder().setUri(url.parseAssetsPath()).build())

        exoPlayer.repeatMode = ExoPlayer.REPEAT_MODE_ALL
        exoPlayer.setMediaSource(videoSource)
        exoPlayer.setVideoTextureView(textureView)

        logger.info { "prepare ${url}, isViewAttached ${textureView.isAttachedToWindow}, isAvailable ${textureView.isAvailable}" }

        exoPlayer.addListener(object : Player.Listener {

            override fun onPlaybackStateChanged(state: Int) {
                logger.info { "onPlaybackStateChanged ${state}" }
                if (state == ExoPlayer.STATE_READY) {
                    onPrepared?.invoke()
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                error.printDebug()
                Toast.makeText(context, error.message, Toast.LENGTH_LONG).show()
            }
        })
        exoPlayer.prepare()
    }

    override fun play() {
        exoPlayer.play()
    }

    override fun pause() {
        exoPlayer.pause()
    }

    override fun release() {
        exoPlayer.release()
    }

    override fun getDuration(): Long {
        return exoPlayer.duration
    }

    override fun isPlaying(): Boolean {
        return exoPlayer.isPlaying
    }
}