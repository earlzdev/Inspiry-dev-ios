package app.inspiry.music.android.client

import android.content.Context
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.upstream.cache.Cache
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * @param scope is used to launch current time job
 */
class ExoAudioStatePlayer(context: Context,
                          cache: Cache, val scope: CoroutineScope) : ExoAudioPlayer(context, cache), BaseAudioStatePlayer {

    override val playingState: MutableStateFlow<AudioPlayerStates.PlayingState> =
        MutableStateFlow(AudioPlayerStates.PlayingState(false, false, ""))

    override val durationState: MutableStateFlow<Long> = MutableStateFlow(0L)
    override val currentTimeState: MutableStateFlow<Long> = MutableStateFlow(0L)

    private var currentTimeJob: Job? = null

    override fun cancelCurrentTimeJob() {
        currentTimeJob?.cancel()
        currentTimeJob = null
    }

    override fun release() {
        cancelCurrentTimeJob()
        super.release()
    }

    private fun startCurrentTimeJob() {
        if (currentTimeJob != null) return

        scope.launch(Dispatchers.Main) {
            while (playingState.value.isPlaying) {
                delay(50L)
                currentTimeState.emit(exoPlayer.currentPosition)
            }
        }
    }

    override fun getListener(): Player.Listener {
        return object : Player.Listener {

            override fun onPlayerError(error: PlaybackException) {
                errorListener?.invoke(error)
                playingState.value = playingState.value.copy(isLoading = false)
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                playingState.value = playingState.value.copy(isPlaying = isPlaying)

                if (isPlaying)
                    startCurrentTimeJob()
                else
                    cancelCurrentTimeJob()
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == ExoPlayer.STATE_READY) {
                    durationState.value = exoPlayer.duration
                    playingState.value = playingState.value.copy(isLoading = false)
                }
            }
        }
    }

    override fun prepare(url: String, startPlayImmediately: Boolean, position: Double) {
        durationState.value = 0L
        currentTimeState.value = 0L

        playingState.value = AudioPlayerStates.PlayingState(
            playingState.value.isPlaying || startPlayImmediately,
            isLoading = url.isRemote(),
            url
        )
        super.prepare(url, startPlayImmediately, position)
    }

    override fun seekTo(timeMillis: Long) {
        currentTimeState.value = timeMillis
        super.seekTo(timeMillis)
        startCurrentTimeJob()
    }
}