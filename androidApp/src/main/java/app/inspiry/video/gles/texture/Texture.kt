package app.inspiry.video.gles.texture

import android.graphics.Bitmap
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLUtils
import app.inspiry.core.data.TransformMediaData
import app.inspiry.core.opengl.PlayerParams
import app.inspiry.media.TextureMatrix
import app.inspiry.media.TextureParams
import app.inspiry.video.gles.AttributeCore
import app.inspiry.video.gles.texture.matrix.*
import app.inspiry.video.grafika.GlUtil
import app.inspiry.video.player.decoder.TextureSize
import kotlin.math.max
import kotlin.math.min

/**
 * One texture in the program
 */
class Texture(
    private val index: Int,
    val name: String,
    private val target: Int,
    val id: Int,
    private var size: TextureSize?,
    val params: TextureParams,
    private var bitmap: Bitmap? = null,
    val playerParams: PlayerParams?,
    val isCanvasTexture: Boolean
) {

    private var isInitialized = false
    private var glBlurRadius = getGlBlurRadius(0F)

    val isReady: Boolean
        get() = size != null

    val isExternalTexture: Boolean
        get() = target == GLES11Ext.GL_TEXTURE_EXTERNAL_OES

    val isTransformTexture: Boolean
        get() = matrices.hasTransformMatrix()

    private val matrices: List<TextureMatrix<*>>
        get() = params.matrices

    private val pixelSizeUniformName by lazy { getPixelSizeUniformName(index) }
    private val blurRadiusUniformName by lazy { getBlurRadiusUniformName(index) }

    fun setSize(size: TextureSize) {
        this.size = size
    }

    private fun setupParameters() {
        GLES20.glTexParameterf(target, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST.toFloat())
        GLES20.glTexParameterf(target, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat())
        GLES20.glTexParameteri(target, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(target, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        GlUtil.checkGlError("glTexParameter")
    }

    fun enable(textureLoc: Int, unitIndex: Int) {
        GLES20.glActiveTexture(unitIndex.toUnit())
        GlUtil.checkGlError("glActiveTexture")
        GLES20.glBindTexture(target, id)
        GlUtil.checkGlError("glBindTexture")
        GLES20.glUniform1i(textureLoc, unitIndex)
        GlUtil.checkGlError("glUniform1i")
        initOnce()
    }

    private fun Int.toUnit() = GLES20.GL_TEXTURE0 + this

    private fun initOnce() {
        if (!isInitialized) {
            isInitialized = true
            setupParameters()
            bitmap?.run {
                bitmap = null
                setBitmap(this)
            }
        }
    }

    private fun setBitmap(bitmap: Bitmap) {
        GLUtils.texImage2D(target, 0, bitmap, 0)
        GlUtil.checkGlError("texImage2D")
    }

    fun disable() {
        GLES20.glBindTexture(target, 0)
        GlUtil.checkGlError("glUnbindTexture")
    }

    fun setParameters(attributeCore: AttributeCore, viewWidth: Int, viewHeight: Int) {
        val width = size?.width ?: return
        val height = size?.height ?: return
        val rotation = size?.rotation ?: 0f
        matrices.setUniformMat4(attributeCore, viewWidth, viewHeight, width, height, rotation)

        if (params.isPixelSizeAvailable || params.isBlurEffectAvailable) {
            val aspectRatio = height / width.toFloat()
            val x = 1f / DEFAULT_WIDTH
            val y = x / aspectRatio
            if (params.isPixelSizeAvailable) {
                attributeCore.setUniform2f(pixelSizeUniformName, x, y)
            }
            if (params.isBlurEffectAvailable) {
                attributeCore.setUniform2f(blurRadiusUniformName, x / glBlurRadius, y / glBlurRadius)
            }
        }
    }

    private fun getPixelSizeUniformName(index: Int) =
        String.format(PIXEL_SIZE_VARIABLE_NAME, index)

    private fun getBlurRadiusUniformName(index: Int) =
        String.format(BLUR_SIZE_VARIABLE_NAME, index)

    fun setTransform(textureTransformData: TransformMediaData) {
        matrices.setTransform(textureTransformData)
    }

    fun setBlurRadius(radius: Float): Boolean {
        if (params.isBlurEffectAvailable) {
            val newBlur = getGlBlurRadius(radius)
            val changed = newBlur != glBlurRadius
            glBlurRadius = newBlur
            return changed
        }
        return false
    }

    private fun getGlBlurRadius(radius: Float) = radius
        .boundBlurRadius()
        .mapBlurRadius()
        .ignoreZero()

    private fun Float.boundBlurRadius() =
        max(min(this, BLUR_MAX_VALUE), BLUR_MIN_VALUE)

    private fun Float.mapBlurRadius() =
        this / BLUR_MAX_VALUE * (BLUR_MAX_GL_VALUE - BLUR_MIN_GL_VALUE) + BLUR_MIN_GL_VALUE

    private fun Float.ignoreZero() =
        if (this == 0F) 0.00001F else this

    override fun toString(): String {
        return "Texture(index=$index, name='$name', target=$target, id=$id, size=$size, isCanvasTexture=$isCanvasTexture, isExternalTexture=$isExternalTexture)"
    }


    companion object {
        private const val PIXEL_SIZE_VARIABLE_NAME = "uPixelSize%d"
        private const val BLUR_SIZE_VARIABLE_NAME = "uBlurSize%d"
        private const val BLUR_MIN_VALUE = 0F
        private const val BLUR_MAX_VALUE = 25F
        private const val BLUR_MIN_GL_VALUE = 0.8F
        private const val BLUR_MAX_GL_VALUE = 0.2F
        private const val DEFAULT_WIDTH = 1080
    }
}