package app.inspiry.video.gles.texture

import android.graphics.Bitmap
import android.opengl.GLES11Ext
import android.opengl.GLES20
import app.inspiry.helpers.K
import app.inspiry.core.opengl.PlayerParams
import app.inspiry.media.TextureParams
import app.inspiry.video.gles.AttributeCore
import app.inspiry.video.grafika.GlUtil
import app.inspiry.video.player.decoder.TextureSize

/**
 * Used to add, change textures in the program
 */
class TextureCore(private val programId: Int) {

    val textureList = mutableListOf<Texture>()
    private val externalTextureHasNewFrame = HashMap<Int, Boolean>()

    val isReady: Boolean
        get() = textureList.all { it.isReady }

    fun hasTexture(textureId: Int) =
        textureList.any { it.id == textureId }

    fun getTexture(textureId: Int) =
        textureList.firstOrNull { it.id == textureId }

    fun getEditableTexture() =
        textureList.firstOrNull { it.isTransformTexture }


    fun isTransformTexture(textureId: Int) =
        textureList.find { it.id == textureId }?.isTransformTexture == true

    fun enableTextures() {
        textureList.forEachIndexed { index, texture ->
            val textureLocation = getTextureLocation(texture)
            texture.enable(textureLocation, index)
        }
    }

    private fun getTextureLocation(texture: Texture): Int {
        val textureLoc = GLES20.glGetUniformLocation(programId, texture.name)
        GlUtil.checkGlError("glGetUniformLocation")
        return textureLoc
    }

    fun addBitmapTexture(
        textureIndex: Int,
        textureName: String,
        textureId: Int,
        textureSize: TextureSize,
        bitmap: Bitmap,
        params: TextureParams
    ) {
        val texture = createTexture(
            textureIndex, textureName, textureId, textureSize,
            GLES20.GL_TEXTURE_2D, params, bitmap, null, false
        )
        textureList.add(texture)
        reorderTexture()
    }

    private fun reorderTexture() {
        textureList.sortBy { !it.isCanvasTexture }
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
        val texture = createTexture(
            textureIndex, textureName, textureId, textureSize,
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES, params, null, playerParams, isCanvasTexture
        )
        externalTextureHasNewFrame[textureId] = false
        textureList.add(texture)
        reorderTexture()
    }

    private fun createTexture(
        textureIndex: Int,
        textureName: String,
        textureId: Int,
        textureSize: TextureSize?,
        textureTarget: Int,
        params: TextureParams,
        bitmap: Bitmap?,
        playerParams: PlayerParams?,
        isCanvasTexture: Boolean
    ) = Texture(
        textureIndex, textureName, textureTarget, textureId,
        textureSize, params, bitmap, playerParams, isCanvasTexture
    )

    fun disableTextures() {
        textureList.forEach { it.disable() }
    }

    fun onExternalTextureAvailable(textureId: Int, isNewFrame: Boolean, isRecording: Boolean) {

        val oldValue = externalTextureHasNewFrame[textureId]

        //condition also can be isRecording || oldValue || isNewFrame. But in such case we can face an issue
        // that frames are not updating of other textures if the player has finished or not started yet
        if (oldValue != null)
            externalTextureHasNewFrame[textureId] = true

        //for better synchronization
        if (CANVAS_TEXTURE_SHOULD_BE_LAST) {
            if (!isRecording && isCanvasTexture(textureId)) {
                textureList.forEach {
                    if (!it.isCanvasTexture && it.isExternalTexture) {
                        externalTextureHasNewFrame[it.id] = false
                    }
                }
            }
        }
    }

    private fun isCanvasTexture(textureId: Int) =
        getTexture(textureId)?.isCanvasTexture == true

    fun isAvailable() = externalTextureHasNewFrame.values.all { it }

    fun setNotAvailable() {
        setAvailableAll(false)
    }

    private fun setAvailableAll(value: Boolean) {
        val iterator = externalTextureHasNewFrame.iterator()
        while (iterator.hasNext()) {
            val prop = iterator.next()
            prop.setValue(value)
        }

        if (externalTextureHasNewFrame.isNotEmpty() && externalTextureHasNewFrame.any { it.value })
            throw IllegalStateException("didn't work")
    }

    fun setParameters(attributeCore: AttributeCore, viewWidth: Int, viewHeight: Int) {
        textureList.forEach {
            it.setParameters(attributeCore, viewWidth, viewHeight)
        }
    }

    fun release() {
        K.i(K.TAG_GLES_PROGRAM) {
            "release textures ${textureList}"
        }
        externalTextureHasNewFrame.clear()
        removeImageTexture()
    }

    private fun removeImageTexture() {
        textureList.filter { !it.isExternalTexture }
            .forEach { GlUtil.removeTexture(it.id) }
    }

    fun setTextureSize(textureId: Int, size: TextureSize) {
        findTexture(textureId)?.setSize(size)
    }

    private fun findTexture(textureId: Int) =
        textureList.firstOrNull { it.id == textureId }

    fun debugInfo() = "externalTextures ${externalTextureHasNewFrame}"


    companion object {
        const val CANVAS_TEXTURE_SHOULD_BE_LAST = false
    }
}