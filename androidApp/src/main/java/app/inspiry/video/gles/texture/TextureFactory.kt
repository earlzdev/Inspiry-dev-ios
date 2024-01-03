package app.inspiry.video.gles.texture

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.util.Size
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import app.inspiry.helpers.K
import app.inspiry.core.opengl.VideoPlayerParams
import app.inspiry.media.TextureParams
import app.inspiry.video.gles.Program
import app.inspiry.video.grafika.GlUtil
import app.inspiry.video.player.PlayerCore
import app.inspiry.video.player.controller.GlVideoPlayerController
import app.inspiry.video.player.decoder.TextureSize
import app.inspiry.video.player.decoder.VideoInfo
import app.inspiry.video.renderThread
import app.inspiry.video.renderThreadHandler
import java.lang.ref.WeakReference

/**
 * Factory used to control template (canvas) and video decoder textures
 */
class TextureFactory(val program: Program, isRecording: Boolean) : GlVideoPlayerController.Callback {

    //we store SurfaceTexture because if we lose a reference to it, it will be released automatically
    private val videoMap = mutableMapOf<String, Triple<Surface, SurfaceTexture, Int>>()
    private val removeVideoMap = mutableMapOf<String, Int>()
    val players = PlayerCore(this, isRecording)
    private val templateTextures = mutableMapOf<String, Triple<Surface, SurfaceTexture, Int>>()
    private val layoutChangeListeners =
        mutableMapOf<WeakReference<View>, View.OnLayoutChangeListener>()

    private fun getTemplateUri(containerView: ViewGroup, textureIndex: Int) =
        "player://template/${containerView.hashCode()}/$textureIndex"

    fun getSurfaceForTemplateTexture(containerView: ViewGroup, textureIndex: Int): Surface? {
        val sourceUri = getTemplateUri(containerView, textureIndex)
        return templateTextures[sourceUri]?.first
    }

    fun createTemplateTexture(
        program: Program,
        textureIndex: Int,
        containerView: ViewGroup,
        params: TextureParams
    ) {
        val sourceUri = getTemplateUri(containerView, textureIndex)
        val textureId = createTexture()
        val textureName = getTextureName(textureIndex)

        val (surfaceTexture, surface) = createSurfaceTexture(textureId)
        surfaceTexture.setDefaultBufferSize(containerView.width, containerView.height)
        templateTextures[sourceUri] = Triple(surface, surfaceTexture, textureId)


        debug {
            "createTemplateTexture: $sourceUri, textureId $textureId, textureName $textureName," +
                    " alreadyExisted ${templateTextures.contains(sourceUri)}"
        }

        program.addExternalTexture(
            textureIndex, textureName, textureId, TextureSize(containerView.width, containerView.height),
            params, null, true
        )

        containerView.post {
            renderThread {
                sendTextureAvailable(textureId, true)
                program.setTextureSize(textureId, TextureSize(containerView.width, containerView.height))
                program.checkReady()
            }

            val onLayoutChangeListener =
                View.OnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
                    debug { "onLayoutChange size ${Size(v.width, v.height)}" }

                    val surfaceTexture = templateTextures[sourceUri]?.second
                    surfaceTexture?.setDefaultBufferSize(v.width, v.height)
                    program.setTextureSize(textureId, TextureSize(v.width, v.height))
                }
            containerView.addOnLayoutChangeListener(onLayoutChangeListener)
            layoutChangeListeners[WeakReference(containerView)] = onLayoutChangeListener
        }
    }

    @Suppress("NAME_SHADOWING")
    fun createDecoderTexture(
        program: Program,
        textureIndex: Int,
        sourceUri: String,
        params: TextureParams,
        playerParams: VideoPlayerParams
    ) {
        debug { "createDecoderTexture: $sourceUri videoStartTimeUs ${playerParams.videoStartTimeUs}" }

        var textureId = videoMap[sourceUri]?.third

        val textureName = getTextureName(textureIndex)
        val isTextureNew = textureId == null
        if (textureId == null) {
            textureId = createTexture()
        }
        program.addExternalTexture(
            textureIndex, textureName, textureId, getVideoSize(sourceUri),
            params, playerParams, false
        )
        if (isTextureNew) {
            bindVideoPlayerAsync(sourceUri, textureId, playerParams)
        } else {
            players.setParamsAsync(sourceUri, playerParams)
            setTextureSizeAndRotation(sourceUri, getVideoSize(sourceUri)!!)
            notifyTextureReady(sourceUri)
        }
    }

    fun createBitmapTexture(
        program: Program,
        textureIndex: Int,
        bitmap: Bitmap,
        params: TextureParams
    ) {
        debug { "createBitmapTexture" }
        val textureId = createTexture()
        program.addBitmapTexture(
            textureIndex, getTextureName(textureIndex), textureId,
            TextureSize(bitmap.width, bitmap.height), bitmap, params
        )
    }

    private fun createTexture(): Int {
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        GlUtil.checkGlError("glGenTextures")
        val textureId = textures[0]
        debug { "createTexture: textureId - $textureId" }
        return textureId
    }

    private fun getTextureName(textureIndex: Int) =
        String.format(TEXTURE_VARIABLE_NAME, textureIndex)

    private fun bindVideoPlayerAsync(
        sourceUri: String,
        textureId: Int,
        playerParams: VideoPlayerParams
    ) {
        val alivePrograms = isProgramAlive(textureId)
        if (alivePrograms) {
            val (surfaceTexture, surface) = createSurfaceTexture(textureId)
            videoMap[sourceUri] = Triple(surface, surfaceTexture, textureId)
            players.registerVideoPlayerAsync(sourceUri, surfaceTexture, surface, playerParams)
        }
    }

    private fun isProgramAlive(textureId: Int) = program.hasTexture(textureId)


    @SuppressLint("Recycle")
    private fun createSurfaceTexture(
        textureId: Int
    ): Pair<SurfaceTexture, Surface> {

        val surfaceTexture = SurfaceTexture(textureId)
        val surface = Surface(surfaceTexture)
        surfaceTexture.setOnFrameAvailableListener({

            if (surface.isValid) {
                it.updateTexImage()
                sendTextureAvailable(textureId, true)
            }


        }, renderThreadHandler)

        return surfaceTexture to surface
    }

    private fun sendTextureAvailable(textureId: Int, isNewFrame: Boolean) {
        if (isProgramAlive(textureId)) {
            program.onExternalTextureAvailable(textureId, isNewFrame)
        }
    }

    fun releaseTextures() {

        layoutChangeListeners.forEach { entry ->
            val view = entry.key.get()
            view?.removeOnLayoutChangeListener(entry.value)
        }
        layoutChangeListeners.clear()

        videoMap.forEach { unbindVideoPlayerAsync(it.key, it.value.third) }
        videoMap.clear()

        templateTextures.forEach {
            if (it.value.first.isValid)
                it.value.first.release()
            it.value.second.release()

            GlUtil.removeTexture(it.value.third)
        }
        templateTextures.clear()
    }

    private fun unbindVideoPlayerAsync(sourceUri: String, textureId: Int) {
        debug { "unbindVideoPlayer $sourceUri" }
        removeVideoMap[sourceUri] = textureId
        players.unregisterPlayerAsync(sourceUri)
    }

    override fun onPlayerReleased(sourceUri: String) {
        renderThread {
            debug { "onPlayerReleased: $sourceUri" }
            val textureId = removeVideoMap.remove(sourceUri)
            if (textureId != null)
                GlUtil.removeTexture(textureId)
            debug { "removeTexture: textureId - $textureId" }
        }
    }

    override fun onPlayerFailure(sourceUri: String, t: Exception) {
        notifyTextureError(sourceUri, t)
    }

    private fun notifyTextureError(uri: String, t: Exception) {
        val textureId = videoMap[uri] ?: return
        if (isProgramAlive(textureId.third))
            program.sendError(t)
    }

    override fun onPlayerCreated(sourceUri: String, videoInfo: VideoInfo) {
        debug { "onPlayerCreated: $sourceUri" }
        setTextureSizeAndRotation(sourceUri, videoInfo.videoSize)
        notifyTextureReady(sourceUri)
    }

    private fun notifyTextureReady(uri: String) {
        val textureId = videoMap[uri] ?: return
        if (program.hasTexture(textureId.third))
            program.checkReady()
    }

    private fun getVideoSize(sourceUri: String) =
        players.getVideoInfo(sourceUri)?.videoSize

    private fun setTextureSizeAndRotation(uri: String, size: TextureSize) {
        val textureId = videoMap[uri] ?: return
        program.setTextureSize(textureId.third, size)
    }

    override fun onFrameSkipped(sourceUri: String) {
        renderThread {
            val textureId = videoMap[sourceUri] ?: return@renderThread
            sendTextureAvailable(textureId.third, false)
        }
    }

    fun executeOnPlayer(program: Program, onlyForEditTexture: Boolean, block: (GlVideoPlayerController) -> Unit) {
        for ((sourceUri, textureData) in videoMap) {

            val execute: Boolean
            if (onlyForEditTexture) {
                execute = program.isTransformTexture(textureData.third)
            } else {
                execute = program.hasTexture(textureData.third)
            }
            if (execute)
                players.execute(sourceUri, block)
        }
    }

    private inline fun debug(msg: () -> String) {
        K.d(K.TAG_TEXTURE_FACTORY) {
            msg() + ", textures.size = ${videoMap.size + templateTextures.size}" +
                    ", removeVideoMap.size = ${removeVideoMap.size}"
        }
    }
}

private const val TEXTURE_VARIABLE_NAME = "sTexture%d"