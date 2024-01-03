package app.inspiry.video.player.decoder

import androidx.annotation.CallSuper
import app.inspiry.helpers.K
import app.inspiry.core.opengl.PlayerParams
import app.inspiry.core.util.getFileNameWithParent

abstract class StepPlayer<T : PlayerParams>(
    val sourceUri: String,
    var params: T
) : VideoInfo {

    val logTag = sourceUri.getFileNameWithParent()

    /**
     * Current video position
     */
    var currentTimeUs = 0L
        protected set

    /**
     * The video has reached the end
     */
    abstract val isCompleted: Boolean

    abstract val doLoop: Boolean

    /**
     * @param sequential if false then only decode one frame at a random time
     * @return true - if has new frame
     */
    abstract fun drawFrame(expectedTimeUs: Long, sequential: Boolean)

    open fun isNextFrameEnabled() = !isCompleted

    @CallSuper
    open fun restart() {
        debug { "restart" }
        currentTimeUs = 0
    }

    abstract fun release()

    /**
     * @return true - if params has changed
     */
    open fun setParams(params: PlayerParams): Boolean {
        if (this.params != params) {
            this.params = params as T
            return true
        }
        return false
    }

    protected inline fun debug(msg: () -> String) {
        K.d(K.TAG_STEP_PLAYER) { "$logTag ${msg()}" }
    }

    protected inline fun verbose(msg: () -> String) {
        K.v(K.TAG_STEP_PLAYER) { "$logTag ${msg()}" }
    }

    companion object {
        const val AVERAGE_FRAME_INTERVAL_US = 20_000L
        const val MAX_OUT_OF_SYNC_TIME_US = 100_000L
    }
}