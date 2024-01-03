package app.inspiry.music.client

interface BaseAudioPlayer {
    fun play()
    fun pause()
    fun release()
    fun prepare(url: String, startPlayImmediately: Boolean = false, position: Double = 0.0)
    fun seekTo(timeMillis: Long)
    fun currentTimeMillis(): Long
    fun getDurationMillis(): Long
    fun setVolume(volume: Float)
    fun isPlayWhenReady(): Boolean
    fun setLoop(enabled: Boolean)

    var errorListener: ((Throwable) -> Unit)?

}

class EmptyAudioPlayer: BaseAudioPlayer {
    override fun play() {

    }

    override fun pause() {

    }

    override fun release() {

    }

    override fun prepare(url: String, startPlayImmediately: Boolean, position: Double) {

    }

    override fun seekTo(timeMillis: Long) {

    }

    override fun currentTimeMillis(): Long {
       return 0L
    }

    override fun getDurationMillis(): Long {
        return 0L
    }

    override fun setVolume(volume: Float) {

    }

    override fun isPlayWhenReady(): Boolean {
        return false
    }

    override fun setLoop(enabled: Boolean) {

    }

    override var errorListener: ((Throwable) -> Unit)? = null

}