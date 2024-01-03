package app.inspiry.video.egl

import android.view.TextureView
import android.view.ViewGroup
import app.inspiry.helpers.K
import app.inspiry.video.CustomTextureView
import app.inspiry.core.data.TransformMediaData
import app.inspiry.video.grafika.EglCore
import app.inspiry.video.player.controller.GlVideoPlayerController
import app.inspiry.video.player.decoder.VideoInfo
import app.inspiry.video.program.DeferredProgramCreator
import app.inspiry.video.renderThread
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * The main class to register a program and execute it on textureView
 */
object EGLOutput : EGLOutputItem.LifecycleListener {

    private var currentTextureViewId = AtomicLong(0)

    // Core EGL state (display, context, config)
    private val eglCore by lazy { EglCore(null, EglCore.FLAG_RECORDABLE) }

    // All running programs. TextureViewId and program
    private val programItemMap = ConcurrentHashMap<Long, EGLProgramItem>()

    // TextureViewId and listener
    private val preparedListenerMap = ConcurrentHashMap<Long, RenderingPreparedListener>()

    // TextureView and textureViewId
    private val textureViewMap = ConcurrentHashMap<CustomTextureView, Long>()

    /**
     * Create new or recreate all program for textureView
     *
     * @return textureId
     */
    fun register(
        textureView: CustomTextureView,
        programCreator: DeferredProgramCreator,
        renderingPreparedListener: RenderingPreparedListener, isRecording: Boolean
    ) {
        renderThread {
            removeNotUsedProgram()
            val programItem = findProgramItem(textureView)
            var textureViewId = textureViewMap[textureView]
            if (programItem != null) {
                if (textureView.isAvailable) programItem.recreateProgram(programCreator)
            } else {
                textureViewId = currentTextureViewId.getAndIncrement()
                preparedListenerMap[textureViewId] = renderingPreparedListener
                //createProgram will be called in onSurfaceCreated in EGLProgramItem
                programItemMap[textureViewId] = EGLProgramItem(textureView, textureViewId, eglCore,
                    programCreator, this, isRecording)
                textureViewMap[textureView] = textureViewId
            }
            debug {
                "register $textureViewId" +
                    ", programItemMap.size = ${programItemMap.size}" +
                    ", preparedListenerMap.size = ${preparedListenerMap.size}" +
                    ", textureViewMap.size = ${textureViewMap.size}" +
                        ", textureView.isAvailable = ${textureView.isAvailable}" +
                        ", programItem is found = ${programItem != null}"
            }
        }
    }

    fun executeOnPlayer(textureView: CustomTextureView, onlyForEditTexture: Boolean, block: (GlVideoPlayerController) -> Unit) {
        val programItem = findProgramItem(textureView)
        programItem?.executeOnPlayer(onlyForEditTexture, block)
    }

    fun getVideoInfo(textureView: CustomTextureView, uri: String): VideoInfo? {
        val programItem = findProgramItem(textureView)
        return programItem?.getVideoInfo(uri)
    }

    fun redrawProgram(textureView: CustomTextureView) {
        val programItem = findProgramItem(textureView)
        renderThread {
            programItem?.drawProgram()
        }
    }


    private fun findProgramItem(textureView: TextureView) = textureViewMap[textureView]
        ?.run { programItemMap[this] }

    private fun removeNotUsedProgram() {
        programItemMap.values
            .forEach { it.removeNotUsedProgram() }
    }

    override fun onSurfaceCreated(textureViewId: Long) {
        preparedListenerMap[textureViewId]?.run {
            val programItem = programItemMap[textureViewId]

            val error = programItem?.totalProgramLastError

            if (error != null) onPrepareFailed(error)
            else {

                if (REDRAW_PROGRAM_WHEN_SURFACE_IS_CREATED) {
                    if (programItem != null) {
                        renderThread { programItem.drawProgram() }
                    }
                }
                onSurfacePrepareCompleted()
            }
        }
    }

    override fun onSurfaceReleased(textureViewId: Long) {
    }

    override fun onDrawFrame(textureViewId: Long) {
        preparedListenerMap[textureViewId]?.onFramePrepared()
    }

    override fun onSurfaceSizeChanged(textureViewId: Long, width: Int, height: Int) {
    }

    override fun onTextureViewDestroyed(textureViewId: Long) {

        preparedListenerMap.remove(textureViewId)
        val programItem = programItemMap.remove(textureViewId)
        val textureView = textureViewMap.entries
            .firstOrNull { it.value == textureViewId }?.key
        textureView?.let { textureViewMap.remove(it) }

        programItem?.removeProgram()

        debug {
            "onTextureViewDestroyed $textureViewId " +
                    ", programItemMap.size = ${programItemMap.size}" +
                    ", preparedListenerMap.size = ${preparedListenerMap.size}" +
                    ", textureViewMap.size = ${textureViewMap.size}"
        }
    }

    fun removeTextureView(textureView: CustomTextureView) {
        renderThread {
            val textureViewId = textureViewMap[textureView] ?: return@renderThread
            onTextureViewDestroyed(textureViewId)
        }
    }

    fun setTransform(
        textureView: CustomTextureView,
        textureTransformData: TransformMediaData
    ) {
        findProgramItem(textureView)
            ?.setTransform(textureTransformData)

        redrawProgram(textureView)
    }

    fun setBlurRadius(
        textureView: CustomTextureView,
        radius: Float
    ) {
        findProgramItem(textureView)
            ?.setBlurRadius(radius)
    }

    private inline fun debug(msg: () -> String) {
        K.d(K.TAG_GLES_OUTPUT) { msg() }
    }

    fun getSurfaceForTemplateTexture(textureView: CustomTextureView, containerView: ViewGroup, textureIndex: Int) =
        findProgramItem(textureView)?.getSurfaceForTemplateTexture(containerView, textureIndex)

    fun setRecording(textureView: CustomTextureView, value: Boolean) {
        findProgramItem(textureView)?.isRecording = value
    }


    interface RenderingPreparedListener {
        /**
         * Used to synchronize initialization
         */
        fun onSurfacePrepareCompleted()

        /**
         * Error during program initialization
         */
        fun onPrepareFailed(e: Exception)

        /**
         * Used to synchronize frame rendering
         */
        fun onFramePrepared()
    }
}

//useful with exoPlayer video. Because it is single color for some reason when we insert it for the first time.
private const val REDRAW_PROGRAM_WHEN_SURFACE_IS_CREATED = true