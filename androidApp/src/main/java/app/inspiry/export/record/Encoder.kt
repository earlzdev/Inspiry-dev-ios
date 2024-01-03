package app.inspiry.export.record

import android.media.MediaCodec
import android.media.MediaFormat
import android.os.Handler
import java.nio.ByteBuffer

abstract class Encoder(
    val handler: Handler,
    //total duration that we need. Not more.
    val durationUs: Long,
    //how much progress of this encoder contribute to the overall progress of PipelineEncoder
    val progressWeight: Float
) {

    var onFormatInitialized: ((MediaFormat) -> Unit)? = null
    var onFinished: (() -> Unit)? = null
    var onError: ((Throwable) -> Unit)? = null
    var onProgress: ((Float) -> Unit)? = null
    var writeSampleData: ((ByteBuffer, MediaCodec.BufferInfo) -> Unit)? = null
    var writingFinished: Boolean = false

    abstract fun initialize()
    abstract val type: EncoderType

    //means that if error has happened we stop recording of everything.
    abstract val isCritical: Boolean

    protected abstract fun checkOutputAvailable()

    var canWrite: Boolean = false
        get() = field
        set(value) {
            field = value
            if (value) {
                checkOutputAvailable()
            }
        }

    protected fun onWritingFinished() {
        writingFinished = true
        canWrite = false
        onProgress?.invoke(1f)
        onFinished?.invoke()
        release()
    }

    open fun onErrorHasHappened(t: Throwable) {
        t.printStackTrace()
        release()
        onError?.invoke(t)
    }

    fun release() {
        if (!isRelease())
            _releaseInner()
    }

    protected open fun _releaseInner() {
        writingFinished = true
        canWrite = false
        onFinished = null
        onError = null
        writeSampleData = null
        onFormatInitialized = null
    }

    abstract fun isRelease(): Boolean

}