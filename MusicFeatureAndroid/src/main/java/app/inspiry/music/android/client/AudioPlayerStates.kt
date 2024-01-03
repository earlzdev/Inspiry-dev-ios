package app.inspiry.music.android.client

import app.inspiry.music.client.BaseAudioPlayer
import kotlinx.coroutines.flow.MutableStateFlow

interface AudioPlayerStates: BaseAudioPlayer {

    val playingState: MutableStateFlow<PlayingState>
    val durationState: MutableStateFlow<Long>
    val currentTimeState: MutableStateFlow<Long>

    // should be invoked on user touch slider
    fun cancelCurrentTimeJob()

    data class PlayingState(val isPlaying: Boolean, val isLoading: Boolean, val url: String)
}