package app.inspiry.video.egl

import android.graphics.Bitmap
import android.view.ViewGroup
import app.inspiry.ap
import app.inspiry.helpers.K
import app.inspiry.video.CustomTextureView
import app.inspiry.video.gles.Program
import app.inspiry.video.gles.texture.TextureFactory
import app.inspiry.core.data.TransformMediaData
import app.inspiry.video.grafika.EglCore
import app.inspiry.video.player.controller.GlVideoPlayerController
import app.inspiry.video.player.decoder.VideoInfo
import app.inspiry.video.program.DeferredProgramCreator
import app.inspiry.video.renderThread
import java.io.File

/**
 * Used to execute program and draw it on textureView
 */
class EGLProgramItem(
    textureView: CustomTextureView,
    textureViewId: Long,
    eglCore: EglCore,
    private var programCreator: DeferredProgramCreator,
    private var lifecycleListener: EGLOutputItem.LifecycleListener? = null,
    isRecording: Boolean
) : EGLOutputItem.LifecycleListener, Program.LifecycleListener {

    private val outputItem = EGLOutputItem(eglCore, textureView, textureViewId, this)
    private var program: Program? = null
    private var textureFactory: TextureFactory? = null

    val isHasError: Boolean
        get() = totalProgramLastError != null

    val totalProgramLastError: Exception?
        get() {
            if (innerLastError != null) return innerLastError
            else if (program?.lastError != null) return program!!.lastError!!
            else if (outputItem.lastError != null) return outputItem.lastError
            else return null
        }

    private var innerLastError: Exception? = null

    // used to prevent drawing if first frame was not available yet to avoid black blicks
    var firstFrameAvailable = false

    var isRecording: Boolean = isRecording
        set(value) {
            field = value
            program?.isRecording = value
            textureFactory?.players?.isRecording = value
        }

    init {
        outputItem.checkSurfaceTextureAvailable()
    }

    override fun onSurfaceCreated(textureViewId: Long) {
        createProgram()
    }

    fun getVideoInfo(uri: String): VideoInfo? = textureFactory?.players?.getVideoInfo(uri)

    private fun createProgram() {
        if (program != null || !programCreator.isReady) return
        outputItem.makeCurrent()

        val res = programCreator.createProgramAndTextures(isRecording, ap)

        program = res.first
        textureFactory = res.second

        program!!.registerLifecycleListener(this@EGLProgramItem)
        program!!.setOnAvailableListener {
            firstFrameAvailable = true
            this@EGLProgramItem.drawProgram()
        }
    }

    fun drawProgram() {

        if (isHasError || !firstFrameAvailable) return

        try {
            program?.removeDrawProgramDelayed()

            outputItem.draw { width, height ->
                program?.drawProgram(width, height)
            }
        } catch (ex: Exception) {
            K.e(ex)
            sendError(ex)
        }
    }

    private fun sendError(e: Exception) {
        innerLastError = e
        lifecycleListener?.onSurfaceCreated(outputItem.textureViewId)
    }

    override fun onSurfaceReleased(textureViewId: Long) {
        releaseProgram()
        lifecycleListener?.onSurfaceReleased(textureViewId)
    }

    private fun releaseProgram() {
        if (program == null) return
        program?.run {
            unregisterLifecycleListener()
            textureFactory?.releaseTextures()
            textureFactory = null
            program = null
            release()
        }
    }

    fun recreateProgram(programCreator: DeferredProgramCreator) {
        releaseProgram()
        this.programCreator = programCreator
        createProgram()
    }

    override fun onTextureViewDestroyed(textureViewId: Long) {
        releaseProgram()
        lifecycleListener?.run {
            lifecycleListener = null
            onTextureViewDestroyed(textureViewId)
        }
    }

    override fun onDrawFrame(textureViewId: Long) {
        lifecycleListener?.onDrawFrame(textureViewId)
    }

    override fun onSurfaceSizeChanged(textureViewId: Long, width: Int, height: Int) {
        if (program?.isReady == true) {
            renderThread {
                drawProgram()
            }
        }

        lifecycleListener?.onSurfaceSizeChanged(textureViewId, width, height)
    }

    fun removeNotUsedProgram() {
        outputItem.removeNotUsedTextureView()
    }

    fun removeProgram() {
        outputItem.removeTextureView()
    }

    fun setTransform(textureTransformData: TransformMediaData) {
        program?.setTransform(textureTransformData)
    }

    fun setBlurRadius(radius: Float) {
        val blurApplied = program?.setBlurRadius(radius)

        // should we really call it?
        /*if (blurApplied == true) {
            renderThread { drawProgram() }
        }*/
    }

    fun saveAsFile(file: File, outputFormat: Bitmap.CompressFormat) {
        outputItem.saveAsFile(file, outputFormat)
    }

    fun executeOnPlayer(onlyForEditTexture: Boolean, block: (GlVideoPlayerController) -> Unit) {
        program?.let { textureFactory?.executeOnPlayer(it, onlyForEditTexture, block) }
    }

    override fun onProgramCreated() {
        lifecycleListener?.onSurfaceCreated(outputItem.textureViewId)
    }

    override fun onProgramFailure(t: Exception) {
        sendError(t)
    }

    fun getSurfaceForTemplateTexture(containerView: ViewGroup, textureIndex: Int) =
        textureFactory?.getSurfaceForTemplateTexture(containerView, textureIndex)

}
