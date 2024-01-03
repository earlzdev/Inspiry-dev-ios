package app.inspiry.helpers

import android.content.Context
import android.util.Log
import app.inspiry.BuildConfig

object K {
    const val LOG_TAG = "insp"
    private const val UNCAUGHT_EXCEPTION_MESSAGE = "uncaught exception"

    const val TAG_VIDEO_DECODER = "video_decoder"
    const val TAG_VIDEO_EXTRACTOR = "video_extractor"
    const val TAG_STEP_PLAYER = "step_player"
    const val TAG_STEP_PLAYER_THREAD = "step_player_thread"
    const val TAG_STEP_MULTI_VIDEO_PLAYER = "step_multi_video_player"
    const val TAG_MULTI_VIDEO_PLAYER = "multi_video_player"
    const val TAG_TEXTURE_FACTORY = "texture_factory"
    const val TAG_GLES_PROGRAM = "gles_program"
    const val TAG_GLES_OUTPUT_ITEM = "gles_output_item"
    const val TAG_GLES_OUTPUT = "gles_output"
    const val TAG_TEXTURE_MATRIX = "gles_texture_matrix"
    const val TAG_CREATE_REMOVE_PLAYER = "create_remove_player"

    private const val FILE_LOGGER_SPLIT_PERIOD_MIN = 30

    lateinit var fileLogger: FileLogger

    fun init(context: Context) {
        fileLogger = FileLogger(context, FILE_LOGGER_SPLIT_PERIOD_MIN)
        setupLogForUncaughtException()
    }

    private fun setupLogForUncaughtException() {
        val oldHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            fileLogger.log(e, UNCAUGHT_EXCEPTION_MESSAGE)
            oldHandler?.uncaughtException(t, e)
        }
    }

    inline fun d(tag: String, msg: () -> String) {
        if (BuildConfig.DEBUG) {
            Log.d(LOG_TAG, msg().addTag(tag))
        }
    }

    fun String.addTag(tag: String) = "$tag $this"

    inline fun i(tag: String, msg: () -> String) {
        if (BuildConfig.DEBUG) {
            Log.i(LOG_TAG, msg().addTag(tag))
        }
    }

    inline fun v(tag: String, msg: () -> String) {
        if (BuildConfig.DEBUG) {
            Log.v(LOG_TAG, msg().addTag(tag))
        }
    }

    inline fun w(tag: String, error: Throwable? = null, msg: () -> String) {
        if (BuildConfig.DEBUG) {

            if (error == null) {
                Log.w(LOG_TAG, msg().addTag(tag))
            } else {
                Log.w(LOG_TAG, msg().addTag(tag), error)
            }
        }
    }

    inline fun e(tag: String, throwable: Throwable? = null, msg: () -> String) {
        if (BuildConfig.DEBUG) {
            e(throwable) { msg().addTag(tag) }
        }
    }

    inline fun e(throwable: Throwable? = null, msg: () -> String) {
        if (BuildConfig.DEBUG) {
            val message = msg()
            Log.e(LOG_TAG, message, throwable)
            fileLogger.log(throwable, message)
        }
    }

    fun e(throwable: Throwable?) {
        e(throwable) { "" }
    }

    inline fun <T>debugTime(tag: String, message: () -> String, action: () -> T): T {
        if (BuildConfig.DEBUG) {
            val time = System.currentTimeMillis()
            val r = action()
            val spend = System.currentTimeMillis() - time

            Log.i(LOG_TAG, message().addTag(tag).format(spend))
            return r
        } else {
            return action()
        }
    }
}