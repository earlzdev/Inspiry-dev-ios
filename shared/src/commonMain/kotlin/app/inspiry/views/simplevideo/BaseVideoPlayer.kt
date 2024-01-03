package app.inspiry.views.simplevideo

interface BaseVideoPlayer {
    var onError: ((Throwable) -> Unit)?
    var onPrepared: (() -> Unit)?

    fun prepare(url: String)
    fun play()
    fun pause()
    fun release()
    fun getDuration(): Long
    fun isPlaying(): Boolean
}