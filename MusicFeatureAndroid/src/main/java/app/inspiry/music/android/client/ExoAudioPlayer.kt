package app.inspiry.music.android.client

import android.content.Context
import android.net.Uri
import app.inspiry.music.client.BaseAudioPlayer
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSource
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit


open class ExoAudioPlayer(val context: Context, val cache: Cache) : BaseAudioPlayer {
    val exoPlayer: ExoPlayer

    override fun setLoop(enabled: Boolean) {
        exoPlayer.repeatMode = if (enabled) ExoPlayer.REPEAT_MODE_ALL else ExoPlayer.REPEAT_MODE_OFF
    }

    override var errorListener: ((Throwable) -> Unit)? = null

    override fun setVolume(volume: Float) {
        exoPlayer.volume = volume
    }

    override fun isPlayWhenReady(): Boolean {
        return exoPlayer.playWhenReady
    }


    private val cacheDataSourceFactory = CacheDataSource.Factory().setCache(cache)
        .setUpstreamDataSourceFactory(DefaultDataSource.Factory(context))

    init {
        val httpDataSourceFactory: HttpDataSource.Factory = OkHttpDataSource.Factory(
            OkHttpClient.Builder()
                .followRedirects(true).followSslRedirects(true)
                .connectTimeout(10000, TimeUnit.MILLISECONDS)
                .build()
        )

        val dataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)

        exoPlayer = ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
            .build()

        exoPlayer.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.CONTENT_TYPE_MUSIC).build(), true
        )

        exoPlayer.addListener(getListener())
    }

    open fun getListener(): Player.Listener {
        return object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                errorListener?.invoke(error)
            }
        }
    }

    override fun play() {
        if (exoPlayer.playbackState == ExoPlayer.STATE_ENDED) {
            exoPlayer.seekTo(0)
        }
        exoPlayer.play()
    }

    override fun pause() {
        exoPlayer.pause()
    }

    override fun release() {
        exoPlayer.release()
    }

    protected fun String.isRemote() = startsWith("http")

    override fun prepare(url: String, startPlayImmediately: Boolean, position: Double) {

        val uri = Uri.parse(url)
        val mediaSource = ProgressiveMediaSource.Factory(cacheDataSourceFactory)
            .createMediaSource(MediaItem.fromUri(uri))

        exoPlayer.setMediaSource(mediaSource)
        exoPlayer.prepare()
        if (startPlayImmediately) exoPlayer.play()

        seekTo(position.toLong())
    }

    override fun seekTo(timeMillis: Long) {
        exoPlayer.seekTo(timeMillis)
    }

    override fun currentTimeMillis() = exoPlayer.currentPosition

    override fun getDurationMillis(): Long = exoPlayer.duration
}