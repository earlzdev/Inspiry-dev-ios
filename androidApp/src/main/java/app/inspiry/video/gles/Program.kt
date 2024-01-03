package app.inspiry.video.gles

import android.graphics.Bitmap
import android.opengl.GLES20
import android.os.Message
import app.inspiry.core.data.FRAME_IN_MILLIS
import app.inspiry.helpers.K
import app.inspiry.core.opengl.PlayerParams
import app.inspiry.video.gles.texture.TextureCore
import app.inspiry.media.TextureParams
import app.inspiry.core.data.TransformMediaData
import app.inspiry.video.grafika.GlUtil
import app.inspiry.video.player.decoder.TextureSize
import app.inspiry.video.renderThreadHandler

/**
 * Used to prepare and execute an OpenGL program
 */
class Program(vertexShaderSource: String, fragmentShaderSource: String, var isRecording: Boolean) {

    private val programId = GlUtil.createProgram(vertexShaderSource, fragmentShaderSource)
    private val textureCore: TextureCore
    private val attributeCore: AttributeCore
    private var availableListener: (() -> Unit)? = null
    private var lifecycleListener: LifecycleListener? = null
    var lastError: Exception? = null
    var isReady = false
        private set

    private val handlerForDelayedDraw = renderThreadHandler

    init {
        setupProgram()
        textureCore = TextureCore(programId)
        attributeCore = AttributeCore(programId)
    }

    fun hasTexture(textureId: Int) =
        textureCore.hasTexture(textureId)

    fun isTransformTexture(textureId: Int) = textureCore.isTransformTexture(textureId)

    private fun setupProgram() {
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        GLES20.glClearColor(0f, 0f, 0f, 0f)
    }

    fun addExternalTexture(
        textureIndex: Int,
        textureName: String,
        textureId: Int,
        textureSize: TextureSize?,
        params: TextureParams,
        playerParams: PlayerParams?,
        isCanvasTexture: Boolean
    ) {
        debug { "addExternalTexture $textureId, ${textureCore.debugInfo()}" }
        textureCore.addExternalTexture(textureIndex, textureName, textureId,
            textureSize, params, playerParams, isCanvasTexture)
    }

    fun addBitmapTexture(
        textureIndex: Int,
        textureName: String,
        textureId: Int,
        textureSize: TextureSize,
        bitmap: Bitmap,
        params: TextureParams
    ) {
        debug { "addBitmapTexture $textureId" }
        textureCore.addBitmapTexture(textureIndex, textureName, textureId, textureSize,
            bitmap, params)
    }

    fun drawProgram(width: Int, height: Int) {
        enableProgram()

        attributeCore.apply {
            setParameters()
            enableAttributes()
        }

        textureCore.apply {
            enableTextures()
            setParameters(attributeCore, width, height)
        }

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GlUtil.checkGlError("glClear: programId - $programId")

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GlUtil.checkGlError("glDrawArrays")

        attributeCore.disableAttributes()
        textureCore.disableTextures()
        disableProgram()
    }

    private fun enableProgram() {
        verbose { "enableProgram" }
        GLES20.glUseProgram(programId)
        GlUtil.checkGlError("glUseProgram")
    }

    private fun disableProgram() {
        verbose { "disableProgram" }
        GLES20.glUseProgram(0)
        GlUtil.checkGlError("glUseProgram")
    }

    fun release() {
        debug { "release" }
        GLES20.glDeleteProgram(programId)
        GlUtil.checkGlError("glDeleteProgram")
        textureCore.release()
    }

    private fun notifyTextureAvailable() {
        textureCore.setNotAvailable()
        availableListener?.invoke()
    }

    private val runnableForceNotifyTextureAvailable = {
        notifyTextureAvailable()
    }

    //should be called before draw in EGLProgramItem
    fun removeDrawProgramDelayed() {
        handlerForDelayedDraw.removeMessages(WHAT_MESSAGE_DELAYED_DRAW)
    }

    /**
     * Here we implement a mechanism for delayed drawing.
     * In order to update the program (perform draw)
     * we need to get updates from all textures.
     * If within specified time we don't get updates from a texture
     * then we perform drawing.
     */
    fun onExternalTextureAvailable(textureId: Int, isNewFrame: Boolean) {
        textureCore.onExternalTextureAvailable(textureId, isNewFrame, isRecording)

        if (textureCore.isAvailable()) {
            notifyTextureAvailable()

        } else {

            if (PERFORM_DELAYED_DRAWING && !handlerForDelayedDraw.hasMessages(WHAT_MESSAGE_DELAYED_DRAW)) {

                val msg = Message.obtain(handlerForDelayedDraw, runnableForceNotifyTextureAvailable)
                msg.what = WHAT_MESSAGE_DELAYED_DRAW
                handlerForDelayedDraw.sendMessageDelayed(msg, DELAY_TO_DRAW_IF_NOT_RECEIVED_ALL_TEXTURES)
            }
        }
    }

    fun setOnAvailableListener(block: () -> Unit) {
        availableListener = block
    }

    fun setTransform(textureTransformData: TransformMediaData) {
        textureCore.getEditableTexture()
            ?.setTransform(textureTransformData)
    }

    fun setBlurRadius(radius: Float): Boolean {
        return textureCore.textureList.any { it.setBlurRadius(radius) }
    }

    fun setTextureSize(textureId: Int, size: TextureSize) {
        textureCore.setTextureSize(textureId, size)
    }

    fun registerLifecycleListener(lifecycleListener: LifecycleListener) {
        this.lifecycleListener = lifecycleListener
        checkReady()
    }

    fun unregisterLifecycleListener() {
        this.lifecycleListener = null
    }

    fun checkReady() {
        if (!isReady && textureCore.isReady && lifecycleListener != null) {
            isReady = true
            lifecycleListener?.onProgramCreated()
        }
    }

    fun sendError(t: Exception) {
        lastError = t
        lifecycleListener?.onProgramFailure(t)
    }

    interface LifecycleListener {
        fun onProgramCreated()
        fun onProgramFailure(t: Exception)
    }

    private inline fun debug(msg: () -> String) {
        K.d(K.TAG_GLES_PROGRAM) { "$programId ${msg()}" }
    }

    private inline fun verbose(msg: () -> String) {
        K.v(K.TAG_GLES_PROGRAM) { "$programId ${msg()}" }
    }

    companion object {
        const val DELAY_TO_DRAW_IF_NOT_RECEIVED_ALL_TEXTURES = FRAME_IN_MILLIS.toLong()
        const val WHAT_MESSAGE_DELAYED_DRAW = 10
        const val PERFORM_DELAYED_DRAWING = true
    }
}